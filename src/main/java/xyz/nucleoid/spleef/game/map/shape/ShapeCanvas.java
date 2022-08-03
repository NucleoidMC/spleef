package xyz.nucleoid.spleef.game.map.shape;

import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanMaps;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import net.minecraft.util.math.ChunkPos;

public final class ShapeCanvas {
    private final Long2BooleanMap points = new Long2BooleanOpenHashMap();
    private int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

    public void putFill(int x, int z) {
        this.put(x, z, true);
    }

    public void putOutline(int x, int z) {
        this.put(x, z, false);
    }

    private void put(int x, int z, boolean fill) {
        if (x < this.minX) this.minX = x;
        if (z < this.minZ) this.minZ = z;
        if (x > this.maxX) this.maxX = x;
        if (z > this.maxZ) this.maxZ = z;

        this.points.put(ChunkPos.toLong(x, z), fill);
    }

    public SpleefShape render() {
        var shape = new SpleefShape.Builder(this.minX, this.minZ, this.maxX, this.maxZ);
        for (var entry : Long2BooleanMaps.fastIterable(this.points)) {
            long pos = entry.getLongKey();
            int x = ChunkPos.getPackedX(pos);
            int z = ChunkPos.getPackedZ(pos);

            if (entry.getBooleanValue()) {
                shape.putFill(x, z);
            } else {
                shape.putOutline(x, z);
            }
        }

        return shape.build();
    }
}
