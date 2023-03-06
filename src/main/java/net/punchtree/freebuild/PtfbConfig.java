package net.punchtree.freebuild;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PtfbConfig {
    private String discordToken;

    public PtfbConfig(JavaPlugin plugin) {
        load(plugin);
    }

    public String getDiscordToken() {
        return discordToken;
    }

    private void load(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            plugin.saveResource("config.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        this.discordToken = config.getString("discord.token");
    }
}
