package net.gegy1000.spleef.game.map;

import net.gegy1000.plasmid.game.map.template.MapTemplate;
import net.gegy1000.plasmid.util.BlockBounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.CompletableFuture;

public final class SpleefMapGenerator {
    private final SpleefMapConfig config;

    public SpleefMapGenerator(SpleefMapConfig config) {
        this.config = config;
    }

    public CompletableFuture<SpleefMap> create() {
        return CompletableFuture.supplyAsync(this::build, Util.getServerWorkerExecutor());
    }

    private SpleefMap build() {
        MapTemplate template = MapTemplate.createEmpty();
        SpleefMap map = new SpleefMap(template, this.config);

        this.addBase(template);
        this.addLevels(template, map);
        this.addWall(template);

        map.setSpawn(new BlockPos(0, this.config.levels * this.config.levelHeight + 2, 0));

        return map;
    }

    private void addBase(MapTemplate template) {
        int radius = this.config.radius;
        BlockState wall = this.config.wall;
        BlockState lava = Blocks.LAVA.getDefaultState();

        this.addShape(template, radius, 0, 0, Brush.fill(wall));
        this.addShape(template, radius, 1, 1, new Brush(wall, lava));
        this.addShape(template, radius, 1, this.config.levelHeight + 1, Brush.outline(wall));
    }

    private void addLevels(MapTemplate template, SpleefMap map) {
        int radius = this.config.radius;
        Brush brush = new Brush(this.config.wall, this.config.floor);

        for (int level = 0; level < this.config.levels; level++) {
            int y = (level + 1) * this.config.levelHeight + 1;
            this.addShape(template, radius, y, y, brush);

            map.addLevel(new BlockBounds(
                    new BlockPos(-radius, y, -radius),
                    new BlockPos(radius, y, radius)
            ));
        }
    }

    private void addWall(MapTemplate template) {
        Brush wallBrush = Brush.outline(this.config.wall);

        int radius = this.config.radius;
        int minY = 1;
        int maxY = (this.config.levels + 1) * this.config.levelHeight;

        this.addShape(template, radius, minY, maxY, wallBrush);
	}
	
	private void addShape(MapTemplate template, int radius, int minY, int maxY, Brush brush) {
		if (this.config.square) {
			this.addSquare(template, radius, minY, maxY, brush);
		} else {
			this.addCircle(template, radius, minY, maxY, brush);
		}
	}

	private void addSquare(MapTemplate template, int radius, int minY, int maxY, Brush brush) {
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

    private void addCircle(MapTemplate template, int radius, int minY, int maxY, Brush brush) {
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

    static final class Brush {
        final BlockState outline;
        final BlockState fill;

        public Brush(BlockState outline, BlockState fill) {
            this.outline = outline;
            this.fill = fill;

            if (outline == null && fill == null) {
                throw new IllegalArgumentException("null brush");
            }
        }

        public static Brush outline(BlockState block) {
            return new Brush(block, null);
        }

        public static Brush fill(BlockState block) {
            return new Brush(null, block);
        }
    }
}
