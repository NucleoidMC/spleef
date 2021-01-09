package xyz.nucleoid.spleef.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class LavaRiseConfig {
    public static final Codec<LavaRiseConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.INT.fieldOf("ticks_per_level").forGetter(LavaRiseConfig::getTicksPerLevel),
                Codec.INT.fieldOf("maximum_height").forGetter(LavaRiseConfig::getMaximumHeight)
        ).apply(instance, LavaRiseConfig::new);
    });

    private final int ticksPerLevel;
    private final int maximumHeight;

    public LavaRiseConfig(int ticksPerLevel, int maximumHeight) {
        this.ticksPerLevel = ticksPerLevel;
        this.maximumHeight = maximumHeight;
    }

    public int getTicksPerLevel() {
        return this.ticksPerLevel;
    }

    public int getMaximumHeight() {
        return this.maximumHeight;
    }
}
