package xyz.nucleoid.spleef;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.util.Identifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.spleef.command.MapShapeRendererArgumentType;
import xyz.nucleoid.spleef.command.RenderShapeCommand;
import xyz.nucleoid.spleef.game.SpleefConfig;
import xyz.nucleoid.spleef.game.SpleefWaiting;
import xyz.nucleoid.spleef.game.map.shape.renderer.*;

public final class Spleef implements ModInitializer {
    public static final String ID = "spleef";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    @Override
    public void onInitialize() {
        GameType.register(
                new Identifier(Spleef.ID, "spleef"),
                SpleefWaiting::open,
                SpleefConfig.CODEC
        );

        MapShapeRenderer.REGISTRY.register(new Identifier(Spleef.ID, "circle"), CircleShapeRenderer.CODEC);
        MapShapeRenderer.REGISTRY.register(new Identifier(Spleef.ID, "square"), SquareShapeRenderer.CODEC);
        MapShapeRenderer.REGISTRY.register(new Identifier(Spleef.ID, "pattern"), PatternShapeRenderer.CODEC);
        
        ArgumentTypes.register("spleef:map_shape_renderer", MapShapeRendererArgumentType.class, new ConstantArgumentSerializer<MapShapeRendererArgumentType>(MapShapeRendererArgumentType::mapShapeRenderer));
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            RenderShapeCommand.register(dispatcher);
        });
    }
}
