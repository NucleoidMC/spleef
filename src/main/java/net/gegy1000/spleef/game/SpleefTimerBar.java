package net.gegy1000.spleef.game;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public final class SpleefTimerBar implements AutoCloseable {
    private final ServerBossBar bar;

    public SpleefTimerBar() {
        LiteralText title = new LiteralText("Dropping in...");

        this.bar = new ServerBossBar(title, BossBar.Color.GREEN, BossBar.Style.NOTCHED_10);
        this.bar.setDarkenSky(false);
        this.bar.setDragonMusic(false);
        this.bar.setThickenFog(false);
    }

    public void update(long ticksUntilDrop, long totalTicksUntilDrop) {
        if (ticksUntilDrop % 20 == 0) {
            this.bar.setName(this.getText(ticksUntilDrop));
            this.bar.setPercent((float) ticksUntilDrop / totalTicksUntilDrop);
        }
    }

    public void addPlayer(ServerPlayerEntity player) {
        this.bar.addPlayer(player);
    }

    public void removePlayer(ServerPlayerEntity player) {
        this.bar.removePlayer(player);
    }

    private Text getText(long ticksUntilDrop) {
        long secondsUntilDrop = ticksUntilDrop / 20;

        long minutes = secondsUntilDrop / 60;
        long seconds = secondsUntilDrop % 60;
        String time = String.format("%02d:%02d", minutes, seconds);

        return new LiteralText("Dropping in: " + time + "...");
    }

    @Override
    public void close() {
        this.bar.clearPlayers();
        this.bar.setVisible(false);
    }
}
