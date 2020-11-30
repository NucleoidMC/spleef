package xyz.nucleoid.spleef.game.map.shape.renderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.spleef.game.map.shape.ShapeCanvas;

public class SquareShapeRenderer implements MapShapeRenderer {
    public static final Codec<SquareShapeRenderer> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.INT.fieldOf("size").forGetter(config -> config.size)
        ).apply(instance, SquareShapeRenderer::new);
    });

    private final int size;

    public SquareShapeRenderer(int size) {
        this.size = size;
    }

    @Override
    public void renderTo(ShapeCanvas canvas) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

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
