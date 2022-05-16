package xyz.nucleoid.spleef.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record LavaRiseConfig(int ticksPerLevel, Optional<Integer> maximumHeight) {
    public static final Codec<LavaRiseConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.INT.fieldOf("ticks_per_level").forGetter(LavaRiseConfig::ticksPerLevel),
                Codec.INT.optionalFieldOf("maximum_height").forGetter(LavaRiseConfig::maximumHeight)
        ).apply(instance, LavaRiseConfig::new);
    });
}
