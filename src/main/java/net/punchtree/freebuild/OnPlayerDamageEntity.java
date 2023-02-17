package net.punchtree.freebuild;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

public class OnPlayerDamageEntity implements Listener {

    @EventHandler()
    public void onPlayerDamageEntityEvent(EntityDamageByEntityEvent event) {
        if(event.isCancelled()) return;
        if (event.getDamager() instanceof Player) {
            if (event.getEntity() instanceof Tameable tameable) {
                AnimalTamer tamer = tameable.getOwner();
                if(tamer == null) return;
                if (tamer instanceof Player) {
                    UUID tamerUUID = ((Player) tamer).getUniqueId();
                    Player damager = (Player) event.getDamager();
                    if(!tamerUUID.equals(damager.getUniqueId()) && !damager.hasPermission("ptfb.protection.damagetamed")) {
                        event.setCancelled(true);
                        damager.sendMessage(Component.text("You can't damage tamed animals that aren't yours!", NamedTextColor.RED));
                    }
                }
            }
        }
    }
}
