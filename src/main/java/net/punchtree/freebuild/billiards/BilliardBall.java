package net.punchtree.freebuild.billiards;

import net.punchtree.freebuild.util.armorstand.ArmorStandUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EquipmentSlot;

public class BilliardBall {

    BilliardTable table;

    ArmorStand stand;

    public BilliardBall(BilliardTable table, Location spawnLocation) {
        this.table = table;

        Bukkit.broadcastMessage("spawning a cue ball!");
        stand = spawnLocation.getWorld().spawn(spawnLocation, ArmorStand.class, stand -> {
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setInvisible(true);
            stand.setInvulnerable(true);
            stand.setCanTick(false);
            ArmorStandUtils.resetPose(stand);
            stand.setItem(EquipmentSlot.HAND, BilliardsItems.CUE_BALL);
            stand.addScoreboardTag("billiards");
        });
    }

    public void remove() {
        stand.remove();
    }

}
