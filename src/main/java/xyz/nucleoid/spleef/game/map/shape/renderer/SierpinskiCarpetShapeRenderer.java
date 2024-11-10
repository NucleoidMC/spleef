package xyz.nucleoid.spleef.game.map.shape.renderer;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.math.MathHelper;
import xyz.nucleoid.spleef.game.map.shape.ShapeCanvas;

public record SierpinskiCarpetShapeRenderer(int order, Optional<Integer> wallOrder) implements MapShapeRenderer {
    public static final MapCodec<SierpinskiCarpetShapeRenderer> CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("order").forGetter(SierpinskiCarpetShapeRenderer::order),
                Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("wall_order").forGetter(SierpinskiCarpetShapeRenderer::wallOrder)
        ).apply(instance, SierpinskiCarpetShapeRenderer::new);
    });

    @Override
    public void renderTo(ShapeCanvas canvas) {
        int size = (int) Math.pow(3, this.order);
        int halfSize = (int) (size / 2d);

        for (int x = -1; x <= size; x++) {
            for (int z = -1; z <= size; z++) {
                if (this.isEdge(x, z, size) || this.isWall(x, z)) {
                    canvas.putOutline(x - halfSize, z - halfSize);
                } else {
                    canvas.putFill(x - halfSize, z - halfSize);
                }
            }
        }
    }

    @Override
    public int getSpawnOffsetX() {
        return MathHelper.ceil(Math.pow(3, this.order) / 6);
    }

    private boolean isEdge(int x, int z, int size) {
        if (x == -1 || z == -1) return true;
        if (x == size || z == size) return true;
        return false;
    }

    private boolean isWall(int x, int z) {
        int order = 0;

        while (x != 0 && z != 0) {
            if (x % 3 == 1 && z % 3 == 1 && this.isWallOrder(order)) {
                return true;
            }

            x /= 3;
            z /= 3;

            order += 1;
        }

        return false;
    }

    private boolean isWallOrder(int order) {
        return !this.wallOrder.isPresent() || (this.order - order) <= this.wallOrder.get();
    }

    @Override
    public MapCodec<SierpinskiCarpetShapeRenderer> getCodec() {
        return CODEC;
    }
}
