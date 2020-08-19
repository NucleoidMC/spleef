package xyz.nucleoid.spleef.game;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.widget.BossBarWidget;

public final class SpleefTimerBar implements AutoCloseable {
    private final BossBarWidget bar;

    SpleefTimerBar(GameWorld gameWorld) {
        LiteralText title = new LiteralText("Dropping in...");
        this.bar = BossBarWidget.open(gameWorld.getPlayerSet(), title, BossBar.Color.GREEN, BossBar.Style.NOTCHED_10);
    }

    public void update(long ticksUntilDrop, long totalTicksUntilDrop) {
        if (ticksUntilDrop % 20 == 0) {
            this.bar.setTitle(this.getText(ticksUntilDrop));
            this.bar.setProgress((float) ticksUntilDrop / totalTicksUntilDrop);
        }
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
        this.bar.close();
    }
}
