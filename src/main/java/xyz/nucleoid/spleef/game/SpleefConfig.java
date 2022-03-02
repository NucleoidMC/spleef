package xyz.nucleoid.spleef.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;
import xyz.nucleoid.spleef.game.map.SpleefMapConfig;

import java.util.Optional;

public final record SpleefConfig(
        SpleefMapConfig map,
        PlayerConfig players,
        ToolConfig tool,
        @Nullable ProjectileConfig projectile,
        @Nullable LavaRiseConfig lavaRise,
        long levelBreakInterval,
        int decay,
        int timeOfDay,
        boolean unstableTnt
) {
    public static final Codec<SpleefConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                SpleefMapConfig.CODEC.fieldOf("map").forGetter(SpleefConfig::map),
                PlayerConfig.CODEC.fieldOf("players").forGetter(SpleefConfig::players),
                ToolConfig.CODEC.optionalFieldOf("tool", ToolConfig.DEFAULT).forGetter(SpleefConfig::tool),
                ProjectileConfig.CODEC.optionalFieldOf("projectile").forGetter(config -> Optional.ofNullable(config.projectile())),
                LavaRiseConfig.CODEC.optionalFieldOf("lava_rise").forGetter(config -> Optional.ofNullable(config.lavaRise())),
                Codec.LONG.optionalFieldOf("level_break_interval", 20L * 60).forGetter(SpleefConfig::levelBreakInterval),
                Codec.INT.optionalFieldOf("decay", -1).forGetter(SpleefConfig::decay),
                Codec.INT.optionalFieldOf("time_of_day", 6000).forGetter(SpleefConfig::timeOfDay),
                Codec.BOOL.optionalFieldOf("unstable_tnt", false).forGetter(SpleefConfig::unstableTnt)
        ).apply(instance, SpleefConfig::new);
    });

    private SpleefConfig(
            SpleefMapConfig map,
            PlayerConfig players,
            ToolConfig tool,
            Optional<ProjectileConfig> projectile,
            Optional<LavaRiseConfig> lavaRise,
            long levelBreakInterval,
            int decay,
            int timeOfDay,
            boolean unstableTnt
    ) {
        this(
                map, players, tool,
                projectile.orElse(null),
                lavaRise.orElse(null),
                levelBreakInterval, decay,
                timeOfDay,
                unstableTnt
        );
    }
}
