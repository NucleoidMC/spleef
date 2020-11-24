package xyz.nucleoid.spleef.game.map.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.registry.TinyRegistry;
import xyz.nucleoid.plasmid.util.BlockBounds;
import xyz.nucleoid.spleef.game.map.SpleefMapGenerator.Brush;

import java.util.Random;
import java.util.function.Function;

public interface MapShape {
    public static final TinyRegistry<Codec<? extends MapShape>> REGISTRY = TinyRegistry.newStable();
    public static final MapCodec<MapShape> REGISTRY_CODEC = REGISTRY.dispatchMap(MapShape::getCodec, Function.identity());

    public void generate(MapTemplate template, int minY, int maxY, Brush brush, Random random);
    public BlockBounds getLevelBounds(int y);

    public default int getSpawnOffset() {
        return 0;
    }

    public Codec<? extends MapShape> getCodec();
}
