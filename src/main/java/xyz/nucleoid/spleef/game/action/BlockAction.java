package xyz.nucleoid.spleef.game.action;

import com.mojang.serialization.Codec;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.registry.TinyRegistry;
import xyz.nucleoid.spleef.game.SpleefActive;

import java.util.function.Function;

public interface BlockAction {
    TinyRegistry<Codec<? extends BlockAction>> REGISTRY = TinyRegistry.create();
    Codec<BlockAction> REGISTRY_CODEC = REGISTRY.dispatchMap(BlockAction::getCodec, Function.identity()).codec();

    public void apply(ServerPlayerEntity player, BlockActionContext context);

    public BlockActionTarget target();

    Codec<? extends BlockAction> getCodec();

    public static void apply(BlockAction action, ServerPlayerEntity self, PlayerSet players, SpleefActive game) {
        var context = new BlockActionContext(self, players, game);

        action.target().forEachTarget(context, player -> {
            action.apply(player, context);
        });
    }
}
