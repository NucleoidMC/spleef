package net.gegy1000.spleef;

import net.fabricmc.api.ModInitializer;
import net.gegy1000.plasmid.game.GameType;
import net.gegy1000.spleef.game.SpleefConfig;
import net.gegy1000.spleef.game.SpleefWaiting;
import net.gegy1000.spleef.game.map.shape.MapShapes;
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
