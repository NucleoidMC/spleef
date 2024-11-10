package xyz.nucleoid.spleef.game.action;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.spleef.game.SpleefActive;

public record BlockActionContext(ServerPlayerEntity self, PlayerSet players, SpleefActive game) {}
