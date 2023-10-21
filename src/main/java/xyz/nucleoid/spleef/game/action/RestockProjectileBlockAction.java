package xyz.nucleoid.spleef.game.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.network.ServerPlayerEntity;

public record RestockProjectileBlockAction(BlockActionTarget target) implements BlockAction {
    public static final Codec<RestockProjectileBlockAction> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                BlockActionTarget.CODEC.fieldOf("target").forGetter(RestockProjectileBlockAction::target)
        ).apply(instance, RestockProjectileBlockAction::new);
    });

    @Override
    public void apply(ServerPlayerEntity player, BlockActionContext context) {
        context.game().restockProjectileFor(player);
    }

    @Override
    public Codec<RestockProjectileBlockAction> getCodec() {
        return CODEC;
    }
}
