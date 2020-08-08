package net.gegy1000.spleef.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gegy1000.plasmid.game.config.GameConfig;
import net.gegy1000.plasmid.game.config.PlayerConfig;
import net.gegy1000.spleef.game.map.SpleefMapConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class SpleefConfig implements GameConfig {
    public static final Codec<SpleefConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                SpleefMapConfig.CODEC.fieldOf("map").forGetter(config -> config.map),
                PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.players),
                ItemStack.CODEC.optionalFieldOf("tool", new ItemStack(Items.DIAMOND_SHOVEL)).forGetter(config -> config.tool),
                Codec.LONG.optionalFieldOf("level_break_interval", 20L * 60).forGetter(config -> config.levelBreakInterval),
                Codec.INT.optionalFieldOf("decay", -1).forGetter(config -> config.decay)
        ).apply(instance, SpleefConfig::new);
    });

    public final SpleefMapConfig map;
    public final PlayerConfig players;

    public final ItemStack tool;

    public final long levelBreakInterval;
    public final int decay;

    public SpleefConfig(
            SpleefMapConfig map,
            PlayerConfig players,
            ItemStack tool,
            long levelBreakInterval,
            int decay
    ) {
        this.map = map;
        this.players = players;
        this.tool = tool;
        this.levelBreakInterval = levelBreakInterval;
        this.decay = decay;
    }
}
