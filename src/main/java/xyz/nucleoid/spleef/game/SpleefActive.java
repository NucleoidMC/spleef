package xyz.nucleoid.spleef.game;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.BlockHitListener;
import xyz.nucleoid.plasmid.game.event.GameOpenListener;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.OfferPlayerListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDamageListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;
import xyz.nucleoid.spleef.game.map.SpleefMap;

public final class SpleefActive {
    private final GameSpace gameSpace;
    private final SpleefMap map;
    private final SpleefConfig config;

    private final SpleefTimerBar timerBar;
    private long nextLevelDropTime = -1;

    private long restockTime = -1;

    private final boolean ignoreWinState;
    private long closeTime = -1;

    private SpleefActive(GameSpace gameSpace, SpleefMap map, SpleefConfig config, GlobalWidgets widgets) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;

        this.ignoreWinState = gameSpace.getPlayerCount() <= 1;

        this.timerBar = SpleefTimerBar.create(widgets);
    }

    public static void open(GameSpace gameSpace, SpleefMap map, SpleefConfig config) {
        gameSpace.openGame(game -> {
            GlobalWidgets widgets = new GlobalWidgets(game);

            SpleefActive active = new SpleefActive(gameSpace, map, config, widgets);

            game.setRule(GameRule.CRAFTING, RuleResult.DENY);
            game.setRule(GameRule.PORTALS, RuleResult.DENY);
            game.setRule(GameRule.PVP, RuleResult.DENY);
            game.setRule(GameRule.BLOCK_DROPS, RuleResult.DENY);
            game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
            game.setRule(GameRule.HUNGER, RuleResult.DENY);
            game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);

            game.on(GameOpenListener.EVENT, active::onOpen);

            game.on(OfferPlayerListener.EVENT, player -> JoinResult.ok());
            game.on(PlayerAddListener.EVENT, active::addPlayer);

            game.on(GameTickListener.EVENT, active::tick);
            game.on(BlockHitListener.EVENT, active::onBlockHit);

            game.on(PlayerDamageListener.EVENT, active::onPlayerDamage);
            game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
        });
    }

    private void onOpen() {
        for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
            this.spawnParticipant(player);
        }
    }

    private void addPlayer(ServerPlayerEntity player) {
        player.setGameMode(GameMode.SPECTATOR);
    }

    private void tick() {
        ServerWorld world = this.gameSpace.getWorld();
        long time = world.getTime();

        if (this.closeTime > 0) {
            this.tickClosing(this.gameSpace, time);
            return;
        }

        if (this.config.decay >= 0) {
            this.map.tickDecay(world);

            for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
                if (player.isSpectator()) continue;
                BlockPos landingPos = player.getLandingPos();
                this.map.tryBeginDecayAt(landingPos, this.config.decay);
            }
        }

        if (time > this.nextLevelDropTime) {
            if (this.nextLevelDropTime != -1) {
                this.map.tryDropLevel(world);
            }

            this.nextLevelDropTime = time + this.config.levelBreakInterval;
        } else {
            long ticksToDrop = this.nextLevelDropTime - time;
            if (ticksToDrop % 20 == 0) {
                this.timerBar.update(ticksToDrop, this.config.levelBreakInterval);
            }
        }

        if (time > this.restockTime && this.config.projectile.isPresent()) {
            if (this.restockTime != -1) {
                this.restockProjectiles(this.config.projectile.get());
            }

            this.restockTime = time + this.config.projectile.get().getRestockInterval();
        }

        WinResult result = this.checkWinResult();
        if (result.isWin()) {
            this.broadcastWin(result);
            this.closeTime = time + 20 * 5;
        }
    }

    private void tickClosing(GameSpace gameSpace, long time) {
        if (time >= this.closeTime) {
            gameSpace.close();
        }
    }

    private void restockProjectiles(ProjectileConfig projectileConfig) {
        ItemStack projectileStack = projectileConfig.getStack();

        for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
            if (player.isSpectator()) continue;
            if (player.inventory.count(projectileStack.getItem()) >= projectileConfig.getMaximum()) continue;

            player.inventory.insertStack(projectileStack.copy());
            player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1, 1);
        }
    }

    private void breakFloorBlock(ServerWorld world, BlockPos pos) {
        if (this.map.providedFloors.contains(world.getBlockState(pos))) {
            world.breakBlock(pos, false);
        }
    }

    private ActionResult onBlockHit(ProjectileEntity entity, BlockHitResult hitResult) {
        if (!this.config.projectile.isPresent()) return ActionResult.FAIL;

        ProjectileConfig projectileConfig = this.config.projectile.get();

        int radius = projectileConfig.getRadius();
        if (radius <= 0) return ActionResult.PASS;

        ServerWorld world = this.gameSpace.getWorld();
        BlockPos breakPos = hitResult.getBlockPos();

        if (radius == 1) {
            this.breakFloorBlock(world, breakPos);
        } else {
            int innerRadius = projectileConfig.getInnerRadius();

            int radiusSquared = radius * radius;
            int innerRadiusSquared = innerRadius * innerRadius;

            for (BlockPos pos : BlockPos.iterate(new BlockPos(-radius, 0, -radius), new BlockPos(radius, 0, radius))) {
                int distance = pos.getX() * pos.getX() + pos.getZ() * pos.getZ();
                if (distance >= radiusSquared) continue;
                if (distance < innerRadiusSquared && innerRadius > 0) continue;

                this.breakFloorBlock(world, pos.add(breakPos));
            }
        }

        return ActionResult.PASS;
    }

    private void broadcastWin(WinResult result) {
        ServerPlayerEntity winningPlayer = result.getWinningPlayer();

        Text message;
        if (winningPlayer != null) {
            message = winningPlayer.getDisplayName().shallowCopy().append(" has won the game!").formatted(Formatting.GOLD);
        } else {
            message = new LiteralText("The game ended, but nobody won!").formatted(Formatting.GOLD);
        }

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.sendSound(SoundEvents.ENTITY_VILLAGER_YES);
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        if (!player.isSpectator() && source == DamageSource.LAVA) {
            this.eliminatePlayer(player);
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        if (!player.isSpectator()) {
            this.eliminatePlayer(player);
        }
        return ActionResult.FAIL;
    }

    private void spawnParticipant(ServerPlayerEntity player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.inventory.clear();

        ItemStackBuilder shovelBuilder = ItemStackBuilder.of(this.config.tool)
                .setUnbreakable()
                .addEnchantment(Enchantments.EFFICIENCY, 2);

        for (BlockState state : this.map.providedFloors) {
            shovelBuilder.addCanDestroy(state.getBlock());
        }

        player.inventory.insertStack(shovelBuilder.build());
    }

    private void eliminatePlayer(ServerPlayerEntity player) {
        Text message = player.getDisplayName().shallowCopy().append(" has been eliminated!")
                .formatted(Formatting.RED);

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.sendSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);

        player.setGameMode(GameMode.SPECTATOR);
    }

    private WinResult checkWinResult() {
        // for testing purposes: don't end the game if we only ever had one participant
        if (this.ignoreWinState) {
            return WinResult.no();
        }

        ServerPlayerEntity winningPlayer = null;

        for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
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

    static class WinResult {
        final ServerPlayerEntity winningPlayer;
        final boolean win;

        private WinResult(ServerPlayerEntity winningPlayer, boolean win) {
            this.winningPlayer = winningPlayer;
            this.win = win;
        }

        static WinResult no() {
            return new WinResult(null, false);
        }

        static WinResult win(ServerPlayerEntity player) {
            return new WinResult(player, true);
        }

        public boolean isWin() {
            return this.win;
        }

        @Nullable
        public ServerPlayerEntity getWinningPlayer() {
            return this.winningPlayer;
        }
    }
}
