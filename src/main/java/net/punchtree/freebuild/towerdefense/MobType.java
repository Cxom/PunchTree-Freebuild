package net.punchtree.freebuild.towerdefense;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

enum MobType {

    PARROT(Material.PARROT_SPAWN_EGG),
    PANDA(Material.PANDA_SPAWN_EGG),
    PUFFERFISH(Material.PUFFERFISH_SPAWN_EGG);

    private Material menuSpawnItemMaterial;

    MobType(Material menuSpawnItemMaterial) {
        this.menuSpawnItemMaterial = menuSpawnItemMaterial;
    }

    public ItemStack getMenuSpawnItem() {
        ItemStack menuSpawnItem = new ItemStack(menuSpawnItemMaterial);
        menuSpawnItem.editMeta(meta -> {
            meta.displayName(
                    Component.text("Spawn " + WordUtils.capitalize(name().toLowerCase()))
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
        });
        return menuSpawnItem;
    }
}
