package xyz.nucleoid.spleef.game;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;
import xyz.nucleoid.spleef.game.map.SpleefGeneratedMapConfig;
import xyz.nucleoid.spleef.game.map.SpleefTemplateMapConfig;

import java.util.Optional;

public record SpleefConfig(
        Either<SpleefGeneratedMapConfig, SpleefTemplateMapConfig> map,
        WaitingLobbyConfig players,
        AttributeModifiersConfig attributeModifiers,
        ToolConfig tool,
        @Nullable ProjectileConfig projectile,
        @Nullable LavaRiseConfig lavaRise,
        long levelBreakInterval,
        int decay,
        int timeOfDay,
        boolean unstableTnt
) {
    public static final MapCodec<SpleefConfig> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.xor(SpleefGeneratedMapConfig.CODEC, SpleefTemplateMapConfig.CODEC).fieldOf("map").forGetter(SpleefConfig::map),
        WaitingLobbyConfig.CODEC.fieldOf("players").forGetter(SpleefConfig::players),
        AttributeModifiersConfig.CODEC.optionalFieldOf("attribute_modifiers", AttributeModifiersConfig.EMPTY).forGetter(SpleefConfig::attributeModifiers),
        ToolConfig.CODEC.optionalFieldOf("tool", ToolConfig.DEFAULT).forGetter(SpleefConfig::tool),
        ProjectileConfig.CODEC.optionalFieldOf("projectile").forGetter(config -> Optional.ofNullable(config.projectile())),
        LavaRiseConfig.CODEC.optionalFieldOf("lava_rise").forGetter(config -> Optional.ofNullable(config.lavaRise())),
        Codec.LONG.optionalFieldOf("level_break_interval", 20L * 60).forGetter(SpleefConfig::levelBreakInterval),
        Codec.INT.optionalFieldOf("decay", -1).forGetter(SpleefConfig::decay),
        Codec.INT.optionalFieldOf("time_of_day", 6000).forGetter(SpleefConfig::timeOfDay),
        Codec.BOOL.optionalFieldOf("unstable_tnt", false).forGetter(SpleefConfig::unstableTnt)
    ).apply(i, SpleefConfig::new));

    private SpleefConfig(
            Either<SpleefGeneratedMapConfig, SpleefTemplateMapConfig> map,
            WaitingLobbyConfig players,
            AttributeModifiersConfig attributeModifiers,
            ToolConfig tool,
            Optional<ProjectileConfig> projectile,
            Optional<LavaRiseConfig> lavaRise,
            long levelBreakInterval,
            int decay,
            int timeOfDay,
            boolean unstableTnt
    ) {
        this(
                map, players, attributeModifiers, tool,
                projectile.orElse(null),
                lavaRise.orElse(null),
                levelBreakInterval, decay,
                timeOfDay,
                unstableTnt
        );
    }
}
