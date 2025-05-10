package xyz.nucleoid.spleef.game;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.spleef.game.map.SpleefMap;
import xyz.nucleoid.spleef.game.map.SpleefMapGenerator;
import xyz.nucleoid.spleef.game.map.SpleefTemplateMapBuilder;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public final class SpleefWaiting {
    private final GameSpace gameSpace;
    private final ServerWorld world;
    private final SpleefMap map;
    private final SpleefConfig config;

    private SpleefWaiting(GameSpace gameSpace, ServerWorld world, SpleefMap map, SpleefConfig config) {
        this.gameSpace = gameSpace;
        this.world = world;
        this.map = map;
        this.config = config;
    }

    public static GameOpenProcedure open(GameOpenContext<SpleefConfig> context) {
        var config = context.config();
        var map = config.map().map(
            generatedConfig -> {
                var generator = new SpleefMapGenerator(generatedConfig);
                return generator.build();
            },
            templateConfig -> {
                var builder = new SpleefTemplateMapBuilder(templateConfig);
                return builder.build(context.server());
            }
        );

        var worldConfig = new RuntimeWorldConfig()
                .setGameRule(GameRules.LAVA_SOURCE_CONVERSION, true)
                .setGenerator(map.asGenerator(context.server()))
                .setTimeOfDay(config.timeOfDay());

        return context.openWithWorld(worldConfig, (game, world) -> {
            GameWaitingLobby.addTo(game, config.players());

            var waiting = new SpleefWaiting(game.getGameSpace(), world, map, config);

            game.deny(GameRuleType.CRAFTING);
            game.deny(GameRuleType.PORTALS);
            game.deny(GameRuleType.PVP);
            game.deny(GameRuleType.FALL_DAMAGE);
            game.deny(GameRuleType.HUNGER);
            game.deny(GameRuleType.THROW_ITEMS);
            game.deny(GameRuleType.INTERACTION);

            game.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);

            game.listen(GamePlayerEvents.ACCEPT, waiting::acceptPlayer);
            game.listen(PlayerDamageEvent.EVENT, (player, source, amount) -> EventResult.DENY);
            game.listen(PlayerDeathEvent.EVENT, (player, source) -> EventResult.DENY);
        });
    }

    private GameResult requestStart() {
        SpleefActive.open(this.gameSpace, this.world, this.map, this.config);
        return GameResult.ok();
    }

    private JoinAcceptorResult acceptPlayer(JoinAcceptor offer) {
        var spawn = this.map.getSpawn();
        return offer.teleport(this.world, Vec3d.ofCenter(spawn))
                .thenRunForEach(player -> {
                    player.changeGameMode(GameMode.ADVENTURE);
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.NIGHT_VISION,
                            StatusEffectInstance.INFINITE,
                            1,
                            true,
                            false
                    ));

                    this.config.attributeModifiers().applyTo(player);
                });
    }
}
