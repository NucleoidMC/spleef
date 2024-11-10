package xyz.nucleoid.spleef.game.action;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.StringIdentifiable;

import java.util.function.Consumer;

public enum BlockActionTarget implements StringIdentifiable {
    SELF("self"),
    OTHERS("others"),
    ALL("all")
    ;

    public static final com.mojang.serialization.Codec<BlockActionTarget> CODEC = StringIdentifiable.createCodec(BlockActionTarget::values);

    public final String name;

    BlockActionTarget(String name) {
        this.name = name;
    }

    public void forEachTarget(BlockActionContext context, Consumer<ServerPlayerEntity> consumer) {
        if (this == SELF) {
            consumer.accept(context.self());
            return;
        }

        context.players().stream()
                .filter(player -> {
                    if (this == OTHERS && player == context.self()) {
                        return false;
                    }

                    return !player.isSpectator();
                })
                .forEach(consumer);
    }

    public String asString() {
        return this.name;
    }
}
