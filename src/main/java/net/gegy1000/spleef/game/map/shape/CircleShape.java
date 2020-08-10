package net.gegy1000.spleef.game.map.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.gegy1000.plasmid.game.map.template.MapTemplate;
import net.gegy1000.plasmid.util.BlockBounds;
import net.gegy1000.spleef.game.map.SpleefMapGenerator.Brush;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class CircleShape implements MapShape {
    public static final Codec<CircleShape> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.INT.fieldOf("radius").forGetter(config -> config.radius)
        ).apply(instance, CircleShape::new);
    });

    private final int radius;

    public CircleShape(int radius) {
        this.radius = radius;
    }

    @Override
    public void generate(MapTemplate template, int minY, int maxY, Brush brush) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        int radius2 = this.radius * this.radius;
        int outlineRadius2 = (this.radius - 1) * (this.radius - 1);

        BlockState outline = brush.outline;
        BlockState fill = brush.fill;

        for (int z = -this.radius; z <= this.radius; z++) {
            for (int x = -this.radius; x <= this.radius; x++) {
                int distance2 = x * x + z * z;
                if (distance2 >= radius2) {
                    continue;
                }

                mutablePos.set(x, 0, z);

                if (distance2 >= outlineRadius2 && outline != null) {
                    for (int y = minY; y <= maxY; y++) {
                        mutablePos.setY(y);
                        template.setBlockState(mutablePos, outline);
                    }
                } else if (fill != null) {
                    for (int y = minY; y <= maxY; y++) {
                        mutablePos.setY(y);
                        template.setBlockState(mutablePos, fill);
                    }
                }
            }
        }
    }

    @Override
    public BlockBounds getLevelBounds(int y) {
        return new BlockBounds(
            new BlockPos(-this.radius, y, -this.radius),
            new BlockPos(this.radius, y, this.radius)
        );
    }

    @Override
    public Codec<CircleShape> getCodec() {
        return CODEC;
    }
}