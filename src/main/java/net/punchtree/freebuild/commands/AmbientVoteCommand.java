package net.punchtree.freebuild.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import net.punchtree.freebuild.ambientvoting.Vote;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class AmbientVoteCommand implements CommandExecutor, Listener {
    private static final Component skipWeatherText;
    private static final Component weatherBossbarTitle;
    private static final Component skipWeatherFailedText;
    private static final Component skipWeatherSuccessText;
    private static final Component notStormingMessage;
    private Vote activeWeatherVote = null;
    private BukkitTask activeWeatherTask = null;

    private final PunchTreeFreebuildPlugin ptfbInstance = PunchTreeFreebuildPlugin.getInstance();

    static {
        weatherBossbarTitle = Component
                .text("Type ", NamedTextColor.AQUA)
                .append(Component.text("/vskip weather ", NamedTextColor.GOLD))
                .append(Component.text("to vote skip the storm.", NamedTextColor.AQUA));

        skipWeatherFailedText = Component
                .text("The vote to skip the weather has ", NamedTextColor.AQUA)
                .append(Component.text("failed", NamedTextColor.RED))
                .append(Component.text("!\n", NamedTextColor.AQUA))
                .append(Component.text("The storm will continue.", NamedTextColor.AQUA));

        skipWeatherSuccessText = Component
                .text("The vote to skip the weather has ", NamedTextColor.AQUA)
                .append(Component.text("succeeded", NamedTextColor.GREEN))
                .append(Component.text("!\n", NamedTextColor.AQUA))
                .append(Component.text("The storm will pass.", NamedTextColor.AQUA));

        skipWeatherText = Component
                .text("Looks like the weather has taken a turn for the worse!\n", NamedTextColor.AQUA)
                .append(Component.text("Type or click ", NamedTextColor.AQUA))
                .append(Component.text("[/vskip weather] ", NamedTextColor.GOLD))
                .append(Component.text("to vote skip the storm.", NamedTextColor.AQUA))
                .clickEvent(ClickEvent.runCommand("/vskip weather"));

        notStormingMessage = Component
                .text("Its not storming right now!", NamedTextColor.RED);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return true;
        World worldToSkip = player.getWorld();

        switch (label) {
            case "skipweather" -> {
                attemptWeatherSkipVote(player, worldToSkip);
                return true;
            }
            case "skipnight" -> {
                return true;
            }
            default -> {
                if (args.length == 0) return false;

                switch (args[0]) {
                    case "weather", "storm" -> {
                        attemptWeatherSkipVote(player, worldToSkip);
                        return true;
                    }
                    case "night", "time" -> {
                        return true;
                    }
                    default -> {
                        return false;
                    }
                }
            }
        }
    }

    private void attemptWeatherSkipVote(Player player, World worldForSkip) {
        if(!worldForSkip.hasStorm()){
            player.sendMessage(notStormingMessage);
            return;
        }
        activeWeatherVote.castVote(player);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        World currentWorld = event.getWorld();
        if(!currentWorld.getName().equals("world")) return;
        if(event.isCancelled() || !event.toWeatherState()) return;

        activeWeatherVote = new Vote(
                skipWeatherText,
                skipWeatherSuccessText,
                skipWeatherFailedText,
                weatherBossbarTitle,
                0.6f,
                voteResult -> {
                    if(voteResult) {
                        currentWorld.setStorm(false);
                    }
                },
                currentWorld::hasStorm);
        activeWeatherTask = activeWeatherVote.runTaskTimer(ptfbInstance, 20L, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        activeWeatherVote.removeVote(event.getPlayer());
    }

    public void cancelAmbientVoteTasks() {
        if(activeWeatherTask != null && !activeWeatherTask.isCancelled()) {
            activeWeatherTask.cancel();
        }
    }
}
