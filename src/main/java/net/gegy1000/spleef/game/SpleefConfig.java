package net.gegy1000.spleef.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gegy1000.plasmid.game.config.GameConfig;
import net.gegy1000.plasmid.game.config.GameMapConfig;
import net.gegy1000.plasmid.game.config.PlayerConfig;

public final class SpleefConfig implements GameConfig {
    public static final Codec<SpleefConfig> CODEC = RecordCodecBuilder.create(instance -> {
        Codec<GameMapConfig<SpleefConfig>> mapCodec = GameMapConfig.codec();

        return instance.group(
                mapCodec.fieldOf("map").forGetter(SpleefConfig::getMapConfig),
                PlayerConfig.CODEC.fieldOf("players").forGetter(SpleefConfig::getPlayerConfig)
        ).apply(instance, SpleefConfig::new);
    });

    private final GameMapConfig<SpleefConfig> mapConfig;
    private final PlayerConfig playerConfig;

    public SpleefConfig(
            GameMapConfig<SpleefConfig> mapConfig,
            PlayerConfig playerConfig
    ) {
        this.mapConfig = mapConfig;
        this.playerConfig = playerConfig;
    }

    public GameMapConfig<SpleefConfig> getMapConfig() {
        return this.mapConfig;
    }

    public PlayerConfig getPlayerConfig() {
        return this.playerConfig;
    }
}
