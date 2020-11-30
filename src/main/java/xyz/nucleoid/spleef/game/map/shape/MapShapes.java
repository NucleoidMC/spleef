package xyz.nucleoid.spleef.game.map.shape;

import com.mojang.serialization.Codec;

import xyz.nucleoid.spleef.Spleef;
import net.minecraft.util.Identifier;

public enum MapShapes {
    CIRCLE("circle", CircleShape.CODEC),
    PATTERN("pattern", PatternShape.CODEC),
    SQUARE("square", SquareShape.CODEC);

    private MapShapes(String path, Codec<? extends MapShape> codec) {
        Identifier id = new Identifier(Spleef.ID, path);
        MapShape.REGISTRY.register(id, codec);
    }

    public static void initialize() {
        return;
    }
}
