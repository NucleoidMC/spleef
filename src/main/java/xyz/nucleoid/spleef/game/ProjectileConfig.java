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
                Codec.INT.optionalFieldOf("maximum", 5).forGetter(ProjectileConfig::getMaximum),
                Codec.INT.optionalFieldOf("radius", 1).forGetter(ProjectileConfig::getRadius),
                Codec.INT.optionalFieldOf("inner_radius", 0).forGetter(ProjectileConfig::getInnerRadius)
        ).apply(instance, ProjectileConfig::new);
    });

    private final boolean enabled;
    private final ItemStack stack;
    private final int restockInterval;
    private final int maximum;
    private final int radius;
    private final int innerRadius;

    public ProjectileConfig(boolean enabled, ItemStack stack, int restockInterval, int maximum, int radius, int innerRadius) {
        this.enabled = enabled;
        this.stack = stack;
        this.restockInterval = restockInterval;
        this.maximum = maximum;
        this.radius = radius;
        this.innerRadius = innerRadius;
    }
    
    public ProjectileConfig() {
        this(false, ItemStack.EMPTY, 0, 0, 0, 0);
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
    }
    
    public int getRadius() {
        return this.radius;
    }
    
    public int getInnerRadius() {
        return this.innerRadius;
    }
}