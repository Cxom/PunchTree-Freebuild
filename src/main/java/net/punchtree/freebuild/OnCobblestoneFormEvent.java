package net.punchtree.freebuild;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;

public class OnCobblestoneFormEvent implements Listener {

    int cobblestoneFormed = 0;
    long lastLogged = System.currentTimeMillis();

    @EventHandler
    public void onCobblestoneFormEvent(BlockFormEvent event) {
        if (event.getNewState().getType() == Material.COBBLESTONE) {
            //print a debug message to the console
            System.out.println("Cobblestone formed!");
            cobblestoneFormed++;
            if(System.currentTimeMillis() - lastLogged > 1000 * 60) {
                Bukkit.getLogger().info("Cobblestone formed: " + cobblestoneFormed);
                lastLogged = System.currentTimeMillis();
            }
            event.setCancelled(true);
        }
    }
}
