package net.gegy1000.spleef.game.map;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.gegy1000.plasmid.game.map.template.MapTemplate;
import net.minecraft.block.BlockState;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

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

        Set<BlockState> providedFloors = new HashSet<>();
        SpleefMap map = new SpleefMap(template, providedFloors);

        Random random = new Random();

        this.addBase(template, random);
        this.addLevels(template, map, providedFloors, random);
        this.addWall(template, random);

        int offset = this.config.shape.getSpawnOffset();
        map.setSpawn(new BlockPos(offset, this.config.levels * this.config.levelHeight + 2, 0));

        return map;
    }

    private void addBase(MapTemplate template, Random random) {
        BlockStateProvider wallProvider = this.config.wallProvider;
        BlockStateProvider lavaProvider = this.config.lavaProvider;

        this.config.shape.generate(template, 0, 0, Brush.fill(wallProvider), random);
        this.config.shape.generate(template, 1, 1, new Brush(wallProvider, lavaProvider), random);
        this.config.shape.generate(template, 1, this.config.levelHeight + 1, Brush.outline(wallProvider), random);
    }

    private void addLevels(MapTemplate template, SpleefMap map, Set<BlockState> providedFloors, Random random) {
        Brush brush = new Brush(this.config.wallProvider, this.config.floorProvider, null, providedFloors);

        for (int level = 0; level < this.config.levels; level++) {
            int y = (level + 1) * this.config.levelHeight + 1;
            this.config.shape.generate(template, y, y, brush, random);
            map.addLevel(this.config.shape.getLevelBounds(y));
        }
    }

    private void addWall(MapTemplate template, Random random) {
        Brush wallBrush = Brush.outline(this.config.wallProvider);

        int minY = 1;
        int maxY = (this.config.levels + 1) * this.config.levelHeight;

        this.config.shape.generate(template, minY, maxY, wallBrush, random);
    }

    public static final class Brush {
        public final BlockStateProvider outlineProvider;
        public final BlockStateProvider fillProvider;

        public final Set<BlockState> outlineResults;
        public final Set<BlockState> fillResults;

        public Brush(BlockStateProvider outlineProvider, BlockStateProvider fillProvider, Set<BlockState> outlineResults, Set<BlockState> fillResults) {
            this.outlineProvider = outlineProvider;
            this.fillProvider = fillProvider;

            if (outlineProvider == null && fillProvider == null) {
                throw new IllegalArgumentException("null brush");
            }

            this.outlineResults = outlineResults;
            this.fillResults = fillResults;
        }

        public Brush(BlockStateProvider outlineProvider, BlockStateProvider fillProvider) {
            this(outlineProvider, fillProvider, null, null);
        }

        public static Brush outline(BlockStateProvider provider, Set<BlockState> results) {
            return new Brush(provider, null, results, null);
        }

        public static Brush outline(BlockStateProvider provider) {
            return Brush.outline(provider, null);
        }

        public static Brush fill(BlockStateProvider provider, Set<BlockState> results) {
            return new Brush(null, provider, null, results);
        }

        public static Brush fill(BlockStateProvider provider) {
            return Brush.fill(provider, null);
        }

        public BlockState provideOutline(Random random, BlockPos pos) {
            BlockState state = this.outlineProvider.getBlockState(random, pos);
            if (this.outlineResults != null) {
                this.outlineResults.add(state);
            }
            return state;
        }

        public BlockState provideFill(Random random, BlockPos pos) {
            BlockState state = this.fillProvider.getBlockState(random, pos);
            if (this.fillResults != null) {
                this.fillResults.add(state);
            }
            return state;
        }
    }
}
