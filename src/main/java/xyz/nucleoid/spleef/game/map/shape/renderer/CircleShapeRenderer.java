package xyz.nucleoid.spleef.game.map.shape.renderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.spleef.game.map.shape.ShapeCanvas;

public record CircleShapeRenderer(int radius, int innerRadius) implements MapShapeRenderer {
    public static final MapCodec<CircleShapeRenderer> CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
                Codec.INT.fieldOf("radius").forGetter(CircleShapeRenderer::radius),
                Codec.INT.optionalFieldOf("inner_radius", 0).forGetter(CircleShapeRenderer::innerRadius)
        ).apply(instance, CircleShapeRenderer::new);
    });

    @Override
    public void renderTo(ShapeCanvas canvas) {
        int radius2 = this.radius * this.radius;
        int outlineRadius2 = (this.radius - 1) * (this.radius - 1);
        int innerRadius2 = (this.innerRadius - 1) * (this.innerRadius - 1);
        int inlineRadius2 = this.innerRadius * this.innerRadius;

        for (int z = -this.radius; z <= this.radius; z++) {
            for (int x = -this.radius; x <= this.radius; x++) {
                int distance2 = x * x + z * z;
                if (distance2 >= radius2 || (distance2 < innerRadius2 && this.innerRadius > 0)) {
                    continue;
                }

                if ((distance2 >= outlineRadius2 || (distance2 < inlineRadius2 && this.innerRadius > 0))) {
                    canvas.putOutline(x, z);
                } else {
                    canvas.putFill(x, z);
                }
            }
        }
    }

    @Override
    public int getSpawnOffsetX() {
        return this.innerRadius + 1;
    }

    @Override
    public MapCodec<CircleShapeRenderer> getCodec() {
        return CODEC;
    }
}
