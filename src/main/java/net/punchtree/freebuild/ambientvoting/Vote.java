package net.punchtree.freebuild.ambientvoting;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;

public class Vote extends BukkitRunnable {
    private final Component startMessage;
    private final Component successMessage;
    private final Component failureMessage;
    private final Component progressBarTitle;
    private final BossBar progressBar;
    private final Consumer<Boolean> onVoteEnd;
    private Audience currentAudience;
    private final float requiredVotePercentage;
    private final HashSet<UUID> activeVoters = new HashSet<>();

    private static final Component voteCastMessage = Component
            .text("Your vote has been cast!", NamedTextColor.AQUA);

    private static final Component voteAlreadyCastMessage = Component
            .text("Your vote has already been cast!", NamedTextColor.RED);

    private static final Component noActiveVoteMessage = Component
            .text("There's no vote happening right now!", NamedTextColor.RED);

    public Vote(Component startMessage, Component successMessage, Component failureMessage, Component progressBarTitle, float requiredVotePercentage, Consumer<Boolean> onVoteEnd) {

        this.startMessage = startMessage;
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
        this.progressBarTitle = progressBarTitle;
        this.requiredVotePercentage = requiredVotePercentage;
        this.progressBar = BossBar.bossBar(appendVoteCount(), 1.0f, BossBar.Color.GREEN, BossBar.Overlay.NOTCHED_20);
        this.onVoteEnd = onVoteEnd;
        this.currentAudience = Audience.audience(Bukkit.getOnlinePlayers());
    }

    @Override
    public void run() {
        currentAudience = Audience.audience(Bukkit.getOnlinePlayers());
        if (progressBar.progress() == 0.0f || activeVoters.size() >= calculateRequiredVotes()) {

            if (activeVoters.size() < calculateRequiredVotes()) {
                currentAudience.sendMessage(failureMessage);
                onVoteEnd.accept(false);
            } else {
                currentAudience.sendMessage(successMessage);
                onVoteEnd.accept(true);
            }
            currentAudience.hideBossBar(progressBar);
            this.cancel();
            return;
        }
        progressBar.progress(Math.max(progressBar.progress() - 0.05f, 0.0f));
        progressBar.name(appendVoteCount());
        currentAudience.showBossBar(progressBar);
    }

    @Override @NotNull
    public synchronized BukkitTask runTaskTimer(@NotNull Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        currentAudience.sendMessage(startMessage);
        currentAudience.showBossBar(progressBar);
        return super.runTaskTimer(plugin, delay, period);
    }

    private int calculateRequiredVotes() {
        return (int) (Math.max(Bukkit.getOnlinePlayers().size() * requiredVotePercentage, 1f));
    }

    private Component appendVoteCount() {
        return progressBarTitle
                .append(Component.text(" : ")
                .append(Component.text(
                        activeVoters.size()
                        + "/"
                        + calculateRequiredVotes()
                        , NamedTextColor.GOLD)));
    }

    public void castVote(Player voter) {
        if(this.isCancelled()) {
            voter.sendMessage(noActiveVoteMessage);
            return;
        }

        if(activeVoters.contains(voter.getUniqueId())) {
            voter.sendMessage(voteAlreadyCastMessage);
            return;
        }
        activeVoters.add(voter.getUniqueId());
        voter.sendMessage(voteCastMessage);
    }

    public void removeVote(Player voter) {
        if(this.isCancelled()) return;

        activeVoters.remove(voter.getUniqueId());
    }
}