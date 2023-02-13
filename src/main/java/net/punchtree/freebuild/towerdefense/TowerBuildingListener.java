package net.punchtree.freebuild.towerdefense;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class TowerBuildingListener implements Listener {

    public static final Material TOWER_PLACEABLE_AREA_MATERIAL = Material.LODESTONE;

    private final TowerDefensePlayerManager towerDefensePlayerManager;
    private TowerSelectionMenu towerSelectionMenu;

    public TowerBuildingListener(TowerDefensePlayerManager towerDefensePlayerManager) {
        this.towerDefensePlayerManager = towerDefensePlayerManager;
        this.towerSelectionMenu = new TowerSelectionMenu(towerDefensePlayerManager);
    }

    @EventHandler
    public void onRightClickBlock(PlayerInteractEvent event) {
        if ( event.getAction() != Action.RIGHT_CLICK_BLOCK ) return;
        if ( event.getHand() != EquipmentSlot.HAND ) return;
        if ( event.getClickedBlock().getType() != TOWER_PLACEABLE_AREA_MATERIAL ) return;

        TowerDefensePlayer tdPlayer = getTowerDefensePlayer(event.getPlayer());
        if (tdPlayer == null) return;

        tdPlayer.setSelectedTowerBuildLocation(event.getClickedBlock());
        showTowerSelectionMenu(tdPlayer);
    }

    private void showTowerSelectionMenu(TowerDefensePlayer tdPlayer) {
        towerSelectionMenu.showTo(tdPlayer);
    }

    private TowerDefensePlayer getTowerDefensePlayer(Player player) {
        return towerDefensePlayerManager.getPlayer(player);
    }
}
