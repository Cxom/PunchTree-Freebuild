package net.punchtree.freebuild.towerdefense;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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

    public Block[] getFootprint(Location selectedTowerBuildLocation) {
        // For now, all tower types are 3x3
        Block centerBlock = selectedTowerBuildLocation.getBlock();
        Block northCenterBlock = centerBlock.getRelative(BlockFace.NORTH);
        Block southCenterBlock = centerBlock.getRelative(BlockFace.SOUTH);
        return new Block[] {
                northCenterBlock.getRelative(BlockFace.WEST), northCenterBlock, northCenterBlock.getRelative(BlockFace.EAST),
                centerBlock.getRelative(BlockFace.WEST),      centerBlock,      centerBlock.getRelative(BlockFace.EAST),
                southCenterBlock.getRelative(BlockFace.WEST), southCenterBlock, southCenterBlock.getRelative(BlockFace.EAST)
        };
    }
}
