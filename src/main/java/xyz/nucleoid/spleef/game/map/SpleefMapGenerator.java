package xyz.nucleoid.spleef.game.map;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.spleef.game.map.shape.ShapeCanvas;
import xyz.nucleoid.spleef.game.map.shape.ShapePlacer;
import xyz.nucleoid.spleef.game.map.shape.SpleefShape;

public final class SpleefMapGenerator {
    private final SpleefMapConfig config;

    public SpleefMapGenerator(SpleefMapConfig config) {
        this.config = config;
    }

    public SpleefMap build(Random random) {
        var template = MapTemplate.createEmpty();

        int baseHeight = 2;
        int ceilingY = (this.config.levels() + 1) * this.config.levelHeight() + baseHeight;

        var map = new SpleefMap(template, ceilingY);

        var canvas = new ShapeCanvas();
        this.config.shape().renderTo(canvas);

        var shape = canvas.render();
        this.buildFromShape(template, map, shape, random, baseHeight, ceilingY);

        int offsetX = this.config.shape().getSpawnOffsetX();
        int offsetZ = this.config.shape().getSpawnOffsetZ();
        map.setSpawn(new BlockPos(offsetX, this.config.levels() * this.config.levelHeight() + 3, offsetZ));

        return map;
    }

    private void buildFromShape(MapTemplate template, SpleefMap map, SpleefShape shape, Random random, int baseHeight, int ceilingY) {
        var floor = new ShapePlacer(template, this.config.floorProvider(), random);
        var walls = new ShapePlacer(template, this.config.wallProvider(), random);
        var lava = new ShapePlacer(template, this.config.lavaProvider(), random);
        var ceiling = new ShapePlacer(template, this.config.ceilingProvider(), random);

        // base
        walls.fill(shape, 0, baseHeight - 1);
        lava.fill(shape, baseHeight, baseHeight);

        // walls
        walls.outline(shape, baseHeight, ceilingY);

        // ceiling
        ceiling.fill(shape, ceilingY, ceilingY);

        // levels
        for (int level = 0; level < this.config.levels(); level++) {
            int y = (level + 1) * this.config.levelHeight() + baseHeight;
            floor.fill(shape, y, y);

            map.addLevel(shape, y);
        }

        map.providedFloors.addAll(floor.getUsedStates());

        map.setLava(shape, this.config.lavaProvider(), baseHeight);
    }
}
