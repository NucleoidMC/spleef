package xyz.nucleoid.spleef.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;
import xyz.nucleoid.spleef.game.map.shape.renderer.MapShapeRenderer;

public record SpleefMapConfig(
        int levels, int levelHeight,
        BlockStateProvider wallProvider,
        BlockStateProvider floorProvider,
        BlockStateProvider ceilingProvider,
        BlockStateProvider lavaProvider,
        MapShapeRenderer shape
) {
    public static final Codec<SpleefMapConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.INT.fieldOf("levels").forGetter(config -> config.levels),
                Codec.INT.fieldOf("level_height").forGetter(map -> map.levelHeight),
                BlockStateProvider.TYPE_CODEC.optionalFieldOf("wall_provider", new SimpleBlockStateProvider(Blocks.STONE_BRICKS.getDefaultState())).forGetter(SpleefMapConfig::wallProvider),
                BlockStateProvider.TYPE_CODEC.optionalFieldOf("floor_provider", new SimpleBlockStateProvider(Blocks.SNOW_BLOCK.getDefaultState())).forGetter(SpleefMapConfig::floorProvider),
                BlockStateProvider.TYPE_CODEC.optionalFieldOf("ceiling_provider", new SimpleBlockStateProvider(Blocks.BARRIER.getDefaultState())).forGetter(SpleefMapConfig::ceilingProvider),
                BlockStateProvider.TYPE_CODEC.optionalFieldOf("lava_provider", new SimpleBlockStateProvider(Blocks.LAVA.getDefaultState())).forGetter(SpleefMapConfig::lavaProvider),
                MapShapeRenderer.REGISTRY_CODEC.fieldOf("shape").forGetter(config -> config.shape)
        ).apply(instance, SpleefMapConfig::new);
    });
}
