package xyz.nucleoid.spleef;

import net.fabricmc.api.ModInitializer;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.spleef.game.SpleefConfig;
import xyz.nucleoid.spleef.game.SpleefWaiting;
import xyz.nucleoid.spleef.game.map.shape.renderer.*;

public final class Spleef implements ModInitializer {
    public static final String ID = "spleef";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    private static final Identifier ELIMINATES_PLAYERS_ID = Identifier.fromNamespaceAndPath(Spleef.ID, "eliminates_players");
    public static final TagKey<DamageType> ELIMINATES_PLAYERS = TagKey.create(Registries.DAMAGE_TYPE, ELIMINATES_PLAYERS_ID);

    @Override
    public void onInitialize() {
        GameType.register(
                Identifier.fromNamespaceAndPath(Spleef.ID, "spleef"),
                SpleefConfig.CODEC,
                SpleefWaiting::open
        );

        MapShapeRenderer.REGISTRY.register(Identifier.fromNamespaceAndPath(Spleef.ID, "circle"), CircleShapeRenderer.CODEC);
        MapShapeRenderer.REGISTRY.register(Identifier.fromNamespaceAndPath(Spleef.ID, "square"), SquareShapeRenderer.CODEC);
        MapShapeRenderer.REGISTRY.register(Identifier.fromNamespaceAndPath(Spleef.ID, "sierpinski_carpet"), SierpinskiCarpetShapeRenderer.CODEC);
        MapShapeRenderer.REGISTRY.register(Identifier.fromNamespaceAndPath(Spleef.ID, "pattern"), PatternShapeRenderer.CODEC);
    }
}
