package xyz.nucleoid.spleef.game.map;

import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.spleef.game.map.shape.SpleefShape;

import java.util.function.Consumer;

public record SpleefLevel(SpleefShape shape, int y) {
    public boolean contains(BlockPos pos) {
        return pos.getY() == this.y && this.shape.isFillAt(pos.getX(), pos.getZ());
    }

    public void forEach(Consumer<BlockPos> consumer) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        this.shape.forEachFill((x, z) -> {
            mutablePos.set(x, this.y, z);
            consumer.accept(mutablePos);
        });
    }
}
