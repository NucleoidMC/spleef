package xyz.nucleoid.spleef.game.map;

import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.spleef.game.map.shape.SpleefShape;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public final class SpleefTemplateMapBuilder {
    private final SpleefTemplateMapConfig config;

    public SpleefTemplateMapBuilder(SpleefTemplateMapConfig config) {
        this.config = config;
    }

    public SpleefMap build(MinecraftServer server) {
        MapTemplate template;
        try {
            template = MapTemplateSerializer.loadFromResource(server, this.config.template());
        } catch (IOException e) {
            throw new GameOpenException(Text.literal("Failed to find map template for: " + this.config.template()));
        }

        final SpleefTemplateMapConfig.Levels levelsConfig = this.config.levels();
        final Predicate<BlockState> levelBlockPredicate = state -> levelsConfig.block() == state.getBlock();

        final List<SpleefLevel> levels = template.getMetadata().getRegionBounds(levelsConfig.region())
            .map(bounds -> buildLevel(template, bounds, levelBlockPredicate))
            .sorted(Comparator.comparingInt(SpleefLevel::y))
            .toList();

        if (levels.isEmpty()) {
            throw new GameOpenException(Text.literal("Found no levels in map template: " + this.config.template()));
        }

        int ceilingY = levels.get(levels.size() - 1).y() + 2;
        var map = new SpleefMap(template, ceilingY);
        levels.forEach(map::addLevel);

        map.providedFloors.add(levelsConfig.block().getDefaultState());

        this.config.lava().ifPresent(lava -> {
            map.setLava(lava.provider(), lava.startY());
        });

        final BlockBounds spawnRegion = template.getMetadata().getFirstRegionBounds(this.config.spawnRegion());
        if (spawnRegion == null) {
            throw new GameOpenException(Text.literal("Found no spawn region in map template: " + this.config.template()));
        }

        map.setSpawn(new BlockPos(spawnRegion.center()));

        return map;
    }

    private static SpleefLevel buildLevel(MapTemplate template, BlockBounds bounds, Predicate<BlockState> levelBlock) {
        int minX = bounds.min().getX();
        int minZ = bounds.min().getZ();
        int maxX = bounds.max().getX();
        int maxZ = bounds.max().getZ();
        int y = bounds.max().getY();

        SpleefShape.Builder shape = new SpleefShape.Builder(minX, minZ, maxX, maxZ);
        for (BlockPos pos : BlockPos.iterate(minX, y, minZ, maxX, y, maxZ)) {
            final BlockState blockState = template.getBlockState(pos);
            if (levelBlock.test(blockState)) {
                shape.putFill(pos.getX(), pos.getZ());
            }
        }

        return shape.build().toLevel(y);
    }
}
