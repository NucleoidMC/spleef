package xyz.nucleoid.spleef.game.map.shape;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import xyz.nucleoid.map_templates.MapTemplate;

import java.util.Set;

public final class ShapePlacer {
    private final MapTemplate template;
    private final BlockStateProvider provider;
    private final RandomSource random;

    private final Set<BlockState> usedStates = new ReferenceOpenHashSet<>();

    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public ShapePlacer(MapTemplate template, BlockStateProvider provider, RandomSource random) {
        this.template = template;
        this.provider = provider;
        this.random = random;
    }

    public void fill(SpleefShape shape, int minY, int maxY) {
        shape.forEachFill((x, z) -> this.setStack(minY, maxY, x, z));
    }

    public void outline(SpleefShape shape, int minY, int maxY) {
        shape.forEachOutline((x, z) -> this.setStack(minY, maxY, x, z));
    }

    private void setStack(int minY, int maxY, int x, int z) {
        this.mutablePos.set(x, 0, z);
        for (int y = minY; y <= maxY; y++) {
            this.mutablePos.setY(y);
            this.set(this.mutablePos);
        }
    }

    private void set(BlockPos pos) {
        var state = this.provider.getState(this.random, pos);
        this.usedStates.add(state);

        this.template.setBlockState(pos, state);
    }

    public Set<BlockState> getUsedStates() {
        return this.usedStates;
    }
}
