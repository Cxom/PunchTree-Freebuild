package net.punchtree.freebuild.ambientvoting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

public class AmbientVoteCommand implements CommandExecutor, TabCompleter, Listener {

    private final List<String> subCommands = Arrays.asList("storm", "night");
    private static final Component SKIP_WEATHER_TEXT = Component.text(
                    "Looks like the weather has taken a turn for the worse!\n", NamedTextColor.AQUA)
            .append(Component.text("Type or click ", NamedTextColor.AQUA))
            .append(Component.text("[/vskip weather] ", NamedTextColor.GOLD)
                    .clickEvent(ClickEvent.runCommand("/vskip weather")))
            .append(Component.text("to vote skip the storm.", NamedTextColor.AQUA));

    private static final Component WEATHER_PROGRESS_BAR_TITLE = Component.text(
                    "Type ", NamedTextColor.AQUA)
            .append(Component.text("/vskip weather ", NamedTextColor.GOLD))
            .append(Component.text("to vote skip the storm.", NamedTextColor.AQUA));

    private static final Component SKIP_WEATHER_FAILED_TEXT = Component.text(
            "The vote to skip the weather has failed!", NamedTextColor.RED);

    private static final Component SKIP_WEATHER_SUCCESS_TEXT = Component.text(
            "The vote to skip the weather has succeeded!", NamedTextColor.GREEN);

    private static final Component NOT_STORMING_MESSAGE = Component.text(
            "It's not storming right now!", NamedTextColor.RED);

    private static final Component NOT_NIGHT_MESSAGE = Component.text(
            "It's not nighttime right now!", NamedTextColor.RED);

    private Vote activeWeatherVote = null;
    private final PunchTreeFreebuildPlugin ptfbInstance = PunchTreeFreebuildPlugin.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        World worldToSkip = Bukkit.getWorld("world");
        //TODO: Add support for skipping other worlds and get rid of this assertion
        assert worldToSkip != null;

        boolean handled;

        if (args.length > 0) {
            handled = handleCommand(args[0], player, worldToSkip);
        } else {
            handled = handleCommand(label, player, worldToSkip);
        }

        return handled;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (label.equalsIgnoreCase("vskip")) {
            if(args.length > 1) return null;
            return subCommands;
        }
        return null;
    }

    private boolean handleCommand(String arg, Player player, World worldToSkip) {
        switch (arg.toLowerCase()) {
            case "weather", "storm", "skipweather" -> {
                return attemptWeatherSkipVote(player, worldToSkip);
            }
            case "night", "time", "skipnight" -> {
                return attemptNightSkipVote(player, worldToSkip);
            }
            default -> {
                return false;
            }
        }
    }

    private boolean attemptWeatherSkipVote(Player player, World worldForSkip) {
        if (!worldForSkip.hasStorm()) {
            player.sendMessage(NOT_STORMING_MESSAGE);
            return true;
        }
        activeWeatherVote.castVote(player);
        return true;
    }

    private boolean attemptNightSkipVote(Player player, World worldForSkip) {
        if (worldForSkip.getTime() < 13000L) {
            player.sendMessage(NOT_NIGHT_MESSAGE);
            return true;
        }
        ptfbInstance.getNightTimeRunnable().getCurrentNightVote().castVote(player);
        return true;
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        World world = event.getWorld();
        if (!world.getName().equals("world") || event.isCancelled() || !event.toWeatherState()) return;

        Consumer<Boolean> voteAction = voteResult -> {
            if (voteResult) {
                world.setStorm(false);
            }
        };
        Supplier<Boolean> voteCondition = () -> !world.hasStorm();

        activeWeatherVote = new Vote(
                SKIP_WEATHER_TEXT,
                SKIP_WEATHER_SUCCESS_TEXT,
                SKIP_WEATHER_FAILED_TEXT,
                WEATHER_PROGRESS_BAR_TITLE,
                0.6f,
                voteAction,
                voteCondition
        );

        activeWeatherVote.runTaskTimer(ptfbInstance, 20L, 20L);
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (activeWeatherVote != null) {
            activeWeatherVote.removeVote(event.getPlayer());
        }
        ptfbInstance.getNightTimeRunnable().getCurrentNightVote().removeVote(event.getPlayer());
    }

    @EventHandler
    public void onCommandPreProcessEvent(PlayerCommandPreprocessEvent event) {
        String commandMessage = event.getMessage().toLowerCase();
        if (!commandMessage.startsWith("/time ") && !commandMessage.startsWith("/etime ")) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                World world = Bukkit.getWorld("world");
                NightTimeRunnable nightTimeRunnable = new NightTimeRunnable(world);
                ptfbInstance.setNightTimeRunnable(nightTimeRunnable, 13000L);
            }
        }.runTaskLater(ptfbInstance, 20);
    }


    public void cancelWeatherVote() {
        if (activeWeatherVote == null) return;
        try {
            activeWeatherVote.cancel();
        } catch (IllegalStateException ignored) {
            ptfbInstance.getLogger().log(Level.WARNING, "Weather vote task was already cancelled");
        }
    }
}
