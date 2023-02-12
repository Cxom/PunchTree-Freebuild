package net.punchtree.freebuild.towerdefense;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum TowerType {
    BASIC(Material.MOSSY_COBBLESTONE_WALL, Component.text("Basic Tower"));

    public final Material iconMaterial;
    private TextComponent name;
    private ItemStack icon;

    TowerType(Material iconMaterial, TextComponent name) {
        this.iconMaterial = iconMaterial;
        this.name = name;
        this.icon = new ItemStack(iconMaterial);
        this.icon.editMeta(meta -> {
            meta.displayName(name);
        });
    }

    public TextComponent getName() {
        return name;
    }

    public ItemStack getIcon() {
        return icon;
    }
}
