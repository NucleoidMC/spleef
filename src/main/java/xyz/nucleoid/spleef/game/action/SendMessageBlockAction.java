package xyz.nucleoid.spleef.game.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.util.PlasmidCodecs;

public record SendMessageBlockAction(Text message, boolean overlay, BlockActionTarget target) implements BlockAction {
    public static final Codec<SendMessageBlockAction> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                PlasmidCodecs.TEXT.fieldOf("message").forGetter(SendMessageBlockAction::message),
                Codec.BOOL.optionalFieldOf("overlay", false).forGetter(SendMessageBlockAction::overlay),
                BlockActionTarget.CODEC.fieldOf("target").forGetter(SendMessageBlockAction::target)
        ).apply(instance, SendMessageBlockAction::new);
    });

    @Override
    public void apply(ServerPlayerEntity player, BlockActionContext context) {
        player.sendMessage(this.message, this.overlay);
    }

    @Override
    public Codec<SendMessageBlockAction> getCodec() {
        return CODEC;
    }
}
