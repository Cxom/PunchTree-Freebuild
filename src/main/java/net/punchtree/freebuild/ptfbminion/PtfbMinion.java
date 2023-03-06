package net.punchtree.freebuild.ptfbminion;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;

import java.util.concurrent.ExecutorService;

public class PtfbMinion extends ListenerAdapter {
    private JDA jda;
    private final String token;
    private final ExecutorService executorService;
    private TextChannel crossServerChatChannel;

    public PtfbMinion(String token, ExecutorService executorService) {
        Bukkit.getLogger().warning("creating PtfbMinion");
        this.token = token;
        this.executorService = executorService;
    }

    public void start() {
        executorService.submit(() -> {
            jda = JDABuilder.createDefault(token).addEventListeners(this, new OnDiscordMessage()).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
            try {
                crossServerChatChannel = jda.awaitReady().getTextChannelById("1081694704292335697");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void stop() {
        if(jda != null) {
            jda.shutdown();
        }
        executorService.shutdown();
    }

    @Override
    public void onReady(ReadyEvent event) {
        event.getJDA().getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("Minecraft"));
    }

    public TextChannel getCrossServerChatChannel() {
        return crossServerChatChannel;
    }

    public void execute(Runnable runnable) {
        executorService.submit(runnable);
    }
}
