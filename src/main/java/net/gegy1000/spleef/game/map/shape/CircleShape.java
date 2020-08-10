package net.gegy1000.spleef.game.map.shape;

import net.gegy1000.plasmid.game.map.template.MapTemplate;
import net.gegy1000.spleef.game.map.SpleefMapGenerator.Brush;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class CircleShape implements MapShape {
    @Override
    public void generate(MapTemplate template, int radius, int minY, int maxY, Brush brush) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        int radius2 = radius * radius;
        int outlineRadius2 = (radius - 1) * (radius - 1);

        BlockState outline = brush.outline;
        BlockState fill = brush.fill;

        for (int z = -radius; z <= radius; z++) {
            for (int x = -radius; x <= radius; x++) {
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
}