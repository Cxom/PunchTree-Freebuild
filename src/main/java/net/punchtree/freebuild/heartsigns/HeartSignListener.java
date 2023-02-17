package net.punchtree.freebuild.heartsigns;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public class HeartSignListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignChange(SignChangeEvent event) {
        Bukkit.getLogger().info("SignChangeEvent");
        if(event.line(0) == null || !event.getPlayer().hasPermission("ptfb.heartsigns.create")) return;
        Bukkit.getLogger().info("Line wasn't null and player has permission");
        Sign sign = (Sign) event.getBlock().getState();
        String line0AsString = PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(event.line(0)));
        String triggerString = "[heart]";
        Bukkit.getLogger().info(line0AsString + " " + triggerString);
        if(line0AsString.equalsIgnoreCase(triggerString)) {
            Bukkit.getLogger().info("Line 0 was equal to trigger string");
            event.setCancelled(true);
            new HeartSign(sign, event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRightClickSign(PlayerInteractEvent event) {
        if(event.getClickedBlock() == null || !(event.getClickedBlock().getState() instanceof Sign sign) || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        Player clicker = event.getPlayer();
        if(HeartSign.isHeartSign(sign)) {
            event.setCancelled(true);
            HeartSign heartSign = new HeartSign(sign);
            if(heartSign.getHearters().contains(clicker.getUniqueId())) {
                if(!clicker.hasPermission("ptfb.heartsigns.unheart") && !clicker.getUniqueId().equals(heartSign.getOwner())) {
                    clicker.sendMessage(Component.text("You don't have permission to unheart builds.", NamedTextColor.RED));
                    return;
                }
                heartSign.removeHearter(clicker.getUniqueId());
                clicker.sendMessage(Component.text("You've removed a heart from this build.", NamedTextColor.RED));
                return;
            }
            if(!clicker.hasPermission("ptfb.heartsigns.heart") && !clicker.getUniqueId().equals(heartSign.getOwner())) {
                clicker.sendMessage(Component.text("You don't have permission to heart builds.", NamedTextColor.RED));
                return;
            }
            clicker.sendMessage(Component.text("You've added a heart to this build.", NamedTextColor.GREEN));
            heartSign.addHearter(clicker.getUniqueId());
        }
    }
}
