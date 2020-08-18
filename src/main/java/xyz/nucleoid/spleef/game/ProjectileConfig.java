package xyz.nucleoid.spleef.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ProjectileConfig {
    public static final Codec<ProjectileConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.BOOL.optionalFieldOf("enabled", false).forGetter(ProjectileConfig::isEnabled),
                ItemStack.CODEC.optionalFieldOf("stack", new ItemStack(Items.SNOWBALL)).forGetter(ProjectileConfig::getStack),
                Codec.INT.optionalFieldOf("restock_interval", 6 * 20).forGetter(ProjectileConfig::getRestockInterval),
                Codec.INT.optionalFieldOf("maximum", 5).forGetter(ProjectileConfig::getMaximum)
        ).apply(instance, ProjectileConfig::new);
    });

    private final boolean enabled;
    private final ItemStack stack;
    private final int restockInterval;
    private final int maximum;

    public ProjectileConfig(boolean enabled, ItemStack stack, int restockInterval, int maximum) {
        this.enabled = enabled;
        this.stack = stack;
        this.restockInterval = restockInterval;
        this.maximum = maximum;
    }
    
    public ProjectileConfig() {
        this(false, ItemStack.EMPTY, 0, 0);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public int getRestockInterval() {
        return this.restockInterval;
    }

    public int getMaximum() {
        return this.maximum;
    };
}