package net.gegy1000.spleef.game.map;

import java.util.HashMap;
import java.util.Map;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gegy1000.spleef.game.map.shape.CircleShape;
import net.gegy1000.spleef.game.map.shape.MapShape;
import net.gegy1000.spleef.game.map.shape.SquareShape;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public final class SpleefMapConfig {
    private static final Map<String, Class<? extends MapShape>> SHAPE_TYPES = new HashMap<>();
    public static final Codec<SpleefMapConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.INT.fieldOf("radius").forGetter(config -> config.radius),
                Codec.INT.fieldOf("levels").forGetter(config -> config.levels),
                Codec.INT.fieldOf("level_height").forGetter(map -> map.levelHeight),
                BlockState.CODEC.optionalFieldOf("wall", Blocks.STONE_BRICKS.getDefaultState()).forGetter(config -> config.wall),
                BlockState.CODEC.optionalFieldOf("floor", Blocks.SNOW_BLOCK.getDefaultState()).forGetter(config -> config.floor),
                Codec.STRING.optionalFieldOf("shape", "circle").forGetter(config -> config.shape)
        ).apply(instance, SpleefMapConfig::new);
    });

    public final int radius;
    public final int levels;
    public final int levelHeight;
    public final BlockState wall;
    public final BlockState floor;
    public final String shape;

    public SpleefMapConfig(int radius, int levels, int levelHeight, BlockState wall, BlockState floor, String shape) {
        this.radius = radius;
        this.levels = levels;
        this.levelHeight = levelHeight;
        this.wall = wall;
        this.floor = floor;
        this.shape = shape;
    }
    
    public Class<? extends MapShape> getShape() {
        return SHAPE_TYPES.getOrDefault(this.shape, CircleShape.class);
    }

    static {
        SHAPE_TYPES.put("circle", CircleShape.class);
        SHAPE_TYPES.put("square", SquareShape.class);
    }
}
