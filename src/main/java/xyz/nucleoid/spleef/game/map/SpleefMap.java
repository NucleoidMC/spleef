package xyz.nucleoid.spleef.game.map;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.util.BlockBounds;
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

    public void tickDecay(ServerWorld world) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        // Remove decayed blocks from previous ticks
        ObjectIterator<Long2IntMap.Entry> iterator = Long2IntMaps.fastIterator(this.decayPositions);
        while (iterator.hasNext()) {
            Long2IntMap.Entry entry = iterator.next();
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
            SpleefShape levelShape = this.levels.get(level).shape;
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
        for (ServerPlayerEntity player : world.getPlayers()) {
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
            Level level = this.levels.get(i);
            if (player.getY() >= level.y) {
                return i;
            }
        }
        return -1;
    }

    private void deleteLevel(ServerWorld world, Level level) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        int y = level.y;

        level.shape.forEachFill((x, z) -> {
            mutablePos.set(x, y, z);
            world.setBlockState(mutablePos, Blocks.AIR.getDefaultState());
        });
    }

    public BlockPos getSpawn() {
        return this.spawn;
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
