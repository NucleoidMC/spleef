package xyz.nucleoid.spleef.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.block.Blocks;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;
import xyz.nucleoid.spleef.game.map.shape.ShapeCanvas;
import xyz.nucleoid.spleef.game.map.shape.ShapePlacer;
import xyz.nucleoid.spleef.game.map.shape.SpleefShape;
import xyz.nucleoid.spleef.game.map.shape.WorldShapePlacer;
import xyz.nucleoid.spleef.game.map.shape.renderer.MapShapeRenderer;

public class RenderShapeCommand {
    private static final BlockStateProvider FILL_PROVIDER = new SimpleBlockStateProvider(Blocks.WHITE_CONCRETE.getDefaultState());
    private static final BlockStateProvider OUTLINE_PROVIDER = new SimpleBlockStateProvider(Blocks.BLACK_CONCRETE.getDefaultState());

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager
            .literal("rendershape")
            .then(CommandManager.argument("shape", MapShapeRendererArgumentType.mapShapeRenderer())
            .executes(RenderShapeCommand::execute)));
    }

    private static int execute(CommandContext<ServerCommandSource> context) {

        ShapeCanvas canvas = new ShapeCanvas();

        MapShapeRenderer renderer = MapShapeRendererArgumentType.getMapShapeRenderer(context, "shape");
        renderer.renderTo(canvas);

        SpleefShape shape = canvas.render();

        Vec3d pos = context.getSource().getPosition();
        int x = (int) pos.getX();
        int y = (int) pos.getY() - 1;
        int z = (int) pos.getZ();

        ShapePlacer fill = new WorldShapePlacer(x, z, context.getSource().getWorld(), FILL_PROVIDER);
        fill.fill(shape, y, y);

        ShapePlacer outline = new WorldShapePlacer(x, z, context.getSource().getWorld(), OUTLINE_PROVIDER);
        outline.outline(shape, y, y);
        
        return 1;
    }
}
