package xyz.nucleoid.spleef.game.map.shape.renderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import xyz.nucleoid.plasmid.registry.TinyRegistry;
import xyz.nucleoid.spleef.game.map.shape.ShapeCanvas;

import java.util.function.Function;

public interface MapShapeRenderer {
    public static final TinyRegistry<Codec<? extends MapShapeRenderer>> REGISTRY = TinyRegistry.newStable();
    public static final MapCodec<MapShapeRenderer> REGISTRY_CODEC = REGISTRY.dispatchMap(MapShapeRenderer::getCodec, Function.identity());

    public void renderTo(ShapeCanvas canvas);

    public default int getSpawnOffsetX() {
        return 0;
    }
    public default int getSpawnOffsetZ() {
        return 0;
    }

    public Codec<? extends MapShapeRenderer> getCodec();
}
