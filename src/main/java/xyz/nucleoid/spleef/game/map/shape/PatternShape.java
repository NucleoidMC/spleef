package xyz.nucleoid.spleef.game.map.shape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.util.BlockBounds;
import xyz.nucleoid.spleef.game.map.SpleefMapGenerator.Brush;

public class PatternShape implements MapShape {
    public static final Codec<PatternShape> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.STRING.listOf().fieldOf("tiles").xmap(tileStringList -> {
                return tileStringList.stream().map(tileString -> {
                    boolean[] tileArray = new boolean[tileString.length()];
                    for (int index = 0; index < tileString.length(); index++) {
                        tileArray[index] = tileString.charAt(index) != ' ';
                    }
                    return tileArray;
                }).toArray(boolean[][]::new);
            }, tileBooleanArrayArray -> {
                List<String> tileStringList = new ArrayList<>();
                for (boolean[] tileBooleanArray : tileBooleanArrayArray) {
                    String tileString = "";
                    for (boolean tileBoolean : tileBooleanArray) {
                        tileString += tileBoolean ? 'x' : ' ';
                    }
                    tileStringList.add(tileString);
                }
                return tileStringList;
            }).forGetter(config -> config.tiles),
            Codec.INT.fieldOf("tile_size").forGetter(config -> config.tileSize)
        ).apply(instance, PatternShape::new);
    });

    private final boolean[][] tiles;
    private final int maxWidth;
    private final int tileSize;

    public PatternShape(boolean[][] tiles, int tileSize) {
        this.tiles = tiles;
        this.maxWidth = Arrays.stream(tiles).map(row -> row.length).max(Integer::compare).get();
        this.tileSize = tileSize;
    }

    private boolean isOutline(int row, int column) {
        if (column >= this.maxWidth - 1) return true;
        return this.tiles[row][column];
    }

    @Override
    public void generate(MapTemplate template, int minY, int maxY, Brush brush, Random random) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        for (int row = 0; row < this.tiles.length; row++) {
            for (int column = 0; column < this.maxWidth; column++) {
                for (int relZ = 0; relZ < this.tileSize; relZ++) {
                    for (int relX = 0; relX < this.tileSize; relX++) {
                        mutablePos.set(column * this.tileSize + relX, 0, row * this.tileSize + relZ);
                        
                        if (this.isOutline(row, column) && brush.outlineProvider != null) {
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
        }
    }

    @Override
    public BlockBounds getLevelBounds(int y) {
        return new BlockBounds(
            new BlockPos(0, y, 0),
            new BlockPos(this.maxWidth * this.tileSize, y, this.maxWidth * this.tileSize)
        );
    }
    
    @Override
    public int getSpawnOffsetX() {
        return this.tiles[this.tiles.length / 2].length * this.tileSize / 2;
    }

    @Override
    public int getSpawnOffsetZ() {
        return this.tiles.length * this.tileSize / 2;
    }

    @Override
    public Codec<PatternShape> getCodec() {
        return CODEC;
    }
}
