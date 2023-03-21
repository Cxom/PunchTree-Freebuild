package net.punchtree.freebuild.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PtfbPlayerOnPlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PtfbPlayer ptfbPlayer = PtfbPlayer.get(event.getPlayer());

        ptfbPlayer.withDataLoaded(loadedPtfbPlayer -> {
            if(loadedPtfbPlayer.getPlayerData("", "uuid").isPresent()) return;
            loadedPtfbPlayer.setPlayerData("", "uuid", ptfbPlayer.getUuid().toString());
            loadedPtfbPlayer.saveData();
        });
    }
}