package xyz.nucleoid.spleef.game.map.shape;

import java.util.Random;

import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public abstract class CustomOffsetShapePlacer extends ShapePlacer {
    private final int offsetX;
    private final int offsetZ;

    public CustomOffsetShapePlacer(int offsetX, int offsetZ, BlockStateProvider provider, Random random) {
        super(provider, random);

        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
    }

    @Override
    public int getOffsetX() {
        return this.offsetX;
    }
        
    @Override
    public int getOffsetZ() {
        return this.offsetZ;
    }
}
