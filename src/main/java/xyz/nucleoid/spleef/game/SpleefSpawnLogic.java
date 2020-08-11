package xyz.nucleoid.spleef.game;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.spleef.Spleef;
import xyz.nucleoid.spleef.game.map.SpleefMap;

public final class SpleefSpawnLogic {
    private final GameWorld gameWorld;
    private final SpleefMap map;

    public SpleefSpawnLogic(GameWorld gameWorld, SpleefMap map) {
        this.gameWorld = gameWorld;
        this.map = map;
    }

    public void spawnPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.setGameMode(gameMode);

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION,
                20 * 60 * 60,
                1,
                true,
                false
        ));

        ServerWorld world = this.gameWorld.getWorld();

        BlockPos pos = this.map.getSpawn();
        if (pos == null) {
            Spleef.LOGGER.warn("Cannot spawn player! No spawn is defined in the map!");
            return;
        }

        player.teleport(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0F, 0.0F);
    }
}
