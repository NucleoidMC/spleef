package xyz.nucleoid.spleef.game;

import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockPredicatesComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.property.Property;
import xyz.nucleoid.plasmid.api.util.ItemStackBuilder;
import xyz.nucleoid.spleef.game.map.SpleefMap;

public record ToolConfig(ItemStack stack, int recipients) {
    private static final ItemStack DEFAULT_STACK = new ItemStack(Items.DIAMOND_SHOVEL);
    private static final int DEFAULT_RECIPIENTS = -1;

    public static final ToolConfig DEFAULT = new ToolConfig(DEFAULT_STACK, DEFAULT_RECIPIENTS);

    private static final Codec<ToolConfig> RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.optionalFieldOf("stack", DEFAULT_STACK).forGetter(ToolConfig::stack),
            Codec.INT.optionalFieldOf("recipients", DEFAULT_RECIPIENTS).forGetter(ToolConfig::recipients)
    ).apply(instance, ToolConfig::new));

    public static final Codec<ToolConfig> CODEC = Codec.either(ItemStack.CODEC, RECORD_CODEC).xmap(either -> either.map(stack -> new ToolConfig(stack, DEFAULT_RECIPIENTS), Function.identity()), Either::right);

    public boolean shouldReceiveTool(int index) {
        return this.recipients == DEFAULT_RECIPIENTS || index < this.recipients;
    }

    public ItemStack createStack(MinecraftServer server, SpleefMap map) {
        var toolBuilder = ItemStackBuilder.of(this.stack())
                .setUnbreakable();

        toolBuilder.addEnchantment(server, Enchantments.EFFICIENCY, 2);

        toolBuilder.set(DataComponentTypes.CAN_BREAK, new BlockPredicatesComponent(map.providedFloors.stream().map(x -> {
                var state = StatePredicate.Builder.create();

                for (var prop : x.getProperties()) {
                    state = state.exactMatch(prop, ((Property) prop).name(x.get(prop)));
                }
                return BlockPredicate.Builder.create().blocks(Registries.BLOCK, x.getBlock()).state(state).build();
        }).toList()));


        return toolBuilder.build();
    }
}