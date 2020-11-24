package xyz.nucleoid.spleef.game;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.widget.BossBarWidget;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;

public final class SpleefTimerBar implements AutoCloseable {
    private final BossBarWidget widget;

    SpleefTimerBar(BossBarWidget widget) {
        this.widget = widget;
    }

    static SpleefTimerBar create(GlobalWidgets widgets) {
        LiteralText title = new LiteralText("Dropping in...");
        return new SpleefTimerBar(widgets.addBossBar(title, BossBar.Color.GREEN, BossBar.Style.NOTCHED_10));
    }

    public void update(long ticksUntilDrop, long totalTicksUntilDrop) {
        if (ticksUntilDrop % 20 == 0) {
            this.widget.setTitle(this.getText(ticksUntilDrop));
            this.widget.setProgress((float) ticksUntilDrop / totalTicksUntilDrop);
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
        this.widget.close();
    }
}
