package xyz.nucleoid.spleef.game.map.shape;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

import java.util.Random;

public abstract class ShapePlacer {
    protected final BlockStateProvider provider;
    protected final Random random;

    private final BlockPos.Mutable mutablePos = new BlockPos.Mutable();

    public ShapePlacer(BlockStateProvider provider, Random random) {
        this.provider = provider;
        this.random = random;
    }

    public void fill(SpleefShape shape, int minY, int maxY) {
        shape.forEachFill((x, z) -> this.setStack(minY, maxY, x, z));
    }

    public void outline(SpleefShape shape, int minY, int maxY) {
        shape.forEachOutline((x, z) -> this.setStack(minY, maxY, x, z));
    }

    private void setStack(int minY, int maxY, int x, int z) {
        this.mutablePos.set(x + this.getOffsetX(), 0, z + this.getOffsetZ());
        for (int y = minY; y <= maxY; y++) {
            this.mutablePos.setY(y);
            this.set(this.mutablePos);
        }
    }
    
    public int getOffsetX() {
        return 0;
    }

    public int getOffsetZ() {
        return 0;
    }

    public abstract void set(BlockPos pos);
}
