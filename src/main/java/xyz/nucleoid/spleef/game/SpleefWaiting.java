package xyz.nucleoid.spleef.game;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.event.OfferPlayerListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.game.world.bubble.BubbleWorldConfig;
import xyz.nucleoid.spleef.game.map.SpleefMap;
import xyz.nucleoid.spleef.game.map.SpleefMapGenerator;

import java.util.concurrent.CompletableFuture;

public final class SpleefWaiting {
    private final GameWorld gameWorld;
    private final SpleefMap map;
    private final SpleefConfig config;

    private final SpleefSpawnLogic spawnLogic;

    private SpleefWaiting(GameWorld gameWorld, SpleefMap map, SpleefConfig config) {
        this.gameWorld = gameWorld;
        this.map = map;
        this.config = config;

        this.spawnLogic = new SpleefSpawnLogic(gameWorld, map);
    }

    public static CompletableFuture<Void> open(MinecraftServer server, SpleefConfig config) {
        SpleefMapGenerator generator = new SpleefMapGenerator(config.map);

        return generator.create().thenAccept(map -> {
            BubbleWorldConfig worldConfig = new BubbleWorldConfig()
                    .setGenerator(map.asGenerator())
                    .setDefaultGameMode(GameMode.SPECTATOR);

            GameWorld gameWorld = GameWorld.open(server, worldConfig);

            SpleefWaiting waiting = new SpleefWaiting(gameWorld, map, config);

            gameWorld.newGame(game -> {
                game.setRule(GameRule.ALLOW_CRAFTING, RuleResult.DENY);
                game.setRule(GameRule.ALLOW_PORTALS, RuleResult.DENY);
                game.setRule(GameRule.ALLOW_PVP, RuleResult.DENY);
                game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
                game.setRule(GameRule.ENABLE_HUNGER, RuleResult.DENY);
                game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);
                game.setRule(GameRule.INTERACT, RuleResult.DENY);

                game.on(RequestStartListener.EVENT, waiting::requestStart);
                game.on(OfferPlayerListener.EVENT, waiting::offerPlayer);

                game.on(PlayerAddListener.EVENT, waiting::addPlayer);
                game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
            });
        });
    }

    private JoinResult offerPlayer(ServerPlayerEntity player) {
        if (this.gameWorld.getPlayerCount() >= this.config.players.getMaxPlayers()) {
            return JoinResult.gameFull();
        }

        return JoinResult.ok();
    }

    private StartResult requestStart() {
        if (this.gameWorld.getPlayerCount() < this.config.players.getMinPlayers()) {
            return StartResult.notEnoughPlayers();
        }

        SpleefActive.open(this.gameWorld, this.map, this.config);

        return StartResult.ok();
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnLogic.spawnPlayer(player, GameMode.ADVENTURE);
    }

    private boolean onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        this.spawnLogic.spawnPlayer(player, GameMode.ADVENTURE);
        return true;
    }
}
