package xyz.nucleoid.spleef.game;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.common.widget.BossBarWidget;

public final class SpleefTimerBar {
    private final BossBarWidget widget;

    SpleefTimerBar(BossBarWidget widget) {
        this.widget = widget;
    }

    static SpleefTimerBar create(GlobalWidgets widgets) {
        var title = getBarTitle(new TranslatableText("text.spleef.bar.dropping.none"));
        return new SpleefTimerBar(widgets.addBossBar(title, BossBar.Color.GREEN, BossBar.Style.NOTCHED_10));
    }

    public void update(long ticksUntilDrop, long totalTicksUntilDrop) {
        if (ticksUntilDrop % 20 == 0) {
            this.widget.setTitle(this.getDroppingText(ticksUntilDrop));
            this.widget.setProgress((float) ticksUntilDrop / totalTicksUntilDrop);
        }
    }

    public void setBarLava(){
        var gameName = new TranslatableText("game.spleef.spleef").formatted(Formatting.BOLD);
        var lavaMsg = new TranslatableText("game.spleef.lava.msg");
        this.widget.setTitle(new LiteralText("").append(gameName).append(" - ").append(lavaMsg).formatted(Formatting.RED));
        this.widget.setStyle(BossBar.Color.RED, BossBar.Style.NOTCHED_10);
        this.widget.setProgress(1f);
    }

    private Text getDroppingText(long ticksUntilDrop) {
        long secondsUntilDrop = ticksUntilDrop / 20;

        long minutes = secondsUntilDrop / 60;
        long seconds = secondsUntilDrop % 60;
        var time = String.format("%02d:%02d", minutes, seconds);

        return getBarTitle(new TranslatableText("text.spleef.bar.dropping", time));
    }

    private static Text getBarTitle(Text customText) {
        var gameName = new TranslatableText("gameType.spleef.spleef").formatted(Formatting.BOLD);
        return new LiteralText("").append(gameName).append(" - ").append(customText).formatted(Formatting.GREEN);
    }
}
