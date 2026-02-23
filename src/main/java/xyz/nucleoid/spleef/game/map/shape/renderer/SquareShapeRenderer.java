package xyz.nucleoid.spleef.game.map.shape.renderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import xyz.nucleoid.spleef.game.map.shape.ShapeCanvas;

public record SquareShapeRenderer(int size) implements MapShapeRenderer {
    public static final MapCodec<SquareShapeRenderer> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("size").forGetter(SquareShapeRenderer::size)
    ).apply(instance, SquareShapeRenderer::new));

    @Override
    public void renderTo(ShapeCanvas canvas) {
        var mutablePos = new BlockPos.MutableBlockPos();

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
    public MapCodec<SquareShapeRenderer> getCodec() {
        return CODEC;
    }
}
