package xyz.nucleoid.spleef.game.map.shape.renderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.spleef.game.map.shape.ShapeCanvas;

public record SquareShapeRenderer(int size) implements MapShapeRenderer {
    public static final Codec<SquareShapeRenderer> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.INT.fieldOf("size").forGetter(SquareShapeRenderer::size)
        ).apply(instance, SquareShapeRenderer::new);
    });

    @Override
    public void renderTo(ShapeCanvas canvas) {
        var mutablePos = new BlockPos.Mutable();

        for (int z = -this.size; z <= this.size; z++) {
            for (int x = -this.size; x <= this.size; x++) {
                mutablePos.set(x, 0, z);

                if (z == -this.size || z == this.size || x == -this.size || x == this.size) {
                    canvas.putOutline(x, z);
                } else {
                    canvas.putFill(x, z);
                }
            }
        }
    }

    @Override
    public Codec<SquareShapeRenderer> getCodec() {
        return CODEC;
    }
}
