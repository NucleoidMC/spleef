package xyz.nucleoid.spleef.game.map.shape;

import xyz.nucleoid.spleef.game.map.SpleefLevel;

public final class SpleefShape {
    private static final byte EMPTY = 0;
    private static final byte FILL = 1;
    private static final byte OUTLINE = 2;

    public final Bounds bounds;
    private final byte[] shape;

    private SpleefShape(Bounds bounds, byte[] shape) {
        this.bounds = bounds;
        this.shape = shape;
    }

    public void forEachFill(ForEach handler) {
        this.forEachOf(handler, FILL);
    }

    public void forEachOutline(ForEach handler) {
        this.forEachOf(handler, OUTLINE);
    }

    private void forEachOf(ForEach handler, byte of) {
        var shape = this.shape;
        for (int z = this.bounds.minZ(); z <= this.bounds.maxZ(); z++) {
            for (int x = this.bounds.minX(); x <= this.bounds.maxX(); x++) {
                int index = this.bounds.index(x, z);
                if (shape[index] == of) {
                    handler.accept(x, z);
                }
            }
        }
    }

    public boolean hasBlockAt(int x, int z) {
        return this.get(x, z) != EMPTY;
    }

    public boolean isFillAt(int x, int z) {
        return this.get(x, z) == FILL;
    }

    public boolean isOutlineAt(int x, int z) {
        return this.get(x, z) == OUTLINE;
    }

    private byte get(int x, int z) {
        int index = this.bounds.index(x, z);
        return index != -1 ? this.shape[index] : EMPTY;
    }

    public SpleefLevel toLevel(int y) {
        return new SpleefLevel(this, y);
    }

    public interface ForEach {
        void accept(int x, int z);
    }

    public static class Builder {
        private final Bounds bounds;
        private final byte[] shape;

        public Builder(int minX, int minZ, int maxX, int maxZ) {
            this.bounds = new Bounds(minX, minZ, maxX, maxZ);
            this.shape = new byte[this.bounds.count()];
        }

        public void putFill(int x, int z) {
            this.shape[this.index(x, z)] = FILL;
        }

        public void putOutline(int x, int z) {
            this.shape[this.index(x, z)] = OUTLINE;
        }

        private int index(int x, int z) {
            int index = this.bounds.index(x, z);
            if (index == -1) {
                throw new IllegalArgumentException("Point (" + x + "; " + z + ") out of bounds " + this.bounds);
            }
            return index;
        }

        public SpleefShape build() {
            return new SpleefShape(this.bounds, this.shape);
        }
    }

    private record Bounds(int minX, int minZ, int maxX, int maxZ) {
        public int index(int x, int z) {
            if (x < this.minX || z < this.minZ || x > this.maxX || z > this.maxZ) {
                return -1;
            }
            return (x - this.minX) + (z - this.minZ) * this.width();
        }

        public int width() {
            return this.maxX - this.minX + 1;
        }

        public int height() {
            return this.maxZ - this.minZ + 1;
        }

        public int count() {
            return this.width() * this.height();
        }
    }
}
