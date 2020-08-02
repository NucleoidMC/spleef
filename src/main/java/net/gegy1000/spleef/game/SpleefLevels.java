package net.gegy1000.spleef.game;

import net.gegy1000.plasmid.game.map.GameMap;
import net.gegy1000.plasmid.world.BlockBounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class SpleefLevels {
    private final GameMap map;
    private final List<BlockBounds> levels;
    private int topLevel;

    private SpleefLevels(GameMap map, List<BlockBounds> levels) {
        this.map = map;
        this.levels = levels;
        this.topLevel = levels.size() - 1;
    }

    public static SpleefLevels create(GameMap map) {
        List<BlockBounds> levels = map.getRegions("level")
                .sorted(Comparator.comparingInt(level -> level.getMax().getY()))
                .collect(Collectors.toList());

        return new SpleefLevels(map, levels);
    }

    public void tryDropLevel(SpleefConfig config) {
        if (this.topLevel < 0) {
            return;
        }

        BlockBounds level = this.levels.get(this.topLevel);
        this.deleteLevel(config, level);

        this.topLevel--;
    }

    private void deleteLevel(SpleefConfig config, BlockBounds level) {
        ServerWorld world = this.map.getWorld();

        for (BlockPos pos : level.iterate()) {
            BlockState state = world.getBlockState(pos);
            if (state == config.getFloor()) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }
    }
}
