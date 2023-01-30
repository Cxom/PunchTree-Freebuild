package net.punchtree.freebuild.billiards;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

class BilliardsItems {

    static final ItemStack CUE_STICK = new ItemStack(Material.BOW);
    static {
        CUE_STICK.editMeta(meta -> {
            meta.setCustomModelData(200);
            meta.displayName(Component.text("Cue Stick").decoration(TextDecoration.ITALIC, false));
        });
    }

    static final Material TABLE_MATERIAL = Material.GREEN_WOOL;

    static final ItemStack CUE_BALL = new ItemStack(Material.BONE_BLOCK);
    static {
        CUE_BALL.editMeta(meta -> {
            meta.setCustomModelData(200);
            meta.displayName(Component.text("Cue Stick").decoration(TextDecoration.ITALIC, false));
        });
    }

}
