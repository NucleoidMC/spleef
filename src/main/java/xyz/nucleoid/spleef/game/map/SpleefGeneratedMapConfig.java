package xyz.nucleoid.spleef.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import xyz.nucleoid.codecs.MoreCodecs;
import xyz.nucleoid.spleef.game.map.shape.renderer.MapShapeRenderer;

public record SpleefGeneratedMapConfig(
        int levels, int levelHeight,
        BlockStateProvider wallProvider,
        BlockStateProvider floorProvider,
        BlockStateProvider ceilingProvider,
        MapShapeRenderer shape,
        BlockStateProvider lavaProvider
) {
    public static final Codec<SpleefGeneratedMapConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("levels").forGetter(SpleefGeneratedMapConfig::levels),
            Codec.INT.fieldOf("level_height").forGetter(SpleefGeneratedMapConfig::levelHeight),
            MoreCodecs.BLOCK_STATE_PROVIDER.optionalFieldOf("wall_provider", BlockStateProvider.of(Blocks.STONE_BRICKS)).forGetter(SpleefGeneratedMapConfig::wallProvider),
            MoreCodecs.BLOCK_STATE_PROVIDER.optionalFieldOf("floor_provider", BlockStateProvider.of(Blocks.SNOW_BLOCK)).forGetter(SpleefGeneratedMapConfig::floorProvider),
            MoreCodecs.BLOCK_STATE_PROVIDER.optionalFieldOf("ceiling_provider", BlockStateProvider.of(Blocks.BARRIER)).forGetter(SpleefGeneratedMapConfig::ceilingProvider),
            MapShapeRenderer.REGISTRY_CODEC.fieldOf("shape").forGetter(SpleefGeneratedMapConfig::shape),
            MoreCodecs.BLOCK_STATE_PROVIDER.optionalFieldOf("lava_provider", BlockStateProvider.of(Blocks.LAVA)).forGetter(SpleefGeneratedMapConfig::lavaProvider)
    ).apply(i, SpleefGeneratedMapConfig::new));
}
