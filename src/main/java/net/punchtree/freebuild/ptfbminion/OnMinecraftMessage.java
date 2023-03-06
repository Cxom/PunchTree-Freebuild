package net.punchtree.freebuild.ptfbminion;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OnMinecraftMessage implements Listener {
    PtfbMinion ptfbMinion = PunchTreeFreebuildPlugin.getPtfbMinion();

    @EventHandler
    public void onMinecraftMessage(AsyncChatEvent event) {
        ptfbMinion.execute(() -> {
            String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
            String plainDisplayName = PlainTextComponentSerializer.plainText().serialize(event.getPlayer().displayName());
            ptfbMinion.getCrossServerChatChannel().sendMessage("**" + plainDisplayName + " > **" + plainMessage).queue();
        });
    }
}
