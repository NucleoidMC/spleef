package net.gegy1000.spleef.game.map;

import net.gegy1000.plasmid.game.map.template.MapTemplate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.CompletableFuture;

public final class SpleefMapGenerator {
    private final SpleefMapConfig config;

    public SpleefMapGenerator(SpleefMapConfig config) {
        this.config = config;
    }

    public CompletableFuture<SpleefMap> create() {
        return CompletableFuture.supplyAsync(this::build, Util.getServerWorkerExecutor());
    }

    private SpleefMap build() {
        MapTemplate template = MapTemplate.createEmpty();
        SpleefMap map = new SpleefMap(template, this.config);

        this.addBase(template);
        this.addLevels(template, map);
        this.addWall(template);

        int offset = this.config.shape.getSpawnOffset();
        map.setSpawn(new BlockPos(offset, this.config.levels * this.config.levelHeight + 2, 0));

        return map;
    }

    private void addBase(MapTemplate template) {
        BlockState wall = this.config.wall;
        BlockState lava = Blocks.LAVA.getDefaultState();

        this.config.shape.generate(template, 0, 0, Brush.fill(wall));
        this.config.shape.generate(template, 1, 1, new Brush(wall, lava));
        this.config.shape.generate(template, 1, this.config.levelHeight + 1, Brush.outline(wall));
    }

    private void addLevels(MapTemplate template, SpleefMap map) {
        Brush brush = new Brush(this.config.wall, this.config.floor);

        for (int level = 0; level < this.config.levels; level++) {
            int y = (level + 1) * this.config.levelHeight + 1;
            this.config.shape.generate(template, y, y, brush);
            map.addLevel(this.config.shape.getLevelBounds(y));
        }
    }

    private void addWall(MapTemplate template) {
        Brush wallBrush = Brush.outline(this.config.wall);

        int minY = 1;
        int maxY = (this.config.levels + 1) * this.config.levelHeight;

        this.config.shape.generate(template, minY, maxY, wallBrush);
    }

    public static final class Brush {
        public final BlockState outline;
        public final BlockState fill;

        public Brush(BlockState outline, BlockState fill) {
            this.outline = outline;
            this.fill = fill;

            if (outline == null && fill == null) {
                throw new IllegalArgumentException("null brush");
            }
        }

        public static Brush outline(BlockState block) {
            return new Brush(block, null);
        }

        public static Brush fill(BlockState block) {
            return new Brush(null, block);
        }
    }
}
