package net.punchtree.freebuild.claiming.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ClaimTestingCommand implements CommandExecutor, TabCompleter {

    /*
     * For consistency, this file uses the following words the following ways
     *  × Region - a worldguard region (the region that the worldguard plugin understands, used in any context)
     *  × Chunk - a Minecraft chunk
     *  × Chunk Region - a worldguard region matching the size and borders of a specific chunk
     *  × Parent Region - a parent nonphysical region to which chunk claims are parented to form one area that one or more player owners control
     *  × Claim - any area a player has marked as their own - this is a general domain language term the player knows
     */

    private static final List<String> SUBCOMMANDS = List.of("calc-chunk-index", "create-test-claim", "create-region-with-confirmation");

    private static IntegerFlag NUMBER_OF_CLAIMS_FLAG;

    private final RegionContainer regionContainer;

    public static void registerCustomWorldguardFlags() {
        FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
        String NUMBER_OF_CLAIMS_FLAG_NAME = "number-of-claims";
        try {
            NUMBER_OF_CLAIMS_FLAG = new IntegerFlag(NUMBER_OF_CLAIMS_FLAG_NAME);
            flagRegistry.register(NUMBER_OF_CLAIMS_FLAG);
        } catch (FlagConflictException fce) {
            Bukkit.getLogger().severe("Could not register our custom flag!");
        } catch (IllegalStateException ise) {
            // the plugin is being loaded after worldguard
            NUMBER_OF_CLAIMS_FLAG = (IntegerFlag) flagRegistry.get(NUMBER_OF_CLAIMS_FLAG_NAME);
        }
    }

    public ClaimTestingCommand() {
        regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return SUBCOMMANDS;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if ( ! (sender instanceof Player player)) return false;

        if (args.length < 1) {
            return false;
        }

        UUID playersId = player.getUniqueId();

        Chunk chunk = player.getLocation().getChunk();
        Block chunkMinBlock = chunk.getBlock(0, player.getWorld().getMinHeight(), 0);
        Block chunkMaxBlock = chunk.getBlock(15, player.getWorld().getMaxHeight(), 15);
        BlockVector3 min = BukkitAdapter.asBlockVector(chunkMinBlock.getLocation());
        BlockVector3 max = BukkitAdapter.asBlockVector(chunkMaxBlock.getLocation());

        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(chunk.getWorld()));

        if (regionManager == null) {
            player.sendMessage(ChatColor.DARK_RED + "Could not load region manager for this world! Could not save newly created region!");
            return true;
        }

        switch (args[0]) {
            // takes a given location (the player's location) and calculates the chunk index
            case "calc-chunk-index" -> {
                player.sendMessage(String.format("Chunk (%d, %d)", chunk.getX(), chunk.getZ()));
                player.sendMessage(String.format("Chunk min block (%d, %d)", chunkMinBlock.getX(), chunkMinBlock.getZ()));
                player.sendMessage(String.format("Chunk max block (%d, %d)", chunkMaxBlock.getX(), chunkMaxBlock.getZ()));
            }
            // create a chunk region at the chunk the player is in - doesn't create a parent region nor verify that the chunk is not claimed yet
            case "create-test-chunk-region" -> {
                String testChunkRegionName = String.format("claim_%d_%d", chunk.getX(), chunk.getZ());
                ProtectedRegion testChunkRegion = new ProtectedCuboidRegion(testChunkRegionName, min, max);

                player.sendMessage(ChatColor.LIGHT_PURPLE + "Created a region for " + testChunkRegionName);

                regionManager.addRegion(testChunkRegion);
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Saved region to this world's region list");
            }
            case "create-region-with-confirmation" -> {
                String chunkRegionName = String.format("claim_%d_%d", chunk.getX(), chunk.getZ());

                // Validate this is an unclaimed chunk
                if (regionManager.hasRegion(chunkRegionName)) {
                    player.sendMessage(ChatColor.RED + "You cannot claim this chunk as it is already claimed!");
                    return true;
                }

                // Check if it is adjacent to other chunk regions that are mine (we'll deal with other folks claims later)
                List<Direction> directionsWithAdjacentChunkRegionsOwnedBySamePlayer =
                        Arrays.stream(Direction.values())
                                .filter(direction -> hasAdjacentChunkRegionOwnedByPlayer(regionManager, playersId, chunk, direction))
                                .toList();

                if (directionsWithAdjacentChunkRegionsOwnedBySamePlayer.size() == 0) {
                    // This is establishing a new region!!!!
                    if (!isConfirmed(args, player)) {
                        sendConfirmationPrompt(player);
                        return true;
                    }

                    int personalRegionIndex = 1;
                    while (regionManager.hasRegion(String.format("%s-%d", playersId, personalRegionIndex))) {
                        ++personalRegionIndex;
                    }
                    String newRegionName = String.format("%s-%d", playersId, personalRegionIndex);
                    ProtectedRegion newParentRegion = new GlobalProtectedRegion(newRegionName);

                    ProtectedRegion newChunkRegion = new ProtectedCuboidRegion(chunkRegionName, min, max);

                    try {
                        newChunkRegion.setParent(newParentRegion);
                    } catch (ProtectedRegion.CircularInheritanceException e) {
                        // This should never actually be thrown since we're only parenting one freshly created region to another
                        throw new RuntimeException(e);
                    }

                    newParentRegion.setFlag(NUMBER_OF_CLAIMS_FLAG, 1);

                    regionManager.addRegion(newChunkRegion);
                    regionManager.addRegion(newParentRegion);

                    player.sendMessage(ChatColor.AQUA + "Established a new region under your name!");
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "Debug: Parent region: " + newRegionName);
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "Debug: Chunk region: " + chunkRegionName);
                } else {
                    // We are appending to an existing region!
                    String regionName ;
                    for (Direction direction : Direction.values()) {
                        if (directionsWithAdjacentChunkRegionsOwnedBySamePlayer.contains(direction)) {
                            ProtectedRegion adjacentChunkRegion = getAdjacentChunkRegion(regionManager, chunk, direction);
                        } else {

                        }
                    }
                }

//            player.sendMessage(ChatColor.RED + "You cannot claim this chunk as it is too close to another claim to the north that is not yours!");
//            return true;

            }
            default -> player.sendMessage(ChatColor.RED + "Subcommand not recognized");
        }

        return true;
    }

    private ProtectedRegion getAdjacentChunkRegion(RegionManager regionManager, Chunk chunk, Direction direction) {
        String chunkRegionName = String.format("claim_%d_%d", chunk.getX() + direction.x, chunk.getZ() + direction.z);

        ProtectedRegion chunkRegion = regionManager.getRegion(chunkRegionName);
        if (chunkRegion == null) return null;
        return chunkRegion.getParent();
    }

    /**
     * @param regionManager
     * @param playersUUID
     * @param chunk
     * @param direction
     * @return if the player with the given uuid has claimed the chunk adjacent to the passed in chunk in the given direction
     */
    private boolean hasAdjacentChunkRegionOwnedByPlayer(RegionManager regionManager, UUID playersUUID, Chunk chunk, Direction direction) {
        ProtectedRegion playersRegion = getAdjacentChunkRegion(regionManager, chunk, direction);
        return playersRegion != null && playersRegion.getId().startsWith(playersUUID.toString());
    }

    private enum Direction { NORTH(0,-1), EAST(1,0), SOUTH(0,1), WEST(-1,0);
        private final int x, z;

        Direction(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }

    private void sendConfirmationPrompt(Player player) {
        Component confirmationMessage1 = Component.text("Are you sure you want to claim this chunk - doing so will consume one of your allocated regions.").color(NamedTextColor.RED);
        Component confirmButton = Component
                .text("CONFIRM")
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD)
                .hoverEvent(Component.text("Click here to confirm that you want to claim this plot").asHoverEvent())
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/claimtest create-region-with-confirmation confirm " + player.getName()));
        Component confirmationMessage2 = Component
                .text("Click ").color(NamedTextColor.RED)
                .append(confirmButton)
                .append(Component.text(" to continue and establish a region beginning with this chunk.").color(NamedTextColor.RED));
        player.sendMessage(confirmationMessage1);
        player.sendMessage(confirmationMessage2);
    }

    private boolean isConfirmed(String[] args, Player player) {
        return args.length > 2 && args[1].equalsIgnoreCase("confirm") && args[2].equalsIgnoreCase(player.getName());
    }

}


