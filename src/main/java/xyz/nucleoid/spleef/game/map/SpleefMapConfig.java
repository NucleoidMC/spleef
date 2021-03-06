package xyz.nucleoid.spleef.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import xyz.nucleoid.spleef.game.map.shape.renderer.MapShapeRenderer;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;

public final class SpleefMapConfig {
    public static final Codec<SpleefMapConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.INT.fieldOf("levels").forGetter(config -> config.levels),
                Codec.INT.fieldOf("level_height").forGetter(map -> map.levelHeight),
                BlockStateProvider.TYPE_CODEC.optionalFieldOf("wall_provider", new SimpleBlockStateProvider(Blocks.STONE_BRICKS.getDefaultState())).forGetter(config -> config.wallProvider),
                BlockStateProvider.TYPE_CODEC.optionalFieldOf("floor_provider", new SimpleBlockStateProvider(Blocks.SNOW_BLOCK.getDefaultState())).forGetter(config -> config.floorProvider),
                BlockStateProvider.TYPE_CODEC.optionalFieldOf("ceiling_provider", new SimpleBlockStateProvider(Blocks.BARRIER.getDefaultState())).forGetter(config -> config.ceilingProvider),
                BlockStateProvider.TYPE_CODEC.optionalFieldOf("lava_provider", new SimpleBlockStateProvider(Blocks.LAVA.getDefaultState())).forGetter(config -> config.lavaProvider),
                MapShapeRenderer.REGISTRY_CODEC.fieldOf("shape").forGetter(config -> config.shape)
        ).apply(instance, SpleefMapConfig::new);
    });

    public final int levels;
    public final int levelHeight;
    public final BlockStateProvider wallProvider;
    public final BlockStateProvider floorProvider;
    public final BlockStateProvider ceilingProvider;
    public final BlockStateProvider lavaProvider;
    public final MapShapeRenderer shape;

    public SpleefMapConfig(int levels, int levelHeight, BlockStateProvider wallProvider, BlockStateProvider floorProvider, BlockStateProvider ceilingProvider, BlockStateProvider lavaProvider, MapShapeRenderer shape) {
        this.levels = levels;
        this.levelHeight = levelHeight;
        this.wallProvider = wallProvider;
        this.floorProvider = floorProvider;
        this.ceilingProvider = ceilingProvider;
        this.lavaProvider = lavaProvider;
        this.shape = shape;
    }
}
