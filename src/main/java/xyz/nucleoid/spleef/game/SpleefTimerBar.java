package xyz.nucleoid.spleef.game;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.common.widget.BossBarWidget;

public final class SpleefTimerBar {
    private final BossBarWidget widget;

    SpleefTimerBar(BossBarWidget widget) {
        this.widget = widget;
    }

    static SpleefTimerBar create(GlobalWidgets widgets) {
        var title = getBarTitle(Text.translatable("text.spleef.bar.dropping.none"), Formatting.GREEN);
        return new SpleefTimerBar(widgets.addBossBar(title, BossBar.Color.GREEN, BossBar.Style.NOTCHED_10));
    }

    public void update(long ticksUntilDrop, long totalTicksUntilDrop) {
        if (ticksUntilDrop % 20 == 0) {
            this.widget.setTitle(this.getDroppingText(ticksUntilDrop));
            this.widget.setProgress((float) ticksUntilDrop / totalTicksUntilDrop);
        }
    }

    public void setBarLava(){
        this.widget.setTitle(getBarTitle(Text.translatable("game.spleef.lava.msg"), Formatting.RED));
        this.widget.setStyle(BossBar.Color.RED, BossBar.Style.NOTCHED_10);
        this.widget.setProgress(1f);
    }

    private Text getDroppingText(long ticksUntilDrop) {
        long secondsUntilDrop = ticksUntilDrop / 20;

        long minutes = secondsUntilDrop / 60;
        long seconds = secondsUntilDrop % 60;
        var time = String.format("%02d:%02d", minutes, seconds);

        return getBarTitle(Text.translatable("text.spleef.bar.dropping", time), Formatting.GREEN);
    }

    private static Text getBarTitle(Text customText, Formatting color) {
        var gameName = Text.translatable("gameType.spleef.spleef").formatted(Formatting.BOLD);
        return Text.empty().append(gameName).append(" - ").append(customText).formatted(color);
    }
}
