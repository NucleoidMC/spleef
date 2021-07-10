package xyz.nucleoid.spleef.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public record ProjectileConfig(ItemStack stack, int restockInterval, int maximum, int radius, int innerRadius) {
    public static final Codec<ProjectileConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                ItemStack.CODEC.optionalFieldOf("stack", new ItemStack(Items.SNOWBALL)).forGetter(ProjectileConfig::stack),
                Codec.INT.optionalFieldOf("restock_interval", 6 * 20).forGetter(ProjectileConfig::restockInterval),
                Codec.INT.optionalFieldOf("maximum", 5).forGetter(ProjectileConfig::maximum),
                Codec.INT.optionalFieldOf("radius", 1).forGetter(ProjectileConfig::radius),
                Codec.INT.optionalFieldOf("inner_radius", 0).forGetter(ProjectileConfig::innerRadius)
        ).apply(instance, ProjectileConfig::new);
    });
}
