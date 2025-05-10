package xyz.nucleoid.spleef.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public record AttributeModifiersConfig(List<Entry> entries) {
    public static final AttributeModifiersConfig EMPTY = new AttributeModifiersConfig(List.of());
    public static final Codec<AttributeModifiersConfig> CODEC = Entry.CODEC.listOf().xmap(AttributeModifiersConfig::new, AttributeModifiersConfig::entries);

    public void applyTo(ServerPlayerEntity player) {
        for (var entry : this.entries()) {
            var instance = player.getAttributeInstance(entry.attribute());

            if (instance != null) {
                instance.removeModifier(entry.modifier().id());
                instance.addTemporaryModifier(entry.modifier());
            }
        }
    }

    private record Entry(RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    EntityAttribute.CODEC.fieldOf("type").forGetter(Entry::attribute),
                    EntityAttributeModifier.MAP_CODEC.forGetter(Entry::modifier)
            ).apply(instance, Entry::new);
        });
    }
}
