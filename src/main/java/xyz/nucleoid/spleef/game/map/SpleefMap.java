package xyz.nucleoid.spleef.game.map;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;
import xyz.nucleoid.spleef.game.LavaRiseConfig;
import xyz.nucleoid.spleef.game.map.shape.SpleefShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SpleefMap {
    private final MapTemplate template;
    public final Set<BlockState> providedFloors = new ReferenceOpenHashSet<>();

    private final List<Level> levels = new ArrayList<>();
    private final Int2IntMap yToLevel = new Int2IntOpenHashMap();

    private int topLevel;

    private final Long2IntMap decayPositions = new Long2IntOpenHashMap();

    private SpleefShape lavaShape;
    private BlockStateProvider lavaProvider;
    private int lavaMinY;

    private int lavaHeight = -1;
    private long lastLavaRise;

    private BlockPos spawn = BlockPos.ORIGIN;

    public SpleefMap(MapTemplate template) {
        this.template = template;
        this.yToLevel.defaultReturnValue(-1);
    }

    public void addLevel(SpleefShape shape, int y) {
        this.levels.add(new Level(shape, y));

        int index = this.levels.size() - 1;

        this.yToLevel.put(y, index);
        this.topLevel = index;
    }

    public void setSpawn(BlockPos pos) {
        this.spawn = pos;
    }

    public void setLava(SpleefShape shape, BlockStateProvider lavaProvider, int lavaMinY) {
        this.lavaShape = shape;
        this.lavaProvider = lavaProvider;
        this.lavaMinY = lavaMinY;
    }

    public void tickDecay(ServerWorld world) {
        var mutablePos = new BlockPos.Mutable();

        // Remove decayed blocks from previous ticks
        var iterator = Long2IntMaps.fastIterator(this.decayPositions);
        while (iterator.hasNext()) {
            var entry = iterator.next();
            long pos = entry.getLongKey();
            int ticksLeft = entry.getIntValue();

            if (ticksLeft == 0) {
                mutablePos.set(pos);
                world.breakBlock(mutablePos, false);
                iterator.remove();
            } else {
                entry.setValue(ticksLeft - 1);
            }
        }
    }

    public void tryBeginDecayAt(BlockPos pos, int timer) {
        int level = this.yToLevel.get(pos.getY());
        if (level != -1) {
            var levelShape = this.levels.get(level).shape;
            if (levelShape.isFillAt(pos.getX(), pos.getZ())) {
                this.decayPositions.putIfAbsent(pos.asLong(), timer);
            }
        }
    }

    public void tryDropLevel(ServerWorld world) {
        if (this.topLevel < 0) {
            return;
        }

        int maxPlayerLevel = this.getMaxPlayerLevel(world);
        int nextLevel = Math.min(this.topLevel - 1, maxPlayerLevel);

        for (int i = this.topLevel; i > nextLevel; i--) {
            Level level = this.levels.get(i);
            this.deleteLevel(world, level);
        }

        this.topLevel = nextLevel;
    }

    public int getMaxPlayerLevel(ServerWorld world) {
        int maxPlayerLevel = -1;
        for (var player : world.getPlayers()) {
            if (player.isSpectator()) continue;

            int playerLevel = this.getLevelFor(player);
            if (playerLevel > maxPlayerLevel) {
                maxPlayerLevel = playerLevel;
            }
        }

        return maxPlayerLevel;
    }

    private int getLevelFor(ServerPlayerEntity player) {
        for (int i = this.topLevel; i >= 0; i--) {
            var level = this.levels.get(i);
            if (player.getY() >= level.y) {
                return i;
            }
        }
        return -1;
    }

    private void deleteLevel(ServerWorld world, Level level) {
        var mutablePos = new BlockPos.Mutable();
        int y = level.y;

        level.shape.forEachFill((x, z) -> {
            mutablePos.set(x, y, z);
            world.setBlockState(mutablePos, Blocks.AIR.getDefaultState());
        });
    }

    public void tickLavaRise(ServerWorld world, long time, LavaRiseConfig config) {
        if (this.topLevel >= 0) {
            return;
        }

        if (this.lavaShape == null || time - this.lastLavaRise < config.ticksPerLevel()) {
            return;
        }

        this.lastLavaRise = time;

        int lavaHeight = ++this.lavaHeight;

        int y = lavaHeight + this.lavaMinY;

        var mutablePos = new BlockPos.Mutable();
        var random = world.random;

        this.lavaShape.forEachFill((x, z) -> {
            mutablePos.set(x, y, z);
            world.setBlockState(mutablePos, this.lavaProvider.getBlockState(random, mutablePos));
        });
    }

    public BlockPos getSpawn() {
        return this.spawn;
    }

    public int getTopLevel() {
        return topLevel;
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }

    static class Level {
        final SpleefShape shape;
        final int y;
        final BlockBounds bounds;

        Level(SpleefShape shape, int y) {
            this.shape = shape;
            this.y = y;
            this.bounds = shape.asBounds(y, y);
        }
    }
}
