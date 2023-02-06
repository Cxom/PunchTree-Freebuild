package net.punchtree.freebuild.billiards;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import static net.punchtree.freebuild.billiards.BilliardsItems.CUE_STICK;

public class BilliardsShootListener implements Listener {

    private final BilliardsManager billiardsManager;

    public BilliardsShootListener(BilliardsManager billiardsManager) {
        this.billiardsManager = billiardsManager;
    }

    private boolean isHoldingCueStick(Player player) {
        return CUE_STICK.equals(player.getInventory().getItemInMainHand());
    }

    @EventHandler
    public void onAimCueStick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!isHoldingCueStick(player)) return;

        player.sendMessage("Aiming with cue stick.");
    }

    @EventHandler
    public void onShootCueStick(EntityShootBowEvent event) {
        if ( ! (event.getEntity() instanceof Player player)) return;
        if (!isHoldingCueStick(player)) return;

        event.setCancelled(true);

        player.sendMessage("Shot with cue stick!");

        BilliardsShot shot = new BilliardsShot(player, event.getForce());
        billiardsManager.getNearbyTables(player.getLocation()).forEach(table -> table.takeShot(shot));
    }

}
