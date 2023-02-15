package net.punchtree.freebuild.ambientvoting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class NightTimeRunnable extends BukkitRunnable {

    private static final Component skipNightText;
    private static final Component nightProgressBarTitle;
    private static final Component skipNightFailedText;
    private static final Component skipNightSuccessText;
    private Vote currentNightVote;
    private final World world;
    private final PunchTreeFreebuildPlugin ptfbInstance;

    static {
        nightProgressBarTitle = Component
                .text("Type ", NamedTextColor.AQUA)
                .append(Component.text("/vskip night ", NamedTextColor.GOLD))
                .append(Component.text("to vote skip the night.", NamedTextColor.AQUA));

        skipNightFailedText = Component
                .text("The vote to skip the night has ", NamedTextColor.AQUA)
                .append(Component.text("failed", NamedTextColor.RED))
                .append(Component.text("!", NamedTextColor.AQUA));

        skipNightSuccessText = Component
                .text("The vote to skip the night has ", NamedTextColor.AQUA)
                .append(Component.text("succeeded", NamedTextColor.GREEN))
                .append(Component.text("!", NamedTextColor.AQUA));

        skipNightText = Component
                .text("The sun has begun to set across the land!\n", NamedTextColor.AQUA)
                .append(Component.text("Type or click ", NamedTextColor.AQUA))
                .append(Component.text("[/vskip night] ", NamedTextColor.GOLD))
                .append(Component.text("to vote skip the night.", NamedTextColor.AQUA))
                .clickEvent(ClickEvent.runCommand("/vskip night"));
    }

    public NightTimeRunnable(World world) {
        this.world = world;
        this.ptfbInstance = PunchTreeFreebuildPlugin.getInstance();
        this.currentNightVote = createVote();
    }

    @Override
    public void run() {
        currentNightVote = createVote();
        currentNightVote.runTaskTimer(ptfbInstance, 20L, 20L);
    }

    public Vote getCurrentNightVote() {
        return currentNightVote;
    }

    private boolean shouldCancel() {
        return world.getTime() < 13000L;
    }

    public void scheduleRepeatingTaskForTime(long timeInTicks) {
        runTaskTimer(ptfbInstance, calcDelay(timeInTicks), 24000L);
    }

    private long calcDelay(long startTick) {
        return (startTick - world.getTime()) >= 0 ? startTick - world.getTime() : (startTick - world.getTime()) + 24000L;
    }

    private Vote createVote() {
        return new  Vote(
                skipNightText,
                skipNightSuccessText,
                skipNightFailedText,
                nightProgressBarTitle,
                0.6f,
                voteResult -> {
                    if(voteResult) {
                        world.setTime(0L);
                        ptfbInstance.setNightTimeRunnable(new NightTimeRunnable(world), 13000L);
                    }
                },
                this::shouldCancel);
    }
}
