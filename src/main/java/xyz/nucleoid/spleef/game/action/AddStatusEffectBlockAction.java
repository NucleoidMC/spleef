package xyz.nucleoid.spleef.game.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.codecs.MoreCodecs;

public record AddStatusEffectBlockAction(StatusEffectInstance effect, BlockActionTarget target) implements BlockAction {
    private static final Codec<StatusEffectInstance> STATUS_EFFECT_CODEC = MoreCodecs.withNbt(effect -> effect.writeNbt(new NbtCompound()), nbt -> {
        if (nbt instanceof NbtCompound compound) {
            var effect = StatusEffectInstance.fromNbt(compound);

            if (effect == null) {
                return DataResult.error(() -> "Unknown status effect ID");
            } else {
                return DataResult.success(effect);
            }
        }

        return DataResult.error(() -> "Expected compound tag");
    });

    public static final Codec<AddStatusEffectBlockAction> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                STATUS_EFFECT_CODEC.fieldOf("effect").forGetter(AddStatusEffectBlockAction::effect),
                BlockActionTarget.CODEC.fieldOf("target").forGetter(AddStatusEffectBlockAction::target)
        ).apply(instance, AddStatusEffectBlockAction::new);
    });

    @Override
    public void apply(ServerPlayerEntity player, BlockActionContext context) {
        player.addStatusEffect(new StatusEffectInstance(this.effect), player);
    }

    @Override
    public Codec<AddStatusEffectBlockAction> getCodec() {
        return CODEC;
    }
}
