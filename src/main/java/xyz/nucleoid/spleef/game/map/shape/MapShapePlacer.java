package xyz.nucleoid.spleef.game.map.shape;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import xyz.nucleoid.plasmid.map.template.MapTemplate;

import java.util.Random;
import java.util.Set;

public final class MapShapePlacer extends ShapePlacer {
    private final MapTemplate template;
    private final Set<BlockState> usedStates = new ReferenceOpenHashSet<>();
    
    public MapShapePlacer(MapTemplate template, BlockStateProvider provider, Random random) {
        super(provider, random);
        this.template = template;
    }

    @Override
    public void set(BlockPos pos) {
        BlockState state = this.provider.getBlockState(this.random, pos);
        this.usedStates.add(state);

        this.template.setBlockState(pos, state);
    }

    public Set<BlockState> getUsedStates() {
        return this.usedStates;
    }
}
