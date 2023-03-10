package net.punchtree.freebuild.arbor;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ArborOnPlayerLeave implements Listener {
    Arbor arbor = PunchTreeFreebuildPlugin.getArbor();
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        arbor.execute(() -> {
            String displayName = PlainTextComponentSerializer.plainText().serialize(event.getPlayer().displayName());
            arbor.getCrossServerChatChannel().sendMessage("```diff" + "\n- " + displayName + "\n```").queue();
        });
    }
}