package net.punchtree.freebuild.arbor;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class ArborOnDiscordMessage implements EventListener {

    Arbor arbor = PunchTreeFreebuildPlugin.getArbor();

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if(event instanceof MessageReceivedEvent messageReceivedEvent) {
            if(messageReceivedEvent.getAuthor().isBot()
                    || messageReceivedEvent.isWebhookMessage()
                    || messageReceivedEvent.getMember() == null
                    || messageReceivedEvent.getChannel().getIdLong() != arbor.getCrossServerChatChannel().getIdLong()) {
                return;
            }
            String memberName = messageReceivedEvent.getMember().getEffectiveName();
            String plainMessage = MessageUtils.sanitizeMessage(messageReceivedEvent.getMessage().getContentStripped());
            messageReceivedEvent.getMessage().delete().queue();
            if(plainMessage.isEmpty() || MessageUtils.containsHyperlink(plainMessage)) return;

            messageReceivedEvent.getChannel().sendMessage("**Discord | " + memberName + " > **" + plainMessage).queue();
            Bukkit.getServer().sendMessage(Component.text("Discord", TextColor.fromHexString("#7289DA"))
                    .append(Component.text(" | ", NamedTextColor.GRAY))
                    .append(Component.text(memberName, NamedTextColor.WHITE))
                    .append(Component.text(" > ", NamedTextColor.GRAY))
                    .append(Component.text(plainMessage, NamedTextColor.WHITE)));
        }
    }
}
