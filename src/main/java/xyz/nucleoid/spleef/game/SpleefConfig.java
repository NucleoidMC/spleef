package xyz.nucleoid.spleef.game;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import xyz.nucleoid.spleef.game.map.SpleefMapConfig;

public final class SpleefConfig {
    public static final Codec<SpleefConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                SpleefMapConfig.CODEC.fieldOf("map").forGetter(config -> config.map),
                PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.players),
                ItemStack.CODEC.optionalFieldOf("tool", new ItemStack(Items.DIAMOND_SHOVEL)).forGetter(config -> config.tool),
                ProjectileConfig.CODEC.optionalFieldOf("projectile").forGetter(config -> config.projectile),
                Codec.LONG.optionalFieldOf("level_break_interval", 20L * 60).forGetter(config -> config.levelBreakInterval),
                Codec.INT.optionalFieldOf("decay", -1).forGetter(config -> config.decay),
                Codec.INT.optionalFieldOf("time_of_day", 6000).forGetter(config -> config.timeOfDay),
                Codec.BOOL.optionalFieldOf("unstable_tnt", false).forGetter(config -> config.unstableTnt)
        ).apply(instance, SpleefConfig::new);
    });

    public final SpleefMapConfig map;
    public final PlayerConfig players;

    public final ItemStack tool;

    public final Optional<ProjectileConfig> projectile;

    public final long levelBreakInterval;
    public final int decay;

    public final int timeOfDay;

    public final boolean unstableTnt;

    public SpleefConfig(
            SpleefMapConfig map,
            PlayerConfig players,
            ItemStack tool,
            Optional<ProjectileConfig> projectile,
            long levelBreakInterval,
            int decay,
            int timeOfDay,
            boolean unstableTnt
    ) {
        this.map = map;
        this.players = players;
        this.tool = tool;
        this.projectile = projectile;
        this.levelBreakInterval = levelBreakInterval;
        this.decay = decay;
        this.timeOfDay = timeOfDay;
        this.unstableTnt = unstableTnt;
    }
}
