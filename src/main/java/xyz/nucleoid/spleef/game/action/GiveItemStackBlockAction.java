package xyz.nucleoid.spleef.game.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public record GiveItemStackBlockAction(ItemStack stack, BlockActionTarget target) implements BlockAction {
    public static final Codec<GiveItemStackBlockAction> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                ItemStack.CODEC.fieldOf("stack").forGetter(GiveItemStackBlockAction::stack),
                BlockActionTarget.CODEC.fieldOf("target").forGetter(GiveItemStackBlockAction::target)
        ).apply(instance, GiveItemStackBlockAction::new);
    });

    @Override
    public void apply(ServerPlayerEntity player, BlockActionContext context) {
        player.giveItemStack(this.stack.copy());
    }

    @Override
    public Codec<GiveItemStackBlockAction> getCodec() {
        return CODEC;
    }
}
