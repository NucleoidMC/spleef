package xyz.nucleoid.spleef.game;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;
import xyz.nucleoid.plasmid.api.game.player.MutablePlayerSet;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.spleef.Spleef;
import xyz.nucleoid.spleef.game.map.SpleefMap;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;
import xyz.nucleoid.stimuli.event.projectile.ProjectileHitEvent;

public final class SpleefActive {
    private final GameSpace gameSpace;
    private final ServerLevel world;
    private final SpleefMap map;
    private final SpleefConfig config;

    private final SpleefTimerBar timerBar;
    private long nextLevelDropTime = -1;

    private final MutablePlayerSet toolRecipients;
    private long restockTime = -1;

    private final boolean ignoreWinState;
    private boolean hasEnded = false;
    private long closeTime = -1;

    private SpleefActive(GameSpace gameSpace, ServerLevel world, SpleefMap map, SpleefConfig config, GlobalWidgets widgets) {
        this.gameSpace = gameSpace;
        this.world = world;
        this.map = map;
        this.config = config;

        this.toolRecipients = new MutablePlayerSet(gameSpace.getServer());

        this.ignoreWinState = gameSpace.getPlayers().size() <= 1;

        this.timerBar = SpleefTimerBar.create(widgets);
    }

    public static void open(GameSpace gameSpace, ServerLevel world, SpleefMap map, SpleefConfig config) {
        gameSpace.setActivity(activity -> {
            var widgets = GlobalWidgets.addTo(activity);

            var active = new SpleefActive(gameSpace, world, map, config, widgets);

            activity.deny(GameRuleType.CRAFTING);
            activity.deny(GameRuleType.PORTALS);
            activity.deny(GameRuleType.PVP);
            activity.deny(GameRuleType.BLOCK_DROPS);
            activity.deny(GameRuleType.FALL_DAMAGE);
            activity.deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.THROW_ITEMS);

            if (config.unstableTnt()) {
                activity.allow(GameRuleType.UNSTABLE_TNT);
                activity.allow(GameRuleType.PVP);
            }

            activity.listen(GameActivityEvents.ENABLE, active::onEnable);
            activity.listen(GameActivityEvents.TICK, active::tick);

            activity.listen(GamePlayerEvents.OFFER, offer -> offer.intent() == JoinIntent.SPECTATE ? offer.accept() : offer.pass());
            activity.listen(GamePlayerEvents.ACCEPT, active::acceptPlayer);
            activity.listen(GamePlayerEvents.REMOVE, active::removePlayer);

            activity.listen(ProjectileHitEvent.BLOCK, active::onBlockHit);

            activity.listen(PlayerDamageEvent.EVENT, active::onPlayerDamage);
            activity.listen(PlayerDeathEvent.EVENT, active::onPlayerDeath);
        });
    }

    private JoinAcceptorResult acceptPlayer(JoinAcceptor offer) {
        var spawn = this.map.getSpawn();
        return offer.teleport(this.world, Vec3.atCenterOf(spawn))
                .thenRunForEach(player -> player.setGameMode(GameType.SPECTATOR));
    }

    private void onEnable() {
        int index = 0;
        for (var player : this.gameSpace.getPlayers().participants()) {
            this.spawnParticipant(player);

            if (this.config.tool().shouldReceiveTool(index)) {
                this.giveTool(player);
            }

            index++;
        }

        for (var player : this.gameSpace.getPlayers().spectators()) {
            player.setGameMode(GameType.SPECTATOR);
        }
    }

    private void tick() {
        long time = this.world.getGameTime();

        if (this.closeTime > 0) {
            this.tickClosing(this.gameSpace, time);
            return;
        }

        if (this.config.decay() >= 0) {
            this.map.tickDecay(this.world);

            for (var player : this.gameSpace.getPlayers()) {
                if (player.isSpectator()) continue;

                var boundingBox = player.getBoundingBox();
                var box = new AABB(boundingBox.minX, boundingBox.minY - Mth.EPSILON, boundingBox.minZ, boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);

                var collisions = new BlockCollisions<>(player.level(), player, box, false, (pos, voxelShape) -> pos);

                while (collisions.hasNext()) {
                    var pos = collisions.next();
                    this.map.tryBeginDecayAt(pos, this.config.decay());
                }
            }
        }

        var lavaRise = this.config.lavaRise();
        if (lavaRise != null) {
            this.map.tickLavaRise(this.world, time, lavaRise);
        }

        if (this.config.levelBreakInterval() < 0) {
            this.timerBar.setBarNone();
        } else if (time > this.nextLevelDropTime) {
            if (this.nextLevelDropTime != -1) {
                this.map.tryDropLevel(this.world);
            }

            this.nextLevelDropTime = time + this.config.levelBreakInterval();
        } else if(map.getTopLevel() <= -1 & lavaRise != null) {
            timerBar.setBarLava();
        } else {
            long ticksToDrop = this.nextLevelDropTime - time;
            if (ticksToDrop % 20 == 0) {
                this.timerBar.update(ticksToDrop, this.config.levelBreakInterval());
            }
        }

        var projectiles = this.config.projectile();
        if (time > this.restockTime && projectiles != null) {
            if (this.restockTime != -1) {
                this.restockProjectiles(projectiles);
            }

            this.restockTime = time + projectiles.restockInterval();
        }

        var result = this.checkWinResult();
        if (result.win()) {
            this.broadcastWin(result);
            this.closeTime = time + 20 * 5;
        }
    }

    private void tickClosing(GameSpace gameSpace, long time) {
        if (time >= this.closeTime) {
            gameSpace.close(GameCloseReason.FINISHED);
        }
    }

    private void restockProjectiles(ProjectileConfig projectileConfig) {
        var projectileStack = projectileConfig.stack();

        for (var player : this.gameSpace.getPlayers()) {
            if (player.isSpectator()) continue;
            if (player.getInventory().countItem(projectileStack.item().value()) >= projectileConfig.maximum()) continue;

            player.getInventory().add(projectileStack.create());
            player.playSound(SoundEvents.ITEM_PICKUP, 1, 1);
        }
    }

    private void breakFloorBlock(BlockPos pos) {
        if (this.map.providedFloors.contains(this.world.getBlockState(pos))) {
            this.world.destroyBlock(pos, false);
        }
    }

    private EventResult onBlockHit(Projectile entity, BlockHitResult hitResult) {
        var projectiles = this.config.projectile();
        if (projectiles == null) return EventResult.DENY;

        int radius = projectiles.radius();
        if (radius <= 0) return EventResult.DENY;

        var breakPos = hitResult.getBlockPos();

        if (radius == 1) {
            this.breakFloorBlock(breakPos);
        } else {
            int innerRadius = projectiles.innerRadius();

            int radiusSquared = radius * radius;
            int innerRadiusSquared = innerRadius * innerRadius;

            for (var pos : BlockBounds.of(-radius, 0, -radius, radius, 0, radius)) {
                int distance = pos.getX() * pos.getX() + pos.getZ() * pos.getZ();
                if (distance >= radiusSquared) continue;
                if (distance < innerRadiusSquared && innerRadius > 0) continue;

                this.breakFloorBlock(pos.offset(breakPos));
            }
        }

        return EventResult.PASS;
    }

    private void broadcastWin(WinResult result) {
        var winningPlayer = result.winningPlayer();

        hasEnded = true;

        Component message;
        if (winningPlayer != null) {
            message = Component.translatable("text.spleef.win", winningPlayer.getDisplayName()).withStyle(ChatFormatting.GOLD);
        } else {
            message = Component.translatable("text.spleef.no_winners").withStyle(ChatFormatting.GOLD);
        }

        var players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.playSound(SoundEvents.VILLAGER_YES);
    }

    private EventResult onPlayerDamage(ServerPlayer player, DamageSource source, float amount) {
        if (!player.isSpectator() && isEliminatingSource(source) && !hasEnded) {
            this.eliminatePlayer(player);
        }
        return EventResult.DENY;
    }

    private static boolean isEliminatingSource(final DamageSource source) {
        return source.is(Spleef.ELIMINATES_PLAYERS);
    }

    private EventResult onPlayerDeath(ServerPlayer player, DamageSource source) {
        if (!player.isSpectator()) {
            this.eliminatePlayer(player);
        }
        return EventResult.DENY;
    }

    private void spawnParticipant(ServerPlayer player) {
        player.setGameMode(GameType.ADVENTURE);
        player.getInventory().clearContent();

        this.config.attributeModifiers().applyTo(player);
    }

    private void giveTool(ServerPlayer player) {
        if (player != null) {
            ItemStack stack = this.config.tool().createStack(player.level().getServer(), this.map);
            player.getInventory().add(stack);

            this.toolRecipients.add(player);
        }
    }

    private void removePlayer(ServerPlayer player) {
        if (this.toolRecipients.contains(player)) {
            this.giveTool(this.getNextToolRecipient());
        }
    }

    private void eliminatePlayer(ServerPlayer player) {
        var message = Component.translatable("text.spleef.eliminated", player.getDisplayName())
                .withStyle(ChatFormatting.RED);

        var players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);

        player.setGameMode(GameType.SPECTATOR);

        if (this.toolRecipients.contains(player)) {
            this.giveTool(this.getNextToolRecipient());
        }
    }

    private ServerPlayer getNextToolRecipient() {
        for (var player : this.gameSpace.getPlayers()) {
            if (!player.isSpectator() && !this.toolRecipients.contains(player)) {
                return player;
            }
        }

        return null;
    }

    private WinResult checkWinResult() {
        // for testing purposes: don't end the game if we only ever had one participant
        if (this.ignoreWinState) {
            return WinResult.no();
        }

        ServerPlayer winningPlayer = null;

        for (var player : this.gameSpace.getPlayers()) {
            if (!player.isSpectator()) {
                // we still have more than one player remaining
                if (winningPlayer != null) {
                    return WinResult.no();
                }

                winningPlayer = player;
            }
        }

        return WinResult.win(winningPlayer);
    }

    record WinResult(@Nullable ServerPlayer winningPlayer, boolean win) {
        static WinResult no() {
            return new WinResult(null, false);
        }

        static WinResult win(ServerPlayer player) {
            return new WinResult(player, true);
        }
    }
}
