package net.punchtree.freebuild.arbor;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ArborOnAsyncChat implements Listener {
    Arbor arbor = PunchTreeFreebuildPlugin.getArbor();

    @EventHandler
    public void onMinecraftMessage(AsyncChatEvent event) {
        arbor.execute(() -> {
            String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
            String plainDisplayName = PlainTextComponentSerializer.plainText().serialize(event.getPlayer().displayName());
            arbor.getCrossServerChatChannel().sendMessage(MessageUtils.escapeEmojis("**" + plainDisplayName + " > **" + plainMessage)).queue();
        });
    }
}
