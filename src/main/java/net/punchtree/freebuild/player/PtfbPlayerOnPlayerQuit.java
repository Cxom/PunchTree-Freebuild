package net.punchtree.freebuild.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PtfbPlayerOnPlayerQuit implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PtfbPlayer ptfbPlayer = PtfbPlayer.get(event.getPlayer());
        PlayerDataHandler playerDataHandler = ptfbPlayer.getPlayerDataHandler();
        playerDataHandler.withDataLoaded(PlayerDataHandler::saveData);
        PtfbPlayer.remove(ptfbPlayer);
    }
}
