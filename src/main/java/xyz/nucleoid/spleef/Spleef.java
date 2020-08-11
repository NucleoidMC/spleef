package xyz.nucleoid.spleef;

import net.fabricmc.api.ModInitializer;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.spleef.game.SpleefConfig;
import xyz.nucleoid.spleef.game.SpleefWaiting;
import xyz.nucleoid.spleef.game.map.shape.MapShapes;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Spleef implements ModInitializer {
    public static final String ID = "spleef";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final GameType<SpleefConfig> TYPE = GameType.register(
            new Identifier(Spleef.ID, "spleef"),
            SpleefWaiting::open,
            SpleefConfig.CODEC
    );

    @Override
    public void onInitialize() {
        MapShapes.initialize();
    }
}
