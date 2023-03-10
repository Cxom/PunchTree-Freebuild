package net.punchtree.freebuild.arbor;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.concurrent.ExecutorService;

public class Arbor extends ListenerAdapter {
    private JDA jda;
    private final String token;
    private final String channelID;
    private final ExecutorService executorService;
    private TextChannel crossServerChatChannel;

    public Arbor(String token, String channelID, ExecutorService executorService) {
        this.token = token;
        this.channelID = channelID;
        this.executorService = executorService;
    }

    public void start() {
        jda = JDABuilder.createDefault(token).addEventListeners(this, new ArborOnDiscordMessage()).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
        executorService.submit(() -> {
            try {
                crossServerChatChannel = jda.awaitReady().getTextChannelById(channelID);
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
        event.getJDA().getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("Freebuild"));
    }

    public TextChannel getCrossServerChatChannel() {
        return crossServerChatChannel;
    }

    public void execute(Runnable runnable) {
        executorService.submit(runnable);
    }
}
