package net.punchtree.freebuild.commands;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class AmbientVoteCommand implements CommandExecutor, Listener {

    private static final Component chatHeader;
    private static final Component chatFooter;
    private static final Component skipWeatherText;
    private static final Component weatherBossbarTitle;
    private static final Component skipWeatherFailedText;
    private static final Component skipWeatherSuccessText;
    private static final Component builtWeatherMessage;
    private static final Component builtWeatherFailedMessage;
    private static final Component builtWeatherSuccessMessage;
    private static final Component voteCastMessage;
    private static final Component voteAlreadyCastMessage;
    private static final Component notStormingMessage;
    private static final Component noActiveVoteMessage;
    private static final BossBar currentWeatherBossbar;
    private static final List<Player> currentWeatherVoters = new ArrayList<>();
    private static ForwardingAudience currentAudience;
    private static BukkitTask activeWeatherTask = null;

    private final PunchTreeFreebuildPlugin ptfbInstance = PunchTreeFreebuildPlugin.getInstance();

    static {
        chatHeader = Component
                .text("-=-=-=-=-=-= ", NamedTextColor.GREEN)
                .append(Component.text("Punch", NamedTextColor.RED))
                .append(Component.text("Tree Ambient Voting =-=-=-=-=-=-\n", NamedTextColor.GREEN));
        chatFooter = Component.text("\n-----------------------------------------------", NamedTextColor.GREEN);

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
                .append(weatherBossbarTitle)
                .clickEvent(ClickEvent.suggestCommand("/vskip weather"));

        voteCastMessage = Component
                .text("Your vote has been cast!", NamedTextColor.AQUA);

        voteAlreadyCastMessage = Component
                .text("Your vote has already been cast!", NamedTextColor.RED);

        notStormingMessage = Component
                .text("Its not storming right now!", NamedTextColor.RED);

        noActiveVoteMessage = Component
                .text("There's no vote happening right now!", NamedTextColor.RED);

        builtWeatherMessage = chatHeader.append(skipWeatherText).append(chatFooter);
        builtWeatherFailedMessage = chatHeader.append(skipWeatherFailedText).append(chatFooter);
        builtWeatherSuccessMessage = chatHeader.append(skipWeatherSuccessText).append(chatFooter);
        currentWeatherBossbar = BossBar.bossBar(weatherBossbarTitle, 1.0f, BossBar.Color.GREEN, BossBar.Overlay.NOTCHED_20);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) return true;

        switch (label) {
            case "skipweather" -> {
                if(!Objects.requireNonNull(Bukkit.getWorld("world")).hasStorm()){
                    player.sendMessage(notStormingMessage);
                    return true;
                }
                if(activeWeatherTask == null) {
                    player.sendMessage(noActiveVoteMessage);
                    return true;
                }
                if(currentWeatherVoters.contains(player)) {
                    player.sendMessage(voteAlreadyCastMessage);
                    return true;
                }
                currentWeatherVoters.add(player);
                player.sendMessage(voteCastMessage);
                return true;
            }
            case "skipnight" -> {
                return true;
            }
            default -> {
                if (args.length == 0) return false;
                switch (args[0]) {
                    case "weather", "storm" -> {
                        if(!Objects.requireNonNull(Bukkit.getWorld("world")).hasStorm()){
                            player.sendMessage(notStormingMessage);
                            return true;
                        }
                        if(activeWeatherTask == null) {
                            player.sendMessage(noActiveVoteMessage);
                            return true;
                        }
                        if(currentWeatherVoters.contains(player)) {
                            player.sendMessage(voteAlreadyCastMessage);
                            return true;
                        }
                        currentWeatherVoters.add(player);
                        player.sendMessage(voteCastMessage);
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

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if(!event.getWorld().getName().equals("world")) return;
        if(event.isCancelled() || !event.toWeatherState()) return;
        currentAudience = Audience.audience(Bukkit.getOnlinePlayers());
        currentWeatherBossbar.name(appendVoteCount());

        Audience permissibleAudience =  currentAudience.filterAudience(audience ->
                audience instanceof Player p && p.hasPermission("ptfb.commands.ambientvoting.notify"));

        permissibleAudience.sendMessage(builtWeatherMessage);
        permissibleAudience.showBossBar(currentWeatherBossbar);
        activeWeatherTask = new BukkitRunnable() {
            @Override
            public void run() {
                currentAudience = Audience.audience(Bukkit.getOnlinePlayers());
                if(this.isCancelled()) {
                    restoreDefaults();
                    return;
                }
                if(Objects.requireNonNull(Bukkit.getWorld("world")).isClearWeather()) {
                    this.cancel();
                    restoreDefaults();
                    return;
                }

                Audience permissibleAudience =  currentAudience.filterAudience(audience ->
                        audience instanceof Player p && p.hasPermission("ptfb.commands.ambientvoting.notify"));

                if(currentWeatherBossbar.progress() == 0.0f || currentWeatherVoters.size() >= calculateRequiredVotes()) {

                    if(currentWeatherVoters.size() < calculateRequiredVotes()) {
                        permissibleAudience.sendMessage(builtWeatherFailedMessage);
                    }else {
                        permissibleAudience.sendMessage(builtWeatherSuccessMessage);
                        Objects.requireNonNull(Bukkit.getWorld("world")).setStorm(false);
                    }
                    this.cancel();
                    restoreDefaults();
                    return;
                }
                currentWeatherBossbar.progress(Math.max(currentWeatherBossbar.progress() - 0.05f, 0.0f));
                currentWeatherBossbar.name(appendVoteCount());
                permissibleAudience.showBossBar(currentWeatherBossbar);
            }
        }.runTaskTimer(ptfbInstance, 20L, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        currentWeatherVoters.remove(event.getPlayer());
    }

    public void restoreDefaults() {
        currentAudience.hideBossBar(currentWeatherBossbar);
        currentWeatherBossbar.name(appendVoteCount());
        currentWeatherBossbar.progress(1.0f);
        currentWeatherVoters.clear();
        activeWeatherTask = null;
    }

    public void cancelAmbientVoteTasks() {
        if(activeWeatherTask != null && !activeWeatherTask.isCancelled()) {
            activeWeatherTask.cancel();
            restoreDefaults();
        }
    }

    private int calculateRequiredVotes() {
        return (int) (Math.max(Bukkit.getOnlinePlayers().size() * 0.7f, 1f));
    }

    private Component appendVoteCount() {
        return weatherBossbarTitle
                .append(Component.text(" : ")
                .append(Component.text(
                        currentWeatherVoters.size()
                        + "/"
                        + calculateRequiredVotes()))
                .color(NamedTextColor.GOLD));
    }
}
