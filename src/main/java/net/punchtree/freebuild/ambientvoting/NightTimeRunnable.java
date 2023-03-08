package net.punchtree.freebuild.ambientvoting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class NightTimeRunnable extends BukkitRunnable {

    private static final Component SKIP_NIGHT_TEXT = Component
            .text("The sun has begun to set across the land!\n", NamedTextColor.AQUA)
            .append(Component.text("Type or click ", NamedTextColor.AQUA))
            .append(Component.text("[/vskip night] ", NamedTextColor.GOLD))
            .append(Component.text("to vote skip the night.", NamedTextColor.AQUA));

    private static final Component NIGHT_PROGRESS_BAR_TITLE = Component
            .text("Type ", NamedTextColor.AQUA)
            .append(Component.text("/vskip night ", NamedTextColor.GOLD))
            .append(Component.text("to vote skip the night.", NamedTextColor.AQUA));

    private static final Component SKIP_NIGHT_FAILED_TEXT = Component
            .text("The vote to skip the night has ", NamedTextColor.AQUA)
            .append(Component.text("failed", NamedTextColor.RED))
            .append(Component.text("!", NamedTextColor.AQUA));

    private static final Component SKIP_NIGHT_SUCCESS_TEXT = Component
            .text("The vote to skip the night has ", NamedTextColor.AQUA)
            .append(Component.text("succeeded", NamedTextColor.GREEN))
            .append(Component.text("!", NamedTextColor.AQUA));

    private final World world;
    private final PunchTreeFreebuildPlugin plugin;
    private Vote currentNightVote;

    private static final long TICKS_PER_DAY = 24000L;
    private static final long START_OF_NIGHT_TICK = 13000L;

    public NightTimeRunnable(World world) {
        this.world = world;
        this.plugin = PunchTreeFreebuildPlugin.getInstance();
        this.currentNightVote = createVote();
    }

    @Override
    public void run() {
        currentNightVote = createVote();
        currentNightVote.runTaskTimer(plugin, 20L, 20L);
    }

    public Vote getCurrentNightVote() {
        return currentNightVote;
    }

    private boolean shouldCancel() {
        return world.getTime() < START_OF_NIGHT_TICK;
    }

    public void scheduleRepeatingTaskForTime(long timeInTicks) {
        runTaskTimer(plugin, calcDelay(timeInTicks), TICKS_PER_DAY);
    }

    private long calcDelay(long startTick) {
        long delay = startTick - world.getTime();
        if (delay < 0) {
            delay += TICKS_PER_DAY;
        }
        return delay;
    }

    private Vote createVote() {
        return new Vote(
                SKIP_NIGHT_TEXT,
                SKIP_NIGHT_SUCCESS_TEXT,
                SKIP_NIGHT_FAILED_TEXT,
                NIGHT_PROGRESS_BAR_TITLE,
                0.6f,
                voteResult -> {
                    if (voteResult) {
                        world.setTime(0L);
                        plugin.setNightTimeRunnable(new NightTimeRunnable(world), START_OF_NIGHT_TICK);
                    }
                },
                this::shouldCancel);
    }
}
