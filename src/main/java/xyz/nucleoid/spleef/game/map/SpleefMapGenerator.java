package xyz.nucleoid.spleef.game.map;

import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.spleef.game.map.shape.SpleefShape;
import xyz.nucleoid.spleef.game.map.shape.MapShapePlacer;
import xyz.nucleoid.spleef.game.map.shape.ShapeCanvas;
import xyz.nucleoid.spleef.game.map.shape.ShapePlacer;

import java.util.Random;

public final class SpleefMapGenerator {
    private final SpleefMapConfig config;

    public SpleefMapGenerator(SpleefMapConfig config) {
        this.config = config;
    }

    public SpleefMap build() {
        MapTemplate template = MapTemplate.createEmpty();

        SpleefMap map = new SpleefMap(template);

        ShapeCanvas canvas = new ShapeCanvas();
        this.config.shape.renderTo(canvas);

        SpleefShape shape = canvas.render();
        this.buildFromShape(template, map, shape);

        int offsetX = this.config.shape.getSpawnOffsetX();
        int offsetZ = this.config.shape.getSpawnOffsetZ();
        map.setSpawn(new BlockPos(offsetX, this.config.levels * this.config.levelHeight + 3, offsetZ));

        return map;
    }

    private void buildFromShape(MapTemplate template, SpleefMap map, SpleefShape shape) {
        Random random = new Random();

        MapShapePlacer floor = new MapShapePlacer(template, this.config.floorProvider, random);
        ShapePlacer walls = new MapShapePlacer(template, this.config.wallProvider, random);
        ShapePlacer lava = new MapShapePlacer(template, this.config.lavaProvider, random);
        ShapePlacer ceiling = new MapShapePlacer(template, this.config.ceilingProvider, random);

        int baseHeight = 2;

        // base
        walls.fill(shape, 0, baseHeight - 1);
        lava.fill(shape, baseHeight, baseHeight);

        // walls
        int ceilingY = (this.config.levels + 1) * this.config.levelHeight + baseHeight;
        walls.outline(shape, baseHeight, ceilingY);

        // ceiling
        ceiling.fill(shape, ceilingY, ceilingY);

        // levels
        for (int level = 0; level < this.config.levels; level++) {
            int y = (level + 1) * this.config.levelHeight + baseHeight;
            floor.fill(shape, y, y);

            map.addLevel(shape, y);
        }

        map.providedFloors.addAll(floor.getUsedStates());
    }
}
