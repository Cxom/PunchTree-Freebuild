package net.punchtree.freebuild.afk;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AfkCommand implements CommandExecutor {

    // Store a list of players who have used the /afk command
    // as well as the time they used it
    private static final Map<UUID, Long> afkCooldownList = new HashMap<>();
    public static final BukkitTask cooldownPruningTask;

    static {
        cooldownPruningTask = Bukkit.getScheduler().runTaskTimer(PunchTreeFreebuildPlugin.getInstance(), () -> {
            for(UUID uuid : afkCooldownList.keySet()) {
                if(System.currentTimeMillis() - afkCooldownList.get(uuid) > 30 * 1000) {
                    afkCooldownList.remove(uuid);
                }
            }
        }, 0, 60 * 20);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(command.getName().equalsIgnoreCase("afk")) {
            if(!(sender instanceof Player player)) return false;
            if(args.length == 0) {
                if(!player.hasPermission("ptfb.commands.afk.self")) {
                    player.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
                    return true;
                }
                toggleAfk(player, false);
                return true;
            }
            if(args.length == 1) {
                if(!player.hasPermission("ptfb.commands.afk.other")) {
                    player.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if(target == null) {
                    player.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                    return true;
                }
                toggleAfk(target, true);
                return true;
            }
        }
        return false;
    }

    public void toggleAfk(Player player, boolean force) {
        Roster afkRoster = RosterManager.getRoster("afk");

        if(afkRoster.containsPlayer(player.getUniqueId())) {
            afkRoster.removePlayer(player.getUniqueId());
        } else {
            if(afkCooldownList.containsKey(player.getUniqueId()) && !force) {
                long timeSinceLastAfk = System.currentTimeMillis() - afkCooldownList.get(player.getUniqueId());
                if(timeSinceLastAfk < 30 * 1000) {
                    player.sendMessage(Component.text("You must wait 30 seconds between toggling AFK.", NamedTextColor.RED));
                    return;
                }
            }
            afkRoster.addPlayer(player.getUniqueId());
            afkCooldownList.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }
}
