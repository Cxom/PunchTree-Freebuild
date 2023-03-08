package net.punchtree.freebuild.ambientvoting;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.punchtree.freebuild.afk.RosterManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Vote extends BukkitRunnable {

    private final Component startMessage;
    private final Component successMessage;
    private final Component failureMessage;
    private final Component progressBarTitle;
    private final BossBar progressBar;
    private final Consumer<Boolean> onVoteEnd;
    private final Supplier<Boolean> isCancelled;
    private Audience currentAudience;
    private final float requiredVotePercentage;
    private final HashSet<UUID> activeVoters = new HashSet<>();

    private static final Component VOTE_CAST_MESSAGE = Component
            .text("Your vote has been cast!", NamedTextColor.AQUA);

    private static final Component VOTE_ALREADY_CAST_MESSAGE = Component
            .text("Your vote has already been cast!", NamedTextColor.RED);

    private static final float PROGRESS_DECREMENT = 0.05f;

    public Vote(Component startMessage, Component successMessage, Component failureMessage, Component progressBarTitle,
                float requiredVotePercentage, Consumer<Boolean> onVoteEnd, Supplier<Boolean> isCancelled) {
        this.startMessage = startMessage;
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
        this.progressBarTitle = progressBarTitle;
        this.requiredVotePercentage = requiredVotePercentage;
        this.progressBar = BossBar.bossBar(createProgressBarComponent(), 1.0f, BossBar.Color.GREEN, BossBar.Overlay.NOTCHED_20);
        this.onVoteEnd = onVoteEnd;
        this.isCancelled = isCancelled;
        this.currentAudience = Audience.audience(Bukkit.getOnlinePlayers());
    }

    @Override
    public void run() {
        currentAudience = Audience.audience(Bukkit.getOnlinePlayers());

        // Cancel the vote if necessary
        if (isCancelled.get()) {
            this.cancel();
            currentAudience.hideBossBar(progressBar);
            return;
        }

        // End the vote if the progress is 0 or the required votes have been cast
        if (progressBar.progress() == 0.0f || activeVoters.size() >= calculateRequiredVotes()) {
            boolean votePassed = activeVoters.size() >= calculateRequiredVotes();
            currentAudience.sendMessage(votePassed ? successMessage : failureMessage);
            onVoteEnd.accept(votePassed);
            currentAudience.hideBossBar(progressBar);
            this.cancel();
            return;
        }

        // Decrement the progress and update the boss bar
        progressBar.progress(Math.max(progressBar.progress() - PROGRESS_DECREMENT, 0.0f));
        progressBar.name(createProgressBarComponent());
        currentAudience.showBossBar(progressBar);
    }


    @Override
    public synchronized @NotNull BukkitTask runTaskTimer(@NotNull Plugin plugin, long delay, long period)
            throws IllegalArgumentException, IllegalStateException {
        currentAudience.sendMessage(startMessage);
        currentAudience.showBossBar(progressBar);
        currentAudience.playSound(Sound.sound(Key.key("minecraft:block.note_block.chime"), Sound.Source.MASTER, 1.0f, 1.5f));
        return super.runTaskTimer(plugin, delay, period);
    }

    private int calculateRequiredVotes() {
        int totalPlayers = Bukkit.getOnlinePlayers().size();
        int afkPlayers = RosterManager.getRoster("afk").getRoster().size();
        int activePlayers = totalPlayers - afkPlayers;

        return (int) Math.max(activePlayers * requiredVotePercentage, 1);
    }

    private Component createProgressBarComponent() {
        int votesNeeded = calculateRequiredVotes();
        int votesCast = activeVoters.size();

        Component voteCount = Component.text(votesCast + "/" + votesNeeded, NamedTextColor.GOLD);

        return progressBarTitle
                .append(Component.text(" : "))
                .append(voteCount);
    }

    public void castVote(Player voter) {
        if (this.isCancelled.get()) {
            throw new IllegalStateException("Vote is cancelled");
        }

        if (activeVoters.contains(voter.getUniqueId())) {
            voter.sendMessage(VOTE_ALREADY_CAST_MESSAGE);
            return;
        }
        activeVoters.add(voter.getUniqueId());
        voter.sendMessage(VOTE_CAST_MESSAGE);
    }

    public void removeVote(Player voter) {
        activeVoters.remove(voter.getUniqueId());
    }
}