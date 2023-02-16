package net.punchtree.freebuild.towerdefense.tower;

import net.punchtree.freebuild.towerdefense.MobSpawnMenu;
import net.punchtree.freebuild.towerdefense.TowerDefensePlayer;
import net.punchtree.freebuild.towerdefense.TowerDefensePlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class TowerDefenseHotbarUiListener implements Listener {

    private static final ItemStack MOBS_SPAWN_MENU_ITEM = new ItemStack(Material.DIAMOND_SHOVEL);
    static {
        MOBS_SPAWN_MENU_ITEM.editMeta(meta -> {
//            meta.displayName(Component.text("Mob Spawn Menu").color(NamedTextColor.GOLD));
//            meta.lore(List.of(Component.text("Left click to open the mob spawn menu")));
            meta.setCustomModelData(300);
        });
    }

    private final TowerDefensePlayerManager towerDefensePlayerManager;

    public TowerDefenseHotbarUiListener(TowerDefensePlayerManager towerDefensePlayerManager) {
        this.towerDefensePlayerManager = towerDefensePlayerManager;
    }

    //TODO make this respond to the correct hotbar item
    @EventHandler
    public void onTriggerMobSpawnMenu(PlayerInteractEvent event) {
        if ( ! Objects.equals(event.getItem(), MOBS_SPAWN_MENU_ITEM)) return;
        Player player = event.getPlayer();
        TowerDefensePlayer towerDefensePlayer = towerDefensePlayerManager.getPlayer(player);
        if (towerDefensePlayer == null) return;
        MobSpawnMenu.open(towerDefensePlayer);
    }
}
