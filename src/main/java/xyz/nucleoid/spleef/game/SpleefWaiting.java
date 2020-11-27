package xyz.nucleoid.spleef.game;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDamageListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.spleef.Spleef;
import xyz.nucleoid.spleef.game.map.SpleefMap;
import xyz.nucleoid.spleef.game.map.SpleefMapGenerator;

public final class SpleefWaiting {
    private final GameSpace gameSpace;
    private final SpleefMap map;
    private final SpleefConfig config;

    private SpleefWaiting(GameSpace gameSpace, SpleefMap map, SpleefConfig config) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;
    }

    public static GameOpenProcedure open(GameOpenContext<SpleefConfig> context) {
        SpleefConfig config = context.getConfig();
        SpleefMapGenerator generator = new SpleefMapGenerator(config.map);
        SpleefMap map = generator.build();

        BubbleWorldConfig worldConfig = new BubbleWorldConfig()
                .setGenerator(map.asGenerator(context.getServer()))
                .setDefaultGameMode(GameMode.SPECTATOR)
                .setTimeOfDay(config.timeOfDay);

        return context.createOpenProcedure(worldConfig, game -> {
            GameWaitingLobby.applyTo(game, config.players);

            SpleefWaiting waiting = new SpleefWaiting(game.getSpace(), map, config);

            game.setRule(GameRule.CRAFTING, RuleResult.DENY);
            game.setRule(GameRule.PORTALS, RuleResult.DENY);
            game.setRule(GameRule.PVP, RuleResult.DENY);
            game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
            game.setRule(GameRule.HUNGER, RuleResult.DENY);
            game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);
            game.setRule(GameRule.INTERACTION, RuleResult.DENY);

            game.on(RequestStartListener.EVENT, waiting::requestStart);

            game.on(PlayerAddListener.EVENT, waiting::addPlayer);
            game.on(PlayerDamageListener.EVENT, waiting::onPlayerDamage);
            game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
        });
    }

    private StartResult requestStart() {
        SpleefActive.open(this.gameSpace, this.map, this.config);
        return StartResult.OK;
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnPlayer(player);
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        return ActionResult.FAIL;
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        this.spawnPlayer(player);
        return ActionResult.FAIL;
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION,
                20 * 60 * 60,
                1,
                true,
                false
        ));

        ServerWorld world = this.gameSpace.getWorld();

        BlockPos pos = this.map.getSpawn();
        if (pos == null) {
            Spleef.LOGGER.warn("Cannot spawn player! No spawn is defined in the map!");
            return;
        }

        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0F, 0.0F);
    }
}
