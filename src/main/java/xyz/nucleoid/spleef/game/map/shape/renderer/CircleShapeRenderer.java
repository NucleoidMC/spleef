package xyz.nucleoid.spleef.game.map.shape.renderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.spleef.game.map.shape.ShapeCanvas;

public class CircleShapeRenderer implements MapShapeRenderer {
    public static final Codec<CircleShapeRenderer> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.INT.fieldOf("radius").forGetter(config -> config.radius),
                Codec.INT.optionalFieldOf("inner_radius", 0).forGetter(config -> config.innerRadius)
        ).apply(instance, CircleShapeRenderer::new);
    });

    private final int radius;
    private final int innerRadius;

    public CircleShapeRenderer(int radius, int innerRadius) {
        this.radius = radius;
        this.innerRadius = innerRadius;
    }

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
    public Codec<CircleShapeRenderer> getCodec() {
        return CODEC;
    }
}
