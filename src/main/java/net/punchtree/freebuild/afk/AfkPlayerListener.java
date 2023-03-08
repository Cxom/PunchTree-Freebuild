package net.punchtree.freebuild.afk;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AfkPlayerListener implements Listener {

    private static final ConcurrentHashMap<UUID, Long> lastActivity = new ConcurrentHashMap<>();

    public static BukkitTask autoAfkTask;

    static {
        autoAfkTask = Bukkit.getScheduler().runTaskTimer(PunchTreeFreebuildPlugin.getInstance(), () -> {
            Iterator<Map.Entry<UUID, Long>> iterator = lastActivity.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, Long> entry = iterator.next();
                UUID uuid = entry.getKey();
                Player afkPlayer = Bukkit.getPlayer(uuid);
                if(afkPlayer == null || !afkPlayer.isOnline() || RosterManager.getRoster("afk").containsPlayer(uuid)) {
                    iterator.remove();
                    continue;
                }
                if(afkPlayer.hasPermission("ptfb.afk.auto.bypass") || !afkPlayer.hasPermission("ptfb.afk.auto")) {
                    continue;
                }
                if (System.currentTimeMillis() - entry.getValue() > 1000 * 60 * 5) {
                    RosterManager.getRoster("afk").addPlayer(uuid);
                    clearActivity(afkPlayer);
                }
            }
        }, 0, 20 * 10);

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        //TODO: un-AFK players after they've moved a certain distance to stop them from abusing flight.
        if(event.getFrom().distanceSquared(event.getTo()) < 0.01) return;
        updateLastActivity(event.getPlayer());
        removePlayerFromRoster(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        updateLastActivity(event.getPlayer());
        removePlayerFromRoster(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        clearActivity(event.getPlayer());
        removePlayerFromRoster(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateLastActivity(event.getPlayer());
        removePlayerFromRoster(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        updateLastActivity(event.getPlayer());
        removePlayerFromRoster(event.getPlayer());
    }

    @EventHandler
    public void onPlayerSendCommand(PlayerCommandPreprocessEvent event) {
        updateLastActivity(event.getPlayer());
        if(event.getMessage().startsWith("/afk")) return;
        removePlayerFromRoster(event.getPlayer());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        updateLastActivity(event.getPlayer());
        removePlayerFromRoster(event.getPlayer());
    }

    public static void updateLastActivity(Player player) {
        lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public static void clearActivity(Player player) {
        lastActivity.remove(player.getUniqueId());
    }

    private void removePlayerFromRoster(Player player) {
        Roster afkRoster = RosterManager.getRoster("afk");
        if(!afkRoster.containsPlayer(player.getUniqueId())) return;
        afkRoster.removePlayer(player.getUniqueId());
    }
}
