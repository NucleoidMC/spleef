package xyz.nucleoid.spleef.game;

import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.spleef.game.map.SpleefMap;

public record ToolConfig(ItemStack stack, int recipients) {
    private static final ItemStack DEFAULT_STACK = new ItemStack(Items.DIAMOND_SHOVEL);
    private static final int DEFAULT_RECIPIENTS = -1;

    public static final ToolConfig DEFAULT = new ToolConfig(DEFAULT_STACK, DEFAULT_RECIPIENTS);

    private static final Codec<ToolConfig> RECORD_CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                ItemStack.CODEC.optionalFieldOf("stack", DEFAULT_STACK).forGetter(ToolConfig::stack),
                Codec.INT.optionalFieldOf("recipients", DEFAULT_RECIPIENTS).forGetter(ToolConfig::recipients)
        ).apply(instance, ToolConfig::new);
    });

    public static final Codec<ToolConfig> CODEC = Codec.either(ItemStack.CODEC, RECORD_CODEC).xmap(either -> {
        return either.map(stack -> {
            return new ToolConfig(stack, DEFAULT_RECIPIENTS);
        }, Function.identity());
    }, Either::right);

    public ItemStack createStack(int index, SpleefMap map) {
        if (this.recipients > DEFAULT_RECIPIENTS && index >= this.recipients) {
            return ItemStack.EMPTY;
        }

        var toolBuilder = ItemStackBuilder.of(this.stack())
                .setUnbreakable();

        // Avoid adding a duplicate enchantment
        if (EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, this.stack()) == 0) {
            toolBuilder.addEnchantment(Enchantments.EFFICIENCY, 2);
        }

        for (var state : map.providedFloors) {
            toolBuilder.addCanDestroy(state.getBlock());
        }

        return toolBuilder.build();
    }
}