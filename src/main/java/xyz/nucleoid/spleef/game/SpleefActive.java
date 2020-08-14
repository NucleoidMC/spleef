package xyz.nucleoid.spleef.game;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.event.GameOpenListener;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.OfferPlayerListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDamageListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.PlayerRemoveListener;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.spleef.game.map.SpleefMap;

import javax.annotation.Nullable;

public final class SpleefActive {
    private final GameWorld gameWorld;
    private final SpleefMap map;
    private final SpleefConfig config;

    private final SpleefTimerBar timerBar;
    private long nextLevelDropTime = -1;

    private final boolean ignoreWinState;
    private long closeTime = -1;

    private SpleefActive(GameWorld gameWorld, SpleefMap map, SpleefConfig config) {
        this.gameWorld = gameWorld;
        this.map = map;
        this.config = config;

        this.ignoreWinState = gameWorld.getPlayerCount() <= 1;

        this.timerBar = gameWorld.addResource(new SpleefTimerBar());
    }

    public static void open(GameWorld gameWorld, SpleefMap map, SpleefConfig config) {
        SpleefActive active = new SpleefActive(gameWorld, map, config);

        gameWorld.openGame(game -> {
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
            game.on(PlayerRemoveListener.EVENT, active::removePlayer);

            game.on(GameTickListener.EVENT, active::tick);

            game.on(PlayerDamageListener.EVENT, active::onPlayerDamage);
            game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
        });
    }

    private void onOpen() {
        for (ServerPlayerEntity player : this.gameWorld.getPlayers()) {
            this.spawnParticipant(player);
        }
    }

    private void addPlayer(ServerPlayerEntity player) {
        player.setGameMode(GameMode.SPECTATOR);
        this.timerBar.addPlayer(player);
    }

    private void removePlayer(ServerPlayerEntity player) {
        this.timerBar.removePlayer(player);
    }

    private void tick() {
        ServerWorld world = this.gameWorld.getWorld();
        long time = world.getTime();

        if (this.closeTime > 0) {
            this.tickClosing(this.gameWorld, time);
            return;
        }

        if (this.config.decay >= 0) {
            this.map.tickDecay(world);

            for (ServerPlayerEntity player : this.gameWorld.getPlayers()) {
                BlockPos landingPos = player.getLandingPos();
                this.map.tryBeginDecayAt(world, landingPos, this.config.decay);
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

        WinResult result = this.checkWinResult();
        if (result.isWin()) {
            this.broadcastWin(result);
            this.closeTime = time + 20 * 5;
        }
    }

    private void tickClosing(GameWorld gameWorld, long time) {
        if (time >= this.closeTime) {
            gameWorld.close();
        }
    }

    private void broadcastWin(WinResult result) {
        ServerPlayerEntity winningPlayer = result.getWinningPlayer();

        Text message;
        if (winningPlayer != null) {
            message = winningPlayer.getDisplayName().shallowCopy().append(" has won the game!").formatted(Formatting.GOLD);
        } else {
            message = new LiteralText("The game ended, but nobody won!").formatted(Formatting.GOLD);
        }

        this.broadcastMessage(message);
        this.broadcastSound(SoundEvents.ENTITY_VILLAGER_YES);
    }

    private boolean onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        if (!player.isSpectator() && source == DamageSource.LAVA) {
            this.eliminatePlayer(player);
            return true;
        }
        return false;
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

        this.broadcastMessage(message);
        this.broadcastSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);

        player.setGameMode(GameMode.SPECTATOR);
    }

    // TODO: extract common broadcast utils into plasmid
    private void broadcastMessage(Text message) {
        for (ServerPlayerEntity player : this.gameWorld.getPlayers()) {
            player.sendMessage(message, false);
        }
    }

    private void broadcastSound(SoundEvent sound) {
        for (ServerPlayerEntity player : this.gameWorld.getPlayers()) {
            player.playSound(sound, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }

    private WinResult checkWinResult() {
        // for testing purposes: don't end the game if we only ever had one participant
        if (this.ignoreWinState) {
            return WinResult.no();
        }

        ServerPlayerEntity winningPlayer = null;

        for (ServerPlayerEntity player : this.gameWorld.getPlayers()) {
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
