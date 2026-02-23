package xyz.nucleoid.spleef.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import xyz.nucleoid.codecs.MoreCodecs;

import java.util.Optional;

public record SpleefTemplateMapConfig(
        Identifier template,
        String spawnRegion,
        Levels levels,
        Optional<Lava> lava
) {
    public static final Codec<SpleefTemplateMapConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
        Identifier.CODEC.fieldOf("template").forGetter(SpleefTemplateMapConfig::template),
        Codec.STRING.fieldOf("spawn_region").forGetter(SpleefTemplateMapConfig::spawnRegion),
        Levels.CODEC.fieldOf("levels").forGetter(SpleefTemplateMapConfig::levels),
        Lava.CODEC.optionalFieldOf("lava").forGetter(SpleefTemplateMapConfig::lava)
    ).apply(i, SpleefTemplateMapConfig::new));

    public record Levels(String region, Block block) {
        public static final Codec<Levels> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("region").forGetter(Levels::region),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(Levels::block)
        ).apply(i, Levels::new));
    }

    public record Lava(int startY, BlockStateProvider provider) {
        public static final Codec<Lava> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("start_y").forGetter(Lava::startY),
            MoreCodecs.BLOCK_STATE_PROVIDER.optionalFieldOf("lava_provider", BlockStateProvider.simple(Blocks.LAVA)).forGetter(Lava::provider)
        ).apply(i, Lava::new));
    }
}
