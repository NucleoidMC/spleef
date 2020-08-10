package net.gegy1000.spleef.game;

import net.gegy1000.plasmid.game.GameWorld;
import net.gegy1000.plasmid.game.event.GameCloseListener;
import net.gegy1000.plasmid.game.event.GameOpenListener;
import net.gegy1000.plasmid.game.event.GameTickListener;
import net.gegy1000.plasmid.game.event.OfferPlayerListener;
import net.gegy1000.plasmid.game.event.PlayerAddListener;
import net.gegy1000.plasmid.game.event.PlayerDamageListener;
import net.gegy1000.plasmid.game.event.PlayerDeathListener;
import net.gegy1000.plasmid.game.event.PlayerRemoveListener;
import net.gegy1000.plasmid.game.player.JoinResult;
import net.gegy1000.plasmid.game.rule.GameRule;
import net.gegy1000.plasmid.game.rule.RuleResult;
import net.gegy1000.plasmid.util.ItemStackBuilder;
import net.gegy1000.spleef.game.map.SpleefMap;
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
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public final class SpleefActive {
    private final GameWorld gameWorld;
    private final SpleefMap map;
    private final SpleefConfig config;

    private final Set<ServerPlayerEntity> participants;

    private final SpleefSpawnLogic spawnLogic;

    private final SpleefTimerBar timerBar = new SpleefTimerBar();
    private long nextLevelDropTime = -1;

    private final boolean ignoreWinState;
    private long closeTime = -1;

    private SpleefActive(GameWorld gameWorld, SpleefMap map, SpleefConfig config, Set<ServerPlayerEntity> participants) {
        this.gameWorld = gameWorld;
        this.map = map;
        this.config = config;
        this.participants = new HashSet<>(participants);

        this.ignoreWinState = this.participants.size() <= 1;

        this.spawnLogic = new SpleefSpawnLogic(gameWorld, map);
    }

    public static void open(GameWorld gameWorld, SpleefMap map, SpleefConfig config) {
        SpleefActive active = new SpleefActive(gameWorld, map, config, gameWorld.getPlayers());

        gameWorld.newGame(game -> {
            game.setRule(GameRule.ALLOW_CRAFTING, RuleResult.DENY);
            game.setRule(GameRule.ALLOW_PORTALS, RuleResult.DENY);
            game.setRule(GameRule.ALLOW_PVP, RuleResult.DENY);
            game.setRule(GameRule.BLOCK_DROPS, RuleResult.DENY);
            game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
            game.setRule(GameRule.ENABLE_HUNGER, RuleResult.DENY);

            game.on(GameOpenListener.EVENT, active::onOpen);
            game.on(GameCloseListener.EVENT, active::onClose);

            game.on(OfferPlayerListener.EVENT, player -> JoinResult.ok());
            game.on(PlayerAddListener.EVENT, active::addPlayer);
            game.on(PlayerRemoveListener.EVENT, active::removePlayer);

            game.on(GameTickListener.EVENT, active::tick);

            game.on(PlayerDamageListener.EVENT, active::onPlayerDamage);
            game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
        });
    }

    private void onOpen() {
        for (ServerPlayerEntity participant : this.participants) {
            this.spawnParticipant(participant);
        }
    }

    private void onClose() {
        this.timerBar.close();
    }

    private void addPlayer(ServerPlayerEntity player) {
        if (!this.participants.contains(player)) {
            this.spawnSpectator(player);
        }
        this.timerBar.addPlayer(player);
    }

    private void removePlayer(ServerPlayerEntity player) {
        this.participants.remove(player);
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

            for (ServerPlayerEntity player : this.participants) {
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
            gameWorld.closeWorld();
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
        if (source == DamageSource.LAVA) {
            this.eliminatePlayer(player);
        }
        return true;
    }

    private boolean onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        this.eliminatePlayer(player);
        return true;
    }

    private void spawnParticipant(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(player);

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

        this.spawnSpectator(player);

        this.participants.remove(player);
    }

    private void spawnSpectator(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
        this.spawnLogic.spawnPlayer(player);
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

        for (ServerPlayerEntity player : this.participants) {
            // we still have more than one player remaining
            if (winningPlayer != null) {
                return WinResult.no();
            }

            winningPlayer = player;
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
