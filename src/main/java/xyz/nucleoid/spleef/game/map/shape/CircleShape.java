package xyz.nucleoid.spleef.game.map.shape;

import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import xyz.nucleoid.plasmid.game.map.template.MapTemplate;
import xyz.nucleoid.plasmid.util.BlockBounds;
import xyz.nucleoid.spleef.game.map.SpleefMapGenerator.Brush;
import net.minecraft.util.math.BlockPos;

public class CircleShape implements MapShape {
    public static final Codec<CircleShape> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.INT.fieldOf("radius").forGetter(config -> config.radius),
            Codec.INT.optionalFieldOf("inner_radius", 0).forGetter(config -> config.innerRadius)
        ).apply(instance, CircleShape::new);
    });

    private final int radius;
    private final int innerRadius;

    public CircleShape(int radius, int innerRadius) {
        this.radius = radius;
        this.innerRadius = innerRadius;
    }

    @Override
    public void generate(MapTemplate template, int minY, int maxY, Brush brush, Random random) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

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

                mutablePos.set(x, 0, z);

                if ((distance2 >= outlineRadius2 || (distance2 < inlineRadius2 && this.innerRadius > 0)) && brush.outlineProvider != null) {
                    for (int y = minY; y <= maxY; y++) {
                        mutablePos.setY(y);
                        template.setBlockState(mutablePos, brush.provideOutline(random, mutablePos));
                    }
                } else if (brush.fillProvider != null) {
                    for (int y = minY; y <= maxY; y++) {
                        mutablePos.setY(y);
                        template.setBlockState(mutablePos, brush.provideFill(random, mutablePos));
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
    public int getSpawnOffset() {
        return this.innerRadius + 1;
    }

    @Override
    public Codec<CircleShape> getCodec() {
        return CODEC;
    }
}
