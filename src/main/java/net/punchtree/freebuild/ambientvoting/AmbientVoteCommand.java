package net.punchtree.freebuild.ambientvoting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import net.punchtree.freebuild.ambientvoting.NightTimeRunnable;
import net.punchtree.freebuild.ambientvoting.Vote;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class AmbientVoteCommand implements CommandExecutor, Listener {
    private static final Component skipWeatherText;
    private static final Component weatherProgressBarTitle;
    private static final Component skipWeatherFailedText;
    private static final Component skipWeatherSuccessText;
    private static final Component notStormingMessage;
    private static final Component notNightMessage;
    private Vote activeWeatherVote = null;

    private final PunchTreeFreebuildPlugin ptfbInstance = PunchTreeFreebuildPlugin.getInstance();

    static {
        weatherProgressBarTitle = Component
                .text("Type ", NamedTextColor.AQUA)
                .append(Component.text("/vskip weather ", NamedTextColor.GOLD))
                .append(Component.text("to vote skip the storm.", NamedTextColor.AQUA));

        skipWeatherFailedText = Component
                .text("The vote to skip the weather has ", NamedTextColor.AQUA)
                .append(Component.text("failed", NamedTextColor.RED))
                .append(Component.text("!", NamedTextColor.AQUA));

        skipWeatherSuccessText = Component
                .text("The vote to skip the weather has ", NamedTextColor.AQUA)
                .append(Component.text("succeeded", NamedTextColor.GREEN))
                .append(Component.text("!", NamedTextColor.AQUA));

        skipWeatherText = Component
                .text("Looks like the weather has taken a turn for the worse!\n", NamedTextColor.AQUA)
                .append(Component.text("Type or click ", NamedTextColor.AQUA))
                .append(Component.text("[/vskip weather] ", NamedTextColor.GOLD))
                .append(Component.text("to vote skip the storm.", NamedTextColor.AQUA))
                .clickEvent(ClickEvent.runCommand("/vskip weather"));

        notStormingMessage = Component
                .text("Its not storming right now!", NamedTextColor.RED);

        notNightMessage = Component
                .text("Its not night time right now!", NamedTextColor.RED);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return true;
        World worldToSkip = Bukkit.getWorld("world");
        //TODO: Add support for skipping other worlds and get rid of this assertion
        assert worldToSkip != null;

        switch (label) {
            case "skipweather" -> {
                attemptWeatherSkipVote(player, worldToSkip);
                return true;
            }
            case "skipnight" -> {
                attemptNightSkipVote(player, worldToSkip);
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
                        attemptNightSkipVote(player, worldToSkip);
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
    private void attemptNightSkipVote(Player player, World worldForSkip) {
        if(worldForSkip.getTime() < 13000L){
            player.sendMessage(notNightMessage);
            return;
        }
        ptfbInstance.getNightTimeRunnable().getCurrentNightVote().castVote(player);
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
                weatherProgressBarTitle,
                0.6f,
                voteResult -> {
                    if(voteResult) {
                        currentWorld.setStorm(false);
                    }
                },
                () -> !currentWorld.hasStorm());
        activeWeatherVote.runTaskTimer(ptfbInstance, 20L, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(activeWeatherVote != null){
            activeWeatherVote.removeVote(event.getPlayer());
        }
        ptfbInstance.getNightTimeRunnable().getCurrentNightVote().removeVote(event.getPlayer());
    }

    @EventHandler()
    public void onCommandPreProcessEvent(PlayerCommandPreprocessEvent event) {
        String commandMessage = event.getMessage().toLowerCase();
        if(commandMessage.startsWith("/time ") || commandMessage.startsWith("/etime ")) {
        new BukkitRunnable() {

            @Override
            public void run() {
                ptfbInstance.setNightTimeRunnable(new NightTimeRunnable(Bukkit.getWorld("world")), 13000L);
            }
        }.runTaskLater(ptfbInstance, 20);
        }
    }

    public void cancelWeatherVote() {
        if(activeWeatherVote == null) return;
        try {
            activeWeatherVote.cancel();
        } catch (IllegalStateException ignored) {
            ptfbInstance.getLogger().log(Level.WARNING, "Weather vote task was already cancelled");
        }
    }
}
