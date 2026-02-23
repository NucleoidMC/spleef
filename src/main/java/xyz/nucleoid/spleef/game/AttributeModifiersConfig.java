package xyz.nucleoid.spleef.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public record AttributeModifiersConfig(List<Entry> entries) {
    public static final AttributeModifiersConfig EMPTY = new AttributeModifiersConfig(List.of());
    public static final Codec<AttributeModifiersConfig> CODEC = Entry.CODEC.listOf().xmap(AttributeModifiersConfig::new, AttributeModifiersConfig::entries);

    public void applyTo(ServerPlayer player) {
        for (var entry : this.entries()) {
            var instance = player.getAttribute(entry.attribute());

            if (instance != null) {
                instance.removeModifier(entry.modifier().id());
                instance.addTransientModifier(entry.modifier());
            }
        }
    }

    private record Entry(Holder<Attribute> attribute, AttributeModifier modifier) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Attribute.CODEC.fieldOf("type").forGetter(Entry::attribute),
                AttributeModifier.MAP_CODEC.forGetter(Entry::modifier)
        ).apply(instance, Entry::new));
    }
}
