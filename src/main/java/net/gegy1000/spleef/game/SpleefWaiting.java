package net.gegy1000.spleef.game;

import net.gegy1000.plasmid.game.Game;
import net.gegy1000.plasmid.game.JoinResult;
import net.gegy1000.plasmid.game.StartResult;
import net.gegy1000.plasmid.game.config.PlayerConfig;
import net.gegy1000.plasmid.game.event.OfferPlayerListener;
import net.gegy1000.plasmid.game.event.PlayerAddListener;
import net.gegy1000.plasmid.game.event.PlayerDeathListener;
import net.gegy1000.plasmid.game.event.RequestStartListener;
import net.gegy1000.plasmid.game.map.GameMap;
import net.gegy1000.plasmid.game.rule.GameRule;
import net.gegy1000.plasmid.game.rule.RuleResult;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

public final class SpleefWaiting {
    private final GameMap map;
    private final SpleefConfig config;

    private final SpleefSpawnLogic spawnLogic;

    private SpleefWaiting(GameMap map, SpleefConfig config) {
        this.map = map;
        this.config = config;

        this.spawnLogic = new SpleefSpawnLogic(map);
    }

    public static Game open(GameMap map, SpleefConfig config) {
        SpleefWaiting waiting = new SpleefWaiting(map, config);

        Game.Builder builder = Game.builder();
        builder.setMap(map);

        builder.setRule(GameRule.ALLOW_CRAFTING, RuleResult.DENY);
        builder.setRule(GameRule.ALLOW_PORTALS, RuleResult.DENY);
        builder.setRule(GameRule.ALLOW_PVP, RuleResult.DENY);
        builder.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
        builder.setRule(GameRule.ENABLE_HUNGER, RuleResult.DENY);

        builder.on(RequestStartListener.EVENT, waiting::requestStart);
        builder.on(OfferPlayerListener.EVENT, waiting::offerPlayer);

        builder.on(PlayerAddListener.EVENT, waiting::addPlayer);
        builder.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);

        return builder.build();
    }

    private JoinResult offerPlayer(Game game, ServerPlayerEntity player) {
        if (game.getPlayerCount() >= this.config.getPlayerConfig().getMaxPlayers()) {
            return JoinResult.gameFull();
        }

        return JoinResult.ok();
    }

    private StartResult requestStart(Game game) {
        PlayerConfig playerConfig = this.config.getPlayerConfig();
        if (game.getPlayerCount() < playerConfig.getMinPlayers()) {
            return StartResult.notEnoughPlayers();
        }

        Game active = SpleefActive.open(this.map, this.config, game.getPlayers());
        return StartResult.ok(active);
    }

    private void addPlayer(Game game, ServerPlayerEntity player) {
        this.spawnPlayer(player);
    }

    private boolean onPlayerDeath(Game game, ServerPlayerEntity player, DamageSource source) {
        this.spawnPlayer(player);
        return true;
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(player);
    }
}
