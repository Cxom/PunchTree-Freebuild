package net.punchtree.freebuild.afk;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AfkPlayerListener implements Listener {

    private static final Map<UUID, Long> lastActivity = new HashMap<>();

    public static BukkitTask autoAfkTask;

    static {
        autoAfkTask = Bukkit.getScheduler().runTaskTimer(PunchTreeFreebuildPlugin.getInstance(), () -> {
            for (UUID uuid : lastActivity.keySet()) {
                Player afkPlayer = Bukkit.getPlayer(uuid);
                if(afkPlayer == null || !afkPlayer.isOnline() || RosterManager.getRoster("afk").containsPlayer(uuid)) {
                    lastActivity.remove(uuid);
                    continue;
                }
                if(afkPlayer.hasPermission("ptfb.afk.auto.bypass") || !afkPlayer.hasPermission("ptfb.afk.auto")) {
                    continue;
                }
                if (System.currentTimeMillis() - lastActivity.get(uuid) > 1000 * 60 * 5) {
                    RosterManager.getRoster("afk").addPlayer(uuid);
                    lastActivity.remove(uuid);
                }
            }
        }, 0, 20 * 10);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        //TODO: un-AFK players after they've moved a certain distance to stop them from abusing flight.
        if(event.getFrom().distanceSquared(event.getTo()) < 0.01) return;
        lastActivity.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        RosterManager.getRoster("afk").removePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        lastActivity.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        RosterManager.getRoster("afk").removePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        lastActivity.remove(event.getPlayer().getUniqueId());
        RosterManager.getRoster("afk").removePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        lastActivity.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        RosterManager.getRoster("afk").removePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        lastActivity.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        RosterManager.getRoster("afk").removePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerSendCommand(PlayerCommandPreprocessEvent event) {
        lastActivity.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        RosterManager.getRoster("afk").removePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        lastActivity.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        RosterManager.getRoster("afk").removePlayer(event.getPlayer().getUniqueId());
    }
}
