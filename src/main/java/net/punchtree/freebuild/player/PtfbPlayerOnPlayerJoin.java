package net.punchtree.freebuild.player;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PtfbPlayerOnPlayerJoin implements Listener {
    private static final PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PtfbPlayer ptfbPlayer = PtfbPlayer.get(event.getPlayer());
        PlayerDataHandler playerDataHandler = ptfbPlayer.getPlayerDataHandler();

        playerDataHandler.withDataLoaded(handler -> {
            if(handler.getPlayerData("", "uuid").isPresent()) return;
            handler.setPlayerData("", "uuid", ptfbPlayer.getUuid().toString());
            handler.saveData();
        });
    }
}