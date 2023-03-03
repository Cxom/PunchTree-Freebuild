package net.punchtree.freebuild.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AdvancementsCommand implements CommandExecutor, TabCompleter {
    //TODO This is a temporary solution to fulfill the suggestion request. Needs dried out and refactored.

    private final List<String> advancementTypes = Arrays.asList("adventuring_time", "monsters_hunted", "two_by_two", "balanced_diet");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) return false;
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players may use this command.", NamedTextColor.RED));
            return true;
        }
        if (!player.hasPermission("ptfb.commands.advancements")) {
            player.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "adventuring_time" -> {
                player.sendMessage(
                        Component.text("Remaining biomes to explore: ", NamedTextColor.GREEN)
                                .append(Component.text(getRemainingBiomesToExplore(player), NamedTextColor.GRAY))
                );
                return true;
            }
            case "monsters_hunted" -> {
                player.sendMessage(
                        Component.text("Remaining monsters to hunt: ", NamedTextColor.GREEN)
                                .append(Component.text(getRemainingMonstersToHunt(player), NamedTextColor.GRAY))
                );
                return true;
            }
            case "two_by_two" -> {
                player.sendMessage(
                        Component.text("Remaining animals to breed: ", NamedTextColor.GREEN)
                                .append(Component.text(getRemainingAnimalsToBreed(player), NamedTextColor.GRAY))
                );
                return true;
            }
            case "balanced_diet" -> {
                player.sendMessage(
                        Component.text("Remaining foods to eat: ", NamedTextColor.GREEN)
                                .append(Component.text(getRemainingFoodsToEat(player), NamedTextColor.GRAY))
                );
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], advancementTypes, new ArrayList<String>());
        } else {
            return Collections.emptyList();
        }
    }

    private String getRemainingBiomesToExplore(Player player) {
        Advancement adventuringTime = Bukkit.getAdvancement(NamespacedKey.minecraft("adventure/adventuring_time"));
        AdvancementProgress advancement = player.getAdvancementProgress(adventuringTime);
        Collection<String> remainingCriteria = advancement.getRemainingCriteria();
        String remainingBiomes = String.join(", ", remainingCriteria);
        remainingBiomes = remainingBiomes.replace("minecraft:", "");
        return remainingBiomes;
    }

    private String getRemainingMonstersToHunt(Player player) {
        Advancement killAllMobs = Bukkit.getAdvancement(NamespacedKey.minecraft("adventure/kill_all_mobs"));
        AdvancementProgress advancement = player.getAdvancementProgress(killAllMobs);
        Collection<String> remainingCriteria = advancement.getRemainingCriteria();
        String remainingMonsters = String.join(", ", remainingCriteria);
        remainingMonsters = remainingMonsters.replace("minecraft:", "");
        return remainingMonsters;
    }

    public String getRemainingAnimalsToBreed(Player player) {
        Advancement breedAllMobs = Bukkit.getAdvancement(NamespacedKey.minecraft("husbandry/bred_all_animals"));
        AdvancementProgress advancement = player.getAdvancementProgress(breedAllMobs);
        Collection<String> remainingCriteria = advancement.getRemainingCriteria();
        String remainingMobs = String.join(", ", remainingCriteria);
        remainingMobs = remainingMobs.replace("minecraft:", "");
        return remainingMobs;
    }

    public String getRemainingFoodsToEat(Player player) {
        Advancement eatAllFoods = Bukkit.getAdvancement(NamespacedKey.minecraft("husbandry/balanced_diet"));
        AdvancementProgress advancement = player.getAdvancementProgress(eatAllFoods);
        Collection<String> remainingCriteria = advancement.getRemainingCriteria();
        String remainingFoods = String.join(", ", remainingCriteria);
        remainingFoods = remainingFoods.replace("minecraft:", "");
        return remainingFoods;
    }
}
