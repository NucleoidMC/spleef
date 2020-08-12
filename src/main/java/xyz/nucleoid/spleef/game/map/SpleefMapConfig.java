package xyz.nucleoid.spleef.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import xyz.nucleoid.spleef.game.map.shape.MapShape;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;

public final class SpleefMapConfig {
    public static final Codec<SpleefMapConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.INT.fieldOf("levels").forGetter(config -> config.levels),
                Codec.INT.fieldOf("level_height").forGetter(map -> map.levelHeight),
                BlockStateProvider.CODEC.fieldOf("wall_provider").orElse(new SimpleBlockStateProvider(Blocks.STONE_BRICKS.getDefaultState())).forGetter(config -> config.wallProvider),
                BlockStateProvider.CODEC.fieldOf("floor_provider").orElse(new SimpleBlockStateProvider(Blocks.SNOW_BLOCK.getDefaultState())).forGetter(config -> config.floorProvider),
                BlockStateProvider.CODEC.fieldOf("lava_provider").orElse(new SimpleBlockStateProvider(Blocks.LAVA.getDefaultState())).forGetter(config -> config.lavaProvider),
                MapShape.REGISTRY_CODEC.fieldOf("shape").forGetter(config -> config.shape)
        ).apply(instance, SpleefMapConfig::new);
    });

    public final int levels;
    public final int levelHeight;
    public final BlockStateProvider wallProvider;
    public final BlockStateProvider floorProvider;
    public final BlockStateProvider lavaProvider;
    public final MapShape shape;

    public SpleefMapConfig(int levels, int levelHeight, BlockStateProvider wallProvider, BlockStateProvider floorProvider, BlockStateProvider lavaProvider, MapShape shape) {
        this.levels = levels;
        this.levelHeight = levelHeight;
        this.wallProvider = wallProvider;
        this.floorProvider = floorProvider;
        this.lavaProvider = lavaProvider;
		this.shape = shape;
    }
}
