package xyz.nucleoid.spleef.game.map;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;
import xyz.nucleoid.spleef.game.LavaRiseConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SpleefMap {
    private final MapTemplate template;
    public final Set<BlockState> providedFloors = new ReferenceOpenHashSet<>();

    private final List<SpleefLevel> levels = new ArrayList<>();
    private final Int2ObjectMap<SpleefLevel> yToLevel = new Int2ObjectOpenHashMap<>();

    private final int ceilingY;
    private int topLevel;

    private final Long2IntMap decayPositions = new Long2IntOpenHashMap();

    @Nullable
    private BlockStateProvider lavaProvider;
    private int lavaMinY;

    private int lavaHeight = -1;
    private long lastLavaRise;

    private BlockPos spawn = BlockPos.ORIGIN;

    public SpleefMap(MapTemplate template, int ceilingY) {
        this.template = template;

        this.ceilingY = ceilingY;
    }

    public void addLevel(SpleefLevel level) {
        int index = this.levels.size();
        this.levels.add(level);
        this.yToLevel.put(level.y(), level);
        this.topLevel = index;
    }

    public void setSpawn(BlockPos pos) {
        this.spawn = pos;
    }

    public void setLava(BlockStateProvider lavaProvider, int lavaMinY) {
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
        var level = this.yToLevel.get(pos.getY());
        if (level != null && level.contains(pos)) {
            this.decayPositions.putIfAbsent(pos.asLong(), timer);
        }
    }

    public void tryDropLevel(ServerWorld world) {
        if (this.topLevel < 0) {
            return;
        }

        int maxPlayerLevel = this.getMaxPlayerLevel(world);
        int nextLevel = Math.min(this.topLevel - 1, maxPlayerLevel);

        for (int i = this.topLevel; i > nextLevel; i--) {
            SpleefLevel level = this.levels.get(i);
            this.deleteLevel(world, level);
        }

        this.topLevel = nextLevel;
    }

    public int getMaxPlayerLevel(ServerWorld world) {
        int maxPlayerLevel = 0;
        for (var player : world.getPlayers()) {
            if (player.isSpectator()) continue;

            int playerLevel = this.getLevelBelow(player.getBlockY());
            if (playerLevel > maxPlayerLevel) {
                maxPlayerLevel = playerLevel;
            }
        }

        return maxPlayerLevel;
    }

    private int getLevelBelow(int y) {
        for (int i = this.topLevel; i >= 0; i--) {
            var level = this.levels.get(i);
            if (y > level.y()) {
                return i;
            }
        }
        return 0;
    }

    private void deleteLevel(ServerWorld world, SpleefLevel level) {
        level.forEach(pos -> world.removeBlock(pos, false));
    }

    public void tickLavaRise(ServerWorld world, long time, LavaRiseConfig config) {
        if (this.topLevel >= 0) {
            return;
        }

        if (this.lavaProvider == null || time - this.lastLavaRise < config.ticksPerLevel()) {
            return;
        }

        this.lastLavaRise = time;

        int lavaHeight = ++this.lavaHeight;

        if (lavaHeight >= config.maximumHeight().orElse(ceilingY - 2)) {
            return;
        }

        int y = lavaHeight + this.lavaMinY;

        var mutablePos = new BlockPos.Mutable();
        var random = world.random;

        int levelIndex = this.getLevelBelow(y);
        var level = this.levels.get(levelIndex);

        level.forEach(pos -> {
            mutablePos.set(pos.getX(), y, pos.getZ());
            world.setBlockState(mutablePos, this.lavaProvider.get(random, mutablePos));
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
}
