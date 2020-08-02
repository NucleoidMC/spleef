package net.gegy1000.spleef.game;

import net.gegy1000.plasmid.game.map.GameMap;
import net.gegy1000.plasmid.world.BlockBounds;
import net.gegy1000.spleef.Spleef;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public final class SpleefSpawnLogic {
    private final GameMap map;

    public SpleefSpawnLogic(GameMap map) {
        this.map = map;
    }

    public void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.inventory.clear();
        player.getEnderChestInventory().clear();
        player.clearStatusEffects();
        player.setHealth(20.0F);
        player.getHungerManager().setFoodLevel(20);
        player.fallDistance = 0.0F;
        player.setGameMode(gameMode);

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION,
                20 * 60 * 60,
                1,
                true,
                false
        ));
    }

    public void spawnPlayer(ServerPlayerEntity player) {
        ServerWorld world = this.map.getWorld();

        BlockBounds spawn = this.map.getFirstRegion("spawn");
        if (spawn == null) {
            Spleef.LOGGER.warn("Cannot spawn player! No spawn is defined in the map!");
            return;
        }

        BlockPos pos = new BlockPos(spawn.getCenter());
        player.teleport(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0F, 0.0F);
    }
}
