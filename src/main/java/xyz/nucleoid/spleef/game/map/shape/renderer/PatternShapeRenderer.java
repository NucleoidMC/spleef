package xyz.nucleoid.spleef.game.map.shape.renderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.spleef.game.map.shape.ShapeCanvas;

import java.util.ArrayList;
import java.util.Arrays;

public class PatternShapeRenderer implements MapShapeRenderer {
    private static final Codec<boolean[][]> PATTERN_CODEC = Codec.STRING.listOf()
            .xmap(pattern -> {
                return pattern.stream().map(row -> {
                    var mask = new boolean[row.length()];
                    for (int index = 0; index < row.length(); index++) {
                        mask[index] = row.charAt(index) != ' ';
                    }
                    return mask;
                }).toArray(boolean[][]::new);
            }, mask -> {
                var pattern = new ArrayList<String>();
                for (var row : mask) {
                    var rowPattern = new StringBuilder();
                    for (boolean tile : row) {
                        rowPattern.append(tile ? 'x' : ' ');
                    }
                    pattern.add(rowPattern.toString());
                }
                return pattern;
            });

    public static final Codec<PatternShapeRenderer> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                PATTERN_CODEC.fieldOf("tiles").forGetter(config -> config.tiles),
                Codec.INT.fieldOf("tile_size").forGetter(config -> config.tileSize)
        ).apply(instance, PatternShapeRenderer::new);
    });

    private final boolean[][] tiles;
    private final int tileSize;
    private final int width;
    private final int height;

    public PatternShapeRenderer(boolean[][] tiles, int tileSize) {
        this.tiles = tiles;
        this.tileSize = tileSize;

        this.width = Arrays.stream(tiles).mapToInt(row -> row.length).max().orElse(0);
        this.height = tiles.length;
    }

    private boolean isOutline(int row, int column) {
        if (column >= this.width - 1) return true;
        return this.tiles[row][column];
    }

    @Override
    public void renderTo(ShapeCanvas canvas) {
        for (int row = 0; row < this.height; row++) {
            for (int column = 0; column < this.width; column++) {
                for (int relZ = 0; relZ < this.tileSize; relZ++) {
                    for (int relX = 0; relX < this.tileSize; relX++) {
                        int x = column * this.tileSize + relX;
                        int z = row * this.tileSize + relZ;

                        if (this.isOutline(row, column)) {
                            canvas.putOutline(x, z);
                        } else {
                            canvas.putFill(x, z);
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getSpawnOffsetX() {
        return this.tiles[this.height / 2].length * this.tileSize / 2;
    }

    @Override
    public int getSpawnOffsetZ() {
        return this.height * this.tileSize / 2;
    }

    @Override
    public Codec<PatternShapeRenderer> getCodec() {
        return CODEC;
    }
}
