package net.punchtree.freebuild.ptfbminion;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class OnDiscordMessage implements EventListener {

    PtfbMinion ptfbMinion = PunchTreeFreebuildPlugin.getPtfbMinion();

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if(event instanceof MessageReceivedEvent messageReceivedEvent) {
            if(messageReceivedEvent.getAuthor().isBot()
                    || messageReceivedEvent.isWebhookMessage()
                    || messageReceivedEvent.getMember() == null
                    || messageReceivedEvent.getChannel().getIdLong() != ptfbMinion.getCrossServerChatChannel().getIdLong()) {
                return;
            }
            String memberName = messageReceivedEvent.getMember().getEffectiveName();
            String plainMessage = sanitizeMessage(messageReceivedEvent.getMessage());
            messageReceivedEvent.getMessage().delete().queue();
            if(plainMessage.isEmpty()) return;

            messageReceivedEvent.getChannel().sendMessage("**Discord | " + memberName + " > **" + plainMessage).queue();
            Bukkit.getServer().sendMessage(Component.text("Discord", TextColor.fromHexString("#7289DA"))
                    .append(Component.text(" | ", NamedTextColor.GRAY))
                    .append(Component.text(memberName, NamedTextColor.WHITE))
                    .append(Component.text(" > ", NamedTextColor.GRAY))
                    .append(Component.text(plainMessage, NamedTextColor.WHITE)));
        }
    }

    public String sanitizeMessage(Message message) {
        String sanitized = message.getContentStripped().codePoints()
                .filter(cp -> Character.UnicodeBlock.of(cp).equals(Character.UnicodeBlock.BASIC_LATIN))
                .mapToObj(Character::toString)
                .collect(Collectors.joining())
                .strip();
        return sanitized.substring(0, Math.min(sanitized.length(), 256));
    }
}
