package xyz.nucleoid.spleef.game;

import net.minecraft.world.BossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.widget.BossBarWidget;

public final class SpleefTimerBar {
    private static final Component NONE_TITLE = getBarTitle(Component.translatable("text.spleef.bar.dropping.none"), ChatFormatting.GREEN);
    private static final Component LAVA_TITLE = getBarTitle(Component.translatable("game.spleef.lava.msg"), ChatFormatting.RED);

    private final BossBarWidget widget;

    SpleefTimerBar(BossBarWidget widget) {
        this.widget = widget;
    }

    static SpleefTimerBar create(GlobalWidgets widgets) {
        return new SpleefTimerBar(widgets.addBossBar(NONE_TITLE, BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.NOTCHED_10));
    }

    public void update(long ticksUntilDrop, long totalTicksUntilDrop) {
        if (ticksUntilDrop % 20 == 0) {
            this.widget.setTitle(this.getDroppingText(ticksUntilDrop));
            this.widget.setProgress((float) ticksUntilDrop / totalTicksUntilDrop);
        }
    }

    public void setBarNone() {
        this.widget.setTitle(NONE_TITLE);
        this.widget.setProgress(1f);
    }

    public void setBarLava(){
        this.widget.setTitle(LAVA_TITLE);
        this.widget.setStyle(BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
        this.widget.setProgress(1f);
    }

    private Component getDroppingText(long ticksUntilDrop) {
        long secondsUntilDrop = ticksUntilDrop / 20;

        long minutes = secondsUntilDrop / 60;
        long seconds = secondsUntilDrop % 60;
        var time = String.format("%02d:%02d", minutes, seconds);

        return getBarTitle(Component.translatable("text.spleef.bar.dropping", time), ChatFormatting.GREEN);
    }

    private static Component getBarTitle(Component customText, ChatFormatting color) {
        var gameName = Component.translatable("gameType.spleef.spleef").withStyle(ChatFormatting.BOLD);
        return Component.empty().append(gameName).append(" - ").append(customText).withStyle(color);
    }
}
