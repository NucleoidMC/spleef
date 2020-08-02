package net.gegy1000.spleef.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gegy1000.plasmid.game.map.GameMap;
import net.gegy1000.plasmid.game.map.GameMapBuilder;
import net.gegy1000.plasmid.game.map.provider.MapProvider;
import net.gegy1000.plasmid.world.BlockBounds;
import net.gegy1000.spleef.game.SpleefConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.CompletableFuture;

public final class SpleefMapProvider implements MapProvider<SpleefConfig> {
    public static final Codec<SpleefMapProvider> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.INT.fieldOf("radius").forGetter(map -> map.radius),
                Codec.INT.fieldOf("levels").forGetter(map -> map.levels),
                Codec.INT.fieldOf("level_height").forGetter(map -> map.levelHeight)
        ).apply(instance, SpleefMapProvider::new);
    });

    private final int radius;
    private final int levels;
    private final int levelHeight;

    public SpleefMapProvider(int radius, int levels, int levelHeight) {
        this.radius = radius;
        this.levels = levels;
        this.levelHeight = levelHeight;
    }

    @Override
    public CompletableFuture<GameMap> createAt(ServerWorld world, BlockPos origin, SpleefConfig config) {
        int maxRadius = this.radius + 10;
        int maxHeight = (this.levels + 1) * this.levelHeight + 10;

        BlockBounds bounds = new BlockBounds(
                new BlockPos(-maxRadius, 0, -maxRadius),
                new BlockPos(maxRadius, maxHeight, maxRadius)
        );

        GameMapBuilder builder = GameMapBuilder.open(world, origin, bounds);

        return CompletableFuture.supplyAsync(() -> {
            this.buildMap(builder, config);
            return builder.build();
        }, world.getServer());
    }

    private void buildMap(GameMapBuilder builder, SpleefConfig config) {
        this.addBase(builder, config);
        this.addLevels(builder, config);
        this.addWall(builder, config);

        BlockPos spawnPos = new BlockPos(0, this.levels * this.levelHeight + 2, 0);
        builder.addRegion("spawn", new BlockBounds(spawnPos));
    }

    private void addBase(GameMapBuilder builder, SpleefConfig config) {
        BlockState wall = config.getWall();
        BlockState lava = Blocks.LAVA.getDefaultState();

        this.addCircle(builder, this.radius, 0, 0, Brush.fill(wall));
        this.addCircle(builder, this.radius, 1, 1, new Brush(wall, lava));
        this.addCircle(builder, this.radius, 1, this.levelHeight + 1, Brush.outline(wall));
    }

    private void addLevels(GameMapBuilder builder, SpleefConfig config) {
        int radius = this.radius;
        Brush brush = new Brush(config.getWall(), config.getFloor());

        for (int level = 0; level < this.levels; level++) {
            int y = (level + 1) * this.levelHeight + 1;
            this.addCircle(builder, radius, y, y, brush);

            builder.addRegion("level", new BlockBounds(
                    new BlockPos(-radius, y, -radius),
                    new BlockPos(radius, y, radius)
            ));
        }
    }

    private void addWall(GameMapBuilder builder, SpleefConfig config) {
        Brush wallBrush = Brush.outline(config.getWall());
        this.addCircle(builder, this.radius, 1, (this.levels + 1) * this.levelHeight, wallBrush);
    }

    private void addCircle(GameMapBuilder builder, int radius, int minY, int maxY, Brush brush) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        int radius2 = radius * radius;
        int outlineRadius2 = (radius - 1) * (radius - 1);

        BlockState outline = brush.outline;
        BlockState fill = brush.fill;

        for (int z = -radius; z <= radius; z++) {
            for (int x = -radius; x <= radius; x++) {
                int distance2 = x * x + z * z;
                if (distance2 >= radius2) {
                    continue;
                }

                mutablePos.set(x, 0, z);

                if (distance2 >= outlineRadius2 && outline != null) {
                    for (int y = minY; y <= maxY; y++) {
                        mutablePos.setY(y);
                        builder.setBlockState(mutablePos, outline);
                    }
                } else if (fill != null) {
                    for (int y = minY; y <= maxY; y++) {
                        mutablePos.setY(y);
                        builder.setBlockState(mutablePos, fill);
                    }
                }
            }
        }
    }

    @Override
    public Codec<? extends MapProvider<?>> getCodec() {
        return CODEC;
    }

    static final class Brush {
        final BlockState outline;
        final BlockState fill;

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
