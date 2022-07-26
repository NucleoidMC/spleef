package xyz.nucleoid.spleef.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

/**
 * A three-dimensional position with
 * optional components, to allow the
 * overriding of specific components
 * in a computed position.
 */
public record OptionalPos(
    Optional<Integer> x,
    Optional<Integer> y,
    Optional<Integer> z
) {
    public static final OptionalPos EMPTY = new OptionalPos(Optional.empty(), Optional.empty(), Optional.empty());

    public static final Codec<OptionalPos> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.INT.optionalFieldOf("x").forGetter(OptionalPos::x),
                Codec.INT.optionalFieldOf("y").forGetter(OptionalPos::y),
                Codec.INT.optionalFieldOf("z").forGetter(OptionalPos::z)
        ).apply(instance, OptionalPos::new);
    });

    public BlockPos apply(BlockPos pos) {
        return new BlockPos(
            this.x.orElse(pos.getX()),
            this.y.orElse(pos.getY()),
            this.z.orElse(pos.getZ())
        );
    }
}
