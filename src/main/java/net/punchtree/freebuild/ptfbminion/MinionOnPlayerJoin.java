package net.punchtree.freebuild.ptfbminion;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MinionOnPlayerJoin implements Listener {
    PtfbMinion ptfbMinion = PunchTreeFreebuildPlugin.getPtfbMinion();
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(PunchTreeFreebuildPlugin.getInstance(), () -> {
            ptfbMinion.execute(() -> {
                String displayName = PlainTextComponentSerializer.plainText().serialize(event.getPlayer().displayName());
                ptfbMinion.getCrossServerChatChannel().sendMessage("```diff" + "\n+ " + displayName + "\n```").queue();
            });
        }, 10L);
    }
}
