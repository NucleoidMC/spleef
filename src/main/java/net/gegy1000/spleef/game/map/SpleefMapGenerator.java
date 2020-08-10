package net.gegy1000.spleef.game.map;

import net.gegy1000.plasmid.game.map.template.MapTemplate;
import net.gegy1000.plasmid.util.BlockBounds;
import net.gegy1000.spleef.game.map.shape.CircleShape;
import net.gegy1000.spleef.game.map.shape.MapShape;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.CompletableFuture;

public final class SpleefMapGenerator {
    private final SpleefMapConfig config;
    private MapShape shape;

    public SpleefMapGenerator(SpleefMapConfig config) {
        this.config = config;

        try {
            this.shape = this.config.getShape().newInstance();
        } catch (Exception e) {
            this.shape = new CircleShape();
        }
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

        map.setSpawn(new BlockPos(0, this.config.levels * this.config.levelHeight + 2, 0));

        return map;
    }

    private void addBase(MapTemplate template) {
        int radius = this.config.radius;
        BlockState wall = this.config.wall;
        BlockState lava = Blocks.LAVA.getDefaultState();

        this.shape.generate(template, radius, 0, 0, Brush.fill(wall));
        this.shape.generate(template, radius, 1, 1, new Brush(wall, lava));
        this.shape.generate(template, radius, 1, this.config.levelHeight + 1, Brush.outline(wall));
    }

    private void addLevels(MapTemplate template, SpleefMap map) {
        int radius = this.config.radius;
        Brush brush = new Brush(this.config.wall, this.config.floor);

        for (int level = 0; level < this.config.levels; level++) {
            int y = (level + 1) * this.config.levelHeight + 1;
            this.shape.generate(template, radius, y, y, brush);

            map.addLevel(new BlockBounds(
                    new BlockPos(-radius, y, -radius),
                    new BlockPos(radius, y, radius)
            ));
        }
    }

    private void addWall(MapTemplate template) {
        Brush wallBrush = Brush.outline(this.config.wall);

        int radius = this.config.radius;
        int minY = 1;
        int maxY = (this.config.levels + 1) * this.config.levelHeight;

        this.shape.generate(template, radius, minY, maxY, wallBrush);
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
