package net.gegy1000.spleef.game.map.shape;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.gegy1000.plasmid.game.map.template.MapTemplate;
import net.gegy1000.plasmid.registry.TinyRegistry;
import net.gegy1000.plasmid.util.BlockBounds;
import net.gegy1000.spleef.game.map.SpleefMapGenerator.Brush;

public interface MapShape {
    public static final TinyRegistry<Codec<? extends MapShape>> REGISTRY = TinyRegistry.newStable();
    public static final MapCodec<MapShape> REGISTRY_CODEC = REGISTRY.dispatchMap(MapShape::getCodec, Function.identity());

    public void generate(MapTemplate template, int minY, int maxY, Brush brush);
    public BlockBounds getLevelBounds(int y);

    public default int getSpawnOffset() {
        return 0;
    }

    public Codec<? extends MapShape> getCodec();
}