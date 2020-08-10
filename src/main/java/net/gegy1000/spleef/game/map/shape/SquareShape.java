package net.gegy1000.spleef.game.map.shape;

import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.gegy1000.plasmid.game.map.template.MapTemplate;
import net.gegy1000.plasmid.util.BlockBounds;
import net.gegy1000.spleef.game.map.SpleefMapGenerator.Brush;
import net.minecraft.util.math.BlockPos;

public class SquareShape implements MapShape {
    public static final Codec<SquareShape> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.INT.fieldOf("size").forGetter(config -> config.size)
        ).apply(instance, SquareShape::new);
    });

    private final int size;

    public SquareShape(int size) {
        this.size = size;
    }

    @Override
    public void generate(MapTemplate template, int minY, int maxY, Brush brush, Random random) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        for (int z = -this.size; z <= this.size; z++) {
            for (int x = -this.size; x <= this.size; x++) {
                mutablePos.set(x, 0, z);

                if ((z == -this.size || z == this.size || x == -this.size || x == this.size) && brush.outlineProvider != null) {
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
            new BlockPos(-this.size, y, -this.size),
            new BlockPos(this.size, y, this.size)
        );
    }

    @Override
    public Codec<SquareShape> getCodec() {
        return CODEC;
    }
}