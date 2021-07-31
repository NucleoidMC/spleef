package xyz.nucleoid.spleef.game.map.shape;

import xyz.nucleoid.map_templates.BlockBounds;

public final class SpleefShape {
    static final byte EMPTY = 0;
    static final byte FILL = 1;
    static final byte OUTLINE = 2;

    public final int minX, minZ;
    public final int maxX, maxZ;
    private final int width;

    private final byte[] shape;

    SpleefShape(int minX, int minZ, int maxX, int maxZ, byte[] shape) {
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;

        this.width = this.maxX - this.minX + 1;
        this.shape = shape;
    }

    public BlockBounds asBounds(int minY, int maxY) {
        return BlockBounds.of(this.minX, minY, this.minZ, this.maxX, maxY, this.maxZ);
    }

    public void forEachFill(ForEach handler) {
        this.forEachOf(handler, FILL);
    }

    public void forEachOutline(ForEach handler) {
        this.forEachOf(handler, OUTLINE);
    }

    private void forEachOf(ForEach handler, byte of) {
        var shape = this.shape;
        for (int z = this.minZ; z <= this.maxZ; z++) {
            for (int x = this.minX; x <= this.maxX; x++) {
                int index = this.index(x, z);
                if (shape[index] == of) {
                    handler.accept(x, z);
                }
            }
        }
    }

    public boolean hasBlockAt(int x, int z) {
        return this.get(x, z) == EMPTY;
    }

    public boolean isFillAt(int x, int z) {
        return this.get(x, z) == FILL;
    }

    public boolean isOutlineAt(int x, int z) {
        return this.get(x, z) == OUTLINE;
    }

    private byte get(int x, int z) {
        int index = this.index(x, z);
        return index != -1 ? this.shape[index] : EMPTY;
    }

    private int index(int x, int z) {
        if (x < this.minX || z < this.minZ || x > this.maxX || z > this.maxZ) {
            return -1;
        }
        return (x - this.minX) + (z - this.minZ) * this.width;
    }

    public interface ForEach {
        void accept(int x, int z);
    }
}
