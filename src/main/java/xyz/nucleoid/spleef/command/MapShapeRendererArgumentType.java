package xyz.nucleoid.spleef.command;

import java.util.Arrays;
import java.util.Collection;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DataResult.PartialResult;

import net.minecraft.command.argument.NbtTagArgumentType;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import xyz.nucleoid.spleef.game.map.shape.renderer.MapShapeRenderer;

public class MapShapeRendererArgumentType implements ArgumentType<MapShapeRenderer> {
    private static final Collection<String> EXAMPLES = Arrays.asList("{type='spleef:circle',radius=16}");

    public static MapShapeRendererArgumentType mapShapeRenderer() {
        return new MapShapeRendererArgumentType();
    }

    public static MapShapeRenderer getMapShapeRenderer(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, MapShapeRenderer.class);
    }

    @Override
    public MapShapeRenderer parse(StringReader reader) throws CommandSyntaxException {
        Tag tag = NbtTagArgumentType.nbtTag().parse(reader);

        DataResult<MapShapeRenderer> result = MapShapeRenderer.REGISTRY_CODEC.codec().parse(NbtOps.INSTANCE, tag);
        if (result.error().isPresent()) {
            PartialResult<MapShapeRenderer> partial = result.error().get();
            throw new SimpleCommandExceptionType(new LiteralText(partial.message())).create();
        }
        return result.result().get();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
