package net.punchtree.freebuild.afk;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class RosterManager {
    private static final Map<String, Roster> rosters = new HashMap<>();
    private static final Team afkTeam;

    static {
        if(Bukkit.getScoreboardManager().getMainScoreboard().getTeam("afk") == null) {
            afkTeam = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("afk");
        } else {
            afkTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("afk");
        }
        assert afkTeam != null;
        afkTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        rosters.put("afk", new Roster(new HashSet<>(),
                UUID -> {
                    Player player = Bukkit.getPlayer(UUID);
                    if(player == null) return;
                    String playerDisplayName = PlainTextComponentSerializer.plainText().serialize(player.displayName());
                    player.setInvulnerable(true);
                    player.setGravity(false);
                    player.setCanPickupItems(false);
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    player.setSleepingIgnored(true);
                    Bukkit.getServer().sendMessage(Component.text(playerDisplayName + " is now AFK.", NamedTextColor.DARK_GRAY));
                    afkTeam.addPlayer(player);
                    AfkPlayerListener.clearActivity(player);
                },
                UUID -> {
                    Player player = Bukkit.getPlayer(UUID);
                    if(player == null) return;
                    String playerDisplayName = PlainTextComponentSerializer.plainText().serialize(player.displayName());
                    player.setInvulnerable(false);
                    player.setGravity(true);
                    player.setCanPickupItems(true);
                    player.setSleepingIgnored(false);
                    if(player.getGameMode().equals(GameMode.SURVIVAL)) {
                        player.setAllowFlight(false);
                        player.setFlying(false);
                    }
                    Bukkit.getScheduler().runTask(PunchTreeFreebuildPlugin.getInstance(), () -> {
                        if(player.isOnline()) {
                            Bukkit.getServer().sendMessage(Component.text(playerDisplayName + " is no longer AFK.", NamedTextColor.DARK_GRAY));
                        }
                    });
                    afkTeam.removePlayer(player);
                    AfkPlayerListener.updateLastActivity(player);
                }
        ));
    }

    public static Roster getRoster(String name) {
        return rosters.get(name);
    }
}