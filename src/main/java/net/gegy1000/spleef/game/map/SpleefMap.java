package net.gegy1000.spleef.game.map;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.gegy1000.plasmid.game.map.template.MapTemplate;
import net.gegy1000.plasmid.game.map.template.TemplateChunkGenerator;
import net.gegy1000.plasmid.util.BlockBounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SpleefMap {
    private final MapTemplate template;
    public final Set<BlockState> providedFloors;

    private final List<BlockBounds> levels = new ArrayList<>();
    private int topLevel;

    private final Long2IntMap decayPositions = new Long2IntOpenHashMap();

    private BlockPos spawn = BlockPos.ORIGIN;

    public SpleefMap(MapTemplate template, Set<BlockState> providedFloors) {
        this.template = template;
        this.providedFloors = providedFloors;
    }

    public void addLevel(BlockBounds bounds) {
        this.levels.add(bounds);
        this.topLevel = this.levels.size() - 1;
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

    public void tryBeginDecayAt(ServerWorld world, BlockPos pos, int timer) {
        if (this.providedFloors.contains(world.getBlockState(pos))) {
            this.decayPositions.putIfAbsent(pos.asLong(), timer);
        }
    }

    public void tryDropLevel(ServerWorld world) {
        if (this.topLevel < 0) {
            return;
        }

        BlockBounds level = this.levels.get(this.topLevel);
        this.deleteLevel(world, level);

        this.topLevel--;
    }

    private void deleteLevel(ServerWorld world, BlockBounds level) {
        for (BlockPos pos : level.iterate()) {
            BlockState state = world.getBlockState(pos);
            if (this.providedFloors.contains(state)) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }
    }

    public BlockPos getSpawn() {
        return this.spawn;
    }

    public ChunkGenerator asGenerator() {
        return new TemplateChunkGenerator(this.template, BlockPos.ORIGIN);
    }
}
