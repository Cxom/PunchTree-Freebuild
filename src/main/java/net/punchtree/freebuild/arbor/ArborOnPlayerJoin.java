package net.punchtree.freebuild.arbor;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ArborOnPlayerJoin implements Listener {
    Arbor arbor = PunchTreeFreebuildPlugin.getArbor();
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(PunchTreeFreebuildPlugin.getInstance(), () -> {
            arbor.execute(() -> {
                String displayName = PlainTextComponentSerializer.plainText().serialize(event.getPlayer().displayName());
                arbor.getCrossServerChatChannel().sendMessage("```diff" + "\n+ " + displayName + "\n```").queue();
            });
        }, 10L);
    }
}
