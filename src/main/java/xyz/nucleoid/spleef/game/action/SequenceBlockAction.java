package xyz.nucleoid.spleef.game.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public record SequenceBlockAction(List<BlockAction> actions) implements BlockAction {
    public static final Codec<SequenceBlockAction> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                BlockAction.REGISTRY_CODEC.listOf().fieldOf("actions").forGetter(SequenceBlockAction::actions)
        ).apply(instance, SequenceBlockAction::new);
    });

    @Override
    public void apply(ServerPlayerEntity player, BlockActionContext context) {
        for (var action : this.actions) {
            action.target().forEachTarget(context, playerx -> {
                action.apply(player, context);
            });
        }
    }

    public BlockActionTarget target() {
        return BlockActionTarget.SELF;
    }

    @Override
    public Codec<SequenceBlockAction> getCodec() {
        return CODEC;
    }
}
