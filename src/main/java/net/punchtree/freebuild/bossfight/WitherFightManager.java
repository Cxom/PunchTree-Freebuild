package net.punchtree.freebuild.bossfight;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class WitherFightManager implements Listener {

    // While cancelled events and a map are still necessary, they could instead be a list of uuids on a custom wither entity
    // This would also prevent withers from even trying to target players not in the fight
    // Explosion and wither effect damage still need to be prevented though

    private final Map<Player, Wither> playersFightingWithers = new HashMap<>();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playersFightingWithers.remove(event.getPlayer());
    }

    @EventHandler
    public void onWitherDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Wither wither)) return;
        playersFightingWithers.entrySet().removeIf(entry -> entry.getValue().equals(wither));
    }

    // We need to verify the following damage cannot apply to unengaged players
    // - Birth explosion (after initial 11 seconds of expanding)
    // - Wither skull hit damage
    // - Wither status effect
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWitherDamage(EntityDamageByEntityEvent event) {
        // Handle player registration on damaging the wither
        if (event.getEntity() instanceof Wither damagedWither) {
            if (event.getDamager() instanceof Player player) {
                playersFightingWithers.put(player, damagedWither);
            } else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
                playersFightingWithers.put(player, damagedWither);
            }
            return;
        }

        if (!(event.getEntity() instanceof Player player && playersFightingWithers.containsKey(player))) return;
        player.sendMessage("Debug: " + event.getDamager().getClass().getSimpleName() + " damaged " + player.getName() + " with " + event.getCause().name() + " for " + event.getDamage() + " damage");
        if (event.getDamager() instanceof Wither wither) {
            if (!wither.getUniqueId().equals(playersFightingWithers.get(player).getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage("Debug: " + ChatColor.RED + "Canceled damage from not-engaged-with wither");
            }
        }
    }

    @EventHandler
    public void onPlayerTakeDamage(EntityDamageEvent event) {
        if ( !(event.getEntity() instanceof Player player) ) return;
        if ((event.getCause() == EntityDamageEvent.DamageCause.WITHER) &&
                !playersFightingWithers.containsKey(player)) {
            event.setCancelled(true);
        }
    }

    public void onEnable() {
        playersFightingWithers.clear();
    }

    public void onDisable() {
        playersFightingWithers.clear();
    }

}
