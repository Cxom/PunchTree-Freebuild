package net.punchtree.freebuild.towerdefense;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

/**
 * A menu that allows the player to spawn mobs
 * It presents a list of mobs that can be spawned
 * Selecting a mob will spawn it in the player's game
 */
public class MobSpawnMenu {

    public static final Component MOB_SPAWN_MENU_TITLE = Component.text("Mob Spawn Menu");

    public static void open(TowerDefensePlayer tdPlayer) {
        MobSpawnMenu menu = new MobSpawnMenu(tdPlayer);
        menu.show();
    }

    private final TowerDefensePlayer tdPlayer;
    private final Inventory inventory;

    public MobSpawnMenu(TowerDefensePlayer tdPlayer) {
        this.tdPlayer = tdPlayer;
        this.inventory = Bukkit.createInventory(null, 9, MOB_SPAWN_MENU_TITLE);
        updateInventory();
    }

    private void show() {
        updateInventory();
        tdPlayer.getPlayer().openInventory(inventory);
    }

    private void updateInventory() {
        inventory.setItem(0, MobType.PARROT.getMenuSpawnItem());
        inventory.setItem(1, MobType.PANDA.getMenuSpawnItem());
        inventory.setItem(2, MobType.PUFFERFISH.getMenuSpawnItem());
    }

}
