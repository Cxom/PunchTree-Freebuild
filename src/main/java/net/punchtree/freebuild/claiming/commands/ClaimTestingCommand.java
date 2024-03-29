package net.punchtree.freebuild.claiming.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
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
import org.jetbrains.annotations.NotNull;

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

    // TODO idea - the CREATOR of a region is a superowner - they cannot be removed as an owner - they can only TRANSFER the region to another player, which returns them an anchor credit and takes an anchor credit from the other player

    public static final String CALC_CHUNK_INDEX_SUBCOMMAND = "calc-chunk-index";
    public static final String CREATE_TEST_CHUNK_REGION_SUBCOMMAND = "create-test-chunk-region";
    public static final String CREATE_REGION_WITH_CONFIRMATION_SUBCOMMAND = "create-region-with-confirmation";
    public static final String UNCLAIM_CHUNK_SUBCOMMAND = "unclaim-chunk";
    public static final String INDICATE_SUBCOMMAND = "indicate";
    private static final List<String> SUBCOMMANDS = List.of(
            CALC_CHUNK_INDEX_SUBCOMMAND,
            CREATE_TEST_CHUNK_REGION_SUBCOMMAND,
            CREATE_REGION_WITH_CONFIRMATION_SUBCOMMAND,
            UNCLAIM_CHUNK_SUBCOMMAND,
            INDICATE_SUBCOMMAND
    );

    private static IntegerFlag NUMBER_OF_CHUNKS_FLAG;

    private final RegionContainer regionContainer;
    private final ClaimTestingRegionIndicator claimTestingRegionIndicator;

    public static void registerCustomWorldguardFlags() {
        FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
        final String NUMBER_OF_CHUNKS_FLAG_NAME = "number-of-chunks";
        try {
            NUMBER_OF_CHUNKS_FLAG = new IntegerFlag(NUMBER_OF_CHUNKS_FLAG_NAME);
            flagRegistry.register(NUMBER_OF_CHUNKS_FLAG);
        } catch (FlagConflictException fce) {
            Bukkit.getLogger().severe("Could not register our custom flag!");
        } catch (IllegalStateException ise) {
            // the plugin is being loaded after worldguard
            NUMBER_OF_CHUNKS_FLAG = (IntegerFlag) flagRegistry.get(NUMBER_OF_CHUNKS_FLAG_NAME);
        }
    }

    public ClaimTestingCommand(ClaimTestingRegionIndicator claimTestingRegionIndicator) {
        this.claimTestingRegionIndicator = claimTestingRegionIndicator;
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

        UUID claimingPlayersId = player.getUniqueId();

        Chunk chunk = player.getLocation().getChunk();

        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(chunk.getWorld()));

        if (regionManager == null) {
            player.sendMessage(ChatColor.DARK_RED + "Could not load region manager for this world! Could not save newly created region!");
            return true;
        }

        switch (args[0]) {
            // takes a given location (the player's location) and calculates the chunk index
            case CALC_CHUNK_INDEX_SUBCOMMAND -> {
                Block chunkMinBlock = chunk.getBlock(0, player.getWorld().getMinHeight(), 0);
                Block chunkMaxBlock = chunk.getBlock(15, player.getWorld().getMaxHeight(), 15);
                player.sendMessage(String.format("Chunk (%d, %d)", chunk.getX(), chunk.getZ()));
                player.sendMessage(String.format("Chunk min block (%d, %d)", chunkMinBlock.getX(), chunkMinBlock.getZ()));
                player.sendMessage(String.format("Chunk max block (%d, %d)", chunkMaxBlock.getX(), chunkMaxBlock.getZ()));
            }
            // create a chunk region at the chunk the player is in - doesn't create a parent region nor verify that the chunk is not claimed yet
            case CREATE_TEST_CHUNK_REGION_SUBCOMMAND -> {
                ProtectedRegion testChunkRegion = createChunkRegion(chunk);

                player.sendMessage(ChatColor.LIGHT_PURPLE + "Created a region for " + testChunkRegion.getId());

                regionManager.addRegion(testChunkRegion);
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Saved region to this world's region list");
            }
            case CREATE_REGION_WITH_CONFIRMATION_SUBCOMMAND -> {
                String chunkRegionName = getChunkRegionName(chunk);

                // Validate this is an unclaimed chunk
                if (regionManager.hasRegion(chunkRegionName)) {
                    player.sendMessage(ChatColor.RED + "You cannot claim this chunk as it is already claimed!");
                    return true;
                }

                // Check if it is adjacent to other chunk regions that are also owned by the claiming player (we'll deal with other folks claims later)
                List<Direction> directionsWithAdjacentChunkRegionsAlsoOwnedByClaimingPlayer =
                        Arrays.stream(Direction.values())
                                .filter(direction -> hasAdjacentChunkRegionOwnedByPlayer(regionManager, claimingPlayersId, chunk, direction))
                                .toList();

                if (directionsWithAdjacentChunkRegionsAlsoOwnedByClaimingPlayer.isEmpty()) {
                    // This is establishing a new region!!!!
                    if (!isConfirmed(args, player)) {
                        sendConfirmationPrompt(player,
                                CREATE_REGION_WITH_CONFIRMATION_SUBCOMMAND,
                                "claim this chunk - doing so will consume one of your allocated regions.",
                                "establish a region beginning with this chunk.",
                                "that you want to claim this chunk"
                                );
                        return true;
                    }

                    ProtectedRegion newParentRegion = createParentRegion(claimingPlayersId, regionManager);

                    ProtectedRegion newChunkRegion = createChunkRegion(chunk);
                    try {
                        newChunkRegion.setParent(newParentRegion);
                    } catch (ProtectedRegion.CircularInheritanceException e) {
                        // This should never actually be thrown since we're only parenting one freshly created region to another
                        throw new AssertionError(e);
                    }

                    newParentRegion.setFlag(NUMBER_OF_CHUNKS_FLAG, 1);

                    regionManager.addRegion(newChunkRegion);
                    regionManager.addRegion(newParentRegion);

                    player.sendMessage(ChatColor.AQUA + "Established a new region under your name!");
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "Debug: Parent region: " + newParentRegion.getId());
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "Debug: Chunk region: " + newChunkRegion.getId());
                } else {
                    // We are appending to an existing region!
                    // There are three things we can find
                    // 1. A region owned by the claiming player
                    // 2. A SECOND region ALSO owned by the claiming player
                    // 3. A claim owned by someone else

                    // Right now, we'll deal with the case of only finding a single region owned by the claiming player

                    Direction firstAdjacentChunkRegionAlsoOwnedByClaimingPlayerDirection = directionsWithAdjacentChunkRegionsAlsoOwnedByClaimingPlayer.get(0);
                    ProtectedRegion firstAdjacentChunkRegionAlsoOwnedByClaimingPlayer = getAdjacentChunkRegion(regionManager, chunk, firstAdjacentChunkRegionAlsoOwnedByClaimingPlayerDirection);
                    ProtectedRegion parentRegion = firstAdjacentChunkRegionAlsoOwnedByClaimingPlayer.getParent();

                    ProtectedRegion newChunkRegion = createChunkRegion(chunk);
                    try {
                        newChunkRegion.setParent(parentRegion);
                    } catch (ProtectedRegion.CircularInheritanceException e) {
                        // This should never actually be thrown since we're only parenting a freshly created region to an existing region
                        throw new AssertionError(e);
                    }

                    int numberOfChunksInParentRegionBefore = parentRegion.getFlag(NUMBER_OF_CHUNKS_FLAG);
                    // TODO right now we're asserting that the number of claims in the adjacent region is always set, and set correctly
                    // Ideally, we'd have some sort of region verification/error correction procedure that can be run whenever a flag
                    // that is expected is not actually there, or otherwise appears to be incorrect/invalid
                    int numberOfChunksInParentRegionAfter = numberOfChunksInParentRegionBefore + 1;
                    parentRegion.setFlag(NUMBER_OF_CHUNKS_FLAG, numberOfChunksInParentRegionAfter);

                    player.sendMessage(ChatColor.AQUA + "Claimed a chunk and added it to existing region '" + parentRegion.getId() + "'");
                    player.sendMessage(ChatColor.AQUA + "The region now has " + numberOfChunksInParentRegionAfter + " chunks in it.");

                    // TODO case 2 and 3
                }

//            player.sendMessage(ChatColor.RED + "You cannot claim this chunk as it is too close to another claim to the north that is not yours!");
//            return true;

            }
            case UNCLAIM_CHUNK_SUBCOMMAND -> {
                ProtectedRegion chunkRegion = regionManager.getRegion(getChunkRegionName(chunk));
                if (chunkRegion == null) {
                    player.sendMessage(ChatColor.RED + "You cannot unclaim this chunk as it is not claimed!");
                    return true;
                }

                ProtectedRegion parentRegion = chunkRegion.getParent();
                if (parentRegion == null) {
                    // This shouldn't happen, but should fail gracefully/quietly if it does
                    player.sendMessage(ChatColor.RED + "You cannot unclaim this chunk (something is wrong with it!)!");
                    return true;
                }

                // TODO this should check co-ownership/permission to unclaim properly, instead of just the parent region name
                if (!parentRegion.getId().startsWith(claimingPlayersId.toString())) {
                    player.sendMessage(ChatColor.RED + "You cannot unclaim this chunk as it does not belong to you!");
                    return true;
                }

                if (!isConfirmed(args, player)) {
                    sendConfirmationPrompt(player,
                            UNCLAIM_CHUNK_SUBCOMMAND,
                            "unclaim this chunk - doing so will mean it is no longer protected.",
                            "unclaim this chunk.",
                            "that you want to unclaim this chunk"
                    );
                    return true;
                }

                int numberOfChunksInParentRegionBefore = parentRegion.getFlag(NUMBER_OF_CHUNKS_FLAG);
                // TODO right now we're asserting that the number of claims in the adjacent region is always set, and set correctly
                if (numberOfChunksInParentRegionBefore > 1) {
                    int numberOfChunksInParentRegionAfter = numberOfChunksInParentRegionBefore - 1;
                    parentRegion.setFlag(NUMBER_OF_CHUNKS_FLAG, numberOfChunksInParentRegionAfter);

                    regionManager.removeRegion(chunkRegion.getId());

                    player.sendMessage(ChatColor.AQUA + "Unclaimed a chunk in the region '" + parentRegion.getId() + "'");
                    player.sendMessage(ChatColor.AQUA + "The region now has " + numberOfChunksInParentRegionAfter + " chunks in it.");
                } else {
                    // TODO unclaim a parent region - issue a warning and require a DOUBLE confirmation
                    if (!isDoubleConfirmedForParentRegionDeletion(args, player)) {
                        sendDoubleConfirmationForParentRegionDeletionPrompt(player);
                        return true;
                    }

                    regionManager.removeRegion(chunkRegion.getId());
                    regionManager.removeRegion(parentRegion.getId());

                    player.sendMessage(ChatColor.AQUA + "Unclaimed the last chunk in and deleted the region '" + parentRegion.getId() + "'!!");
                }
            }
            case INDICATE_SUBCOMMAND -> {
                claimTestingRegionIndicator.toggle(player);
            }
            default -> {
                player.sendMessage(ChatColor.RED + "Subcommand not recognized");
                return true;
            }
        }

        try {
            regionManager.saveChanges();
        } catch (StorageException e) {
            e.printStackTrace();
        }

        return true;
    }

    @NotNull
    private ProtectedRegion createParentRegion(UUID claimingPlayersId, RegionManager regionManager) {
        int personalRegionIndex = 1;
        while (regionManager.hasRegion(String.format("%s-%d", claimingPlayersId, personalRegionIndex))) {
            ++personalRegionIndex;
        }
        // TODO is this guaranteed to be the only hyphen? It's probably more useful to be able to know it's a unique character in the string. This should also be isolated logic in its own util logic for region management/claiming
        String newParentRegionName = String.format("%s-%d", claimingPlayersId, personalRegionIndex);
        ProtectedRegion newParentRegion = new GlobalProtectedRegion(newParentRegionName);
        return newParentRegion;
    }

    private ProtectedCuboidRegion createChunkRegion(Chunk chunk) {
        String chunkRegionName = getChunkRegionName(chunk);
        Block chunkMinBlock = chunk.getBlock(0, chunk.getWorld().getMinHeight(), 0);
        Block chunkMaxBlock = chunk.getBlock(15, chunk.getWorld().getMaxHeight(), 15);
        BlockVector3 min = BukkitAdapter.asBlockVector(chunkMinBlock.getLocation());
        BlockVector3 max = BukkitAdapter.asBlockVector(chunkMaxBlock.getLocation());
        return new ProtectedCuboidRegion(chunkRegionName, min, max);
    }

    private String getChunkRegionName(Chunk chunk) {
        return String.format("claim_%d_%d", chunk.getX(), chunk.getZ());
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
        ProtectedRegion adjacentChunkRegion = getAdjacentChunkRegion(regionManager, chunk, direction);
        return adjacentChunkRegion != null && adjacentChunkRegion.getId().startsWith(playersUUID.toString());
    }

    private enum Direction { NORTH(0,-1), EAST(1,0), SOUTH(0,1), WEST(-1,0);
        private final int x, z;

        Direction(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }

    private void sendConfirmationPrompt(Player player, String subcommand, String promptAreYouSureYouWantTo, String promptClickConfirmToContinueAnd, String buttonHoverTooltipClickHereToConfirm) {
        Component confirmationMessage1 = Component.text("Are you sure you want to " + promptAreYouSureYouWantTo).color(NamedTextColor.RED);
        Component confirmButton = Component
                .text("CONFIRM")
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD)
                .hoverEvent(Component.text("Click here to confirm " + buttonHoverTooltipClickHereToConfirm).asHoverEvent())
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/claimtest " + subcommand + " confirm " + player.getName()));
        Component confirmationMessage2 = Component
                .text("Click ").color(NamedTextColor.RED)
                .append(confirmButton)
                .append(Component.text(" to continue and " + promptClickConfirmToContinueAnd).color(NamedTextColor.RED));
        player.sendMessage(confirmationMessage1);
        player.sendMessage(confirmationMessage2);
    }

    private boolean isConfirmed(String[] args, Player player) {
        return args.length >= 3 && args[1].equalsIgnoreCase("confirm") && args[2].equalsIgnoreCase(player.getName());
    }

    private void sendDoubleConfirmationForParentRegionDeletionPrompt(Player player) {
        Component confirmationMessage1part1 = Component.text("Are you ").color(NamedTextColor.RED);
        Component confirmationMessage2part2 = Component.text("absolutely").color(NamedTextColor.RED).decorate(TextDecoration.ITALIC);
        Component confirmationMessage3part3 = Component.text(" sure you want to unclaim this chunk?").color(NamedTextColor.RED);
        Component confirmationMessage1 = confirmationMessage1part1.append(confirmationMessage2part2).append(confirmationMessage3part3);
        Component explanationMessage = Component.text("As this is the last chunk in your region, the region itself, including all owner, member, and flag information will be deleted if you unclaim this chunk!").color(NamedTextColor.RED);
        Component confirmButton = Component
                .text("I'M SURE")
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD)
                .hoverEvent(Component.text("Click here to confirm you wish to unclaim this chunk and delete this region").asHoverEvent())
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/claimtest " + UNCLAIM_CHUNK_SUBCOMMAND + " confirm " + player.getName() + " imsure " + player.getName()));
        Component confirmationMessage2 = Component
                .text("Click ").color(NamedTextColor.RED)
                .append(confirmButton)
                .append(Component.text(" to continue and unclaim this last chunk and delete your region").color(NamedTextColor.RED));
        player.sendMessage(confirmationMessage1);
        player.sendMessage(explanationMessage);
        player.sendMessage(confirmationMessage2);
    }

    private boolean isDoubleConfirmedForParentRegionDeletion(String[] args, Player player) {
        return isConfirmed(args, player) && args.length >= 5 && args[3].equalsIgnoreCase("imsure") && args[4].equalsIgnoreCase(player.getName());
    }

}


