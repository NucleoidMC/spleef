package net.gegy1000.spleef.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gegy1000.plasmid.game.config.GameConfig;
import net.gegy1000.plasmid.game.config.GameMapConfig;
import net.gegy1000.plasmid.game.config.PlayerConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class SpleefConfig implements GameConfig {
    public static final Codec<SpleefConfig> CODEC = RecordCodecBuilder.create(instance -> {
        Codec<GameMapConfig<SpleefConfig>> mapCodec = GameMapConfig.codec();

        return instance.group(
                mapCodec.fieldOf("map").forGetter(SpleefConfig::getMapConfig),
                PlayerConfig.CODEC.fieldOf("players").forGetter(SpleefConfig::getPlayerConfig),
                BlockState.CODEC.optionalFieldOf("wall", Blocks.STONE_BRICKS.getDefaultState()).forGetter(SpleefConfig::getWall),
                BlockState.CODEC.optionalFieldOf("floor", Blocks.SNOW_BLOCK.getDefaultState()).forGetter(SpleefConfig::getFloor),
                ItemStack.CODEC.optionalFieldOf("tool", new ItemStack(Items.DIAMOND_SHOVEL)).forGetter(SpleefConfig::getTool),
                Codec.LONG.optionalFieldOf("level_break_interval", 20L * 60).forGetter(SpleefConfig::getLevelBreakInterval),
                Codec.BOOL.optionalFieldOf("decay", false).forGetter(SpleefConfig::getDecay)
        ).apply(instance, SpleefConfig::new);
    });

    private final GameMapConfig<SpleefConfig> mapConfig;
    private final PlayerConfig playerConfig;

    private final BlockState wall;
    private final BlockState floor;
    private final ItemStack tool;

    private final long levelBreakInterval;
    private final boolean decay;

    public SpleefConfig(
            GameMapConfig<SpleefConfig> mapConfig,
            PlayerConfig playerConfig,
            BlockState wall,
            BlockState floor,
            ItemStack tool,
            long levelBreakInterval,
            boolean decay
    ) {
        this.mapConfig = mapConfig;
        this.playerConfig = playerConfig;
        this.wall = wall;
        this.floor = floor;
        this.tool = tool;
        this.levelBreakInterval = levelBreakInterval;
        this.decay = decay;
    }

    public GameMapConfig<SpleefConfig> getMapConfig() {
        return this.mapConfig;
    }

    public PlayerConfig getPlayerConfig() {
        return this.playerConfig;
    }

    public BlockState getWall() {
        return this.wall;
    }

    public BlockState getFloor() {
        return this.floor;
    }

    public ItemStack getTool() {
        return this.tool.copy();
    }

    public long getLevelBreakInterval() {
        return this.levelBreakInterval;
    }
    
    public boolean getDecay() {
        return this.decay;
    }
}
