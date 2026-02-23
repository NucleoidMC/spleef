package xyz.nucleoid.spleef.game;

import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.state.properties.Property;
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

        toolBuilder.set(DataComponents.CAN_BREAK, new AdventureModePredicate(map.providedFloors.stream().map(x -> {
                var state = StatePropertiesPredicate.Builder.properties();

                for (var prop : x.getProperties()) {
                    state = state.hasProperty(prop, ((Property) prop).getName(x.getValue(prop)));
                }
                return net.minecraft.advancements.criterion.BlockPredicate.Builder.block().of(BuiltInRegistries.BLOCK, x.getBlock()).setProperties(state).build();
        }).toList()));


        return toolBuilder.build();
    }
}