package net.gegy1000.spleef;

import net.fabricmc.api.ModInitializer;
import net.gegy1000.plasmid.game.GameType;
import net.gegy1000.plasmid.game.config.GameMapConfig;
import net.gegy1000.plasmid.game.map.provider.MapProvider;
import net.gegy1000.spleef.game.SpleefConfig;
import net.gegy1000.spleef.game.SpleefWaiting;
import net.gegy1000.spleef.game.map.SpleefMapProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Spleef implements ModInitializer {
    public static final String ID = "spleef";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final GameType<SpleefConfig> TYPE = GameType.register(
            new Identifier(Spleef.ID, "spleef"),
            (server, config) -> {
                GameMapConfig<SpleefConfig> mapConfig = config.getMapConfig();
                RegistryKey<World> dimension = mapConfig.getDimension();
                BlockPos origin = mapConfig.getOrigin();
                ServerWorld world = server.getWorld(dimension);

                return mapConfig.getProvider().createAt(world, origin, config).thenApply(map -> {
                    return SpleefWaiting.open(map, config);
                });
            },
            SpleefConfig.CODEC
    );

    @Override
    public void onInitialize() {
        MapProvider.REGISTRY.register(new Identifier(Spleef.ID, "spleef"), SpleefMapProvider.CODEC);
    }
}
