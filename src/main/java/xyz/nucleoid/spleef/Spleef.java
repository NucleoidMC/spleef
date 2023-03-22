package xyz.nucleoid.spleef;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.spleef.game.SpleefConfig;
import xyz.nucleoid.spleef.game.SpleefWaiting;
import xyz.nucleoid.spleef.game.map.shape.renderer.*;

public final class Spleef implements ModInitializer {
    public static final String ID = "spleef";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    private static final Identifier ELIMINATES_PLAYERS_ID = new Identifier(Spleef.ID, "eliminates_players");
    public static final TagKey<DamageType> ELIMINATES_PLAYERS = TagKey.of(RegistryKeys.DAMAGE_TYPE, ELIMINATES_PLAYERS_ID);

    @Override
    public void onInitialize() {
        GameType.register(
                new Identifier(Spleef.ID, "spleef"),
                SpleefConfig.CODEC,
                SpleefWaiting::open
        );

        MapShapeRenderer.REGISTRY.register(new Identifier(Spleef.ID, "circle"), CircleShapeRenderer.CODEC);
        MapShapeRenderer.REGISTRY.register(new Identifier(Spleef.ID, "square"), SquareShapeRenderer.CODEC);
        MapShapeRenderer.REGISTRY.register(new Identifier(Spleef.ID, "sierpinski_carpet"), SierpinskiCarpetShapeRenderer.CODEC);
        MapShapeRenderer.REGISTRY.register(new Identifier(Spleef.ID, "pattern"), PatternShapeRenderer.CODEC);
    }
}
