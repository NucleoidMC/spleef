package net.gegy1000.spleef.game.map.shape;

import net.gegy1000.plasmid.game.map.template.MapTemplate;
import net.gegy1000.spleef.game.map.SpleefMapGenerator.Brush;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class SquareShape implements MapShape {

    @Override
    public void generate(MapTemplate template, int radius, int minY, int maxY, Brush brush) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        BlockState outline = brush.outline;
        BlockState fill = brush.fill;

        for (int z = -radius; z <= radius; z++) {
            for (int x = -radius; x <= radius; x++) {
                mutablePos.set(x, 0, z);

                if ((z == -radius || z == radius || x == -radius || x == radius) && outline != null) {
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