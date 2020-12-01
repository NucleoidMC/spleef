package xyz.nucleoid.spleef.game.map.shape;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public final class WorldShapePlacer extends CustomOffsetShapePlacer {
    private final World world;

    public WorldShapePlacer(int offsetX, int offsetZ, World world, BlockStateProvider provider) {
        super(offsetX, offsetZ, provider, world.getRandom());
        this.world = world;
    }

    @Override
    public void set(BlockPos pos) {
        BlockState state = this.provider.getBlockState(this.random, pos);
        this.world.setBlockState(pos, state);
    }
}
