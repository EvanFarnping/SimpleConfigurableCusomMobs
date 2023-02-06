package me.evanf.simplecustommobs;

import me.evanf.simplecustommobs.commands.SpawnMobs;
import me.evanf.simplecustommobs.mobs.CustomIllusioner;
import me.evanf.simplecustommobs.mobs.CustomSpider;
import me.evanf.simplecustommobs.mobs.CustomWitherSkeleton;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleCustomMobs extends JavaPlugin {

    public static SimpleCustomMobs plugin;
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        // Config
        plugin = this;
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = this.getConfig();
        this.saveDefaultConfig();

        // Events
        this.getServer().getPluginManager().registerEvents(
                new CustomSpider(this), this);
        this.getServer().getPluginManager().registerEvents(
                new CustomWitherSkeleton(this), this);
        this.getServer().getPluginManager().registerEvents(
                new CustomIllusioner(this), this);

        // Commands
        this.getCommand("spawncustomspider").setExecutor(new SpawnMobs());
        this.getCommand("spawncustomwitherskeleton").setExecutor(new SpawnMobs());
        this.getCommand("spawncustomillusioner").setExecutor(new SpawnMobs());

        this.getLogger().info("Enable Simple Custom Mobs Plugin " + this.getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Disabling Simple Custom Mobs Plugin " + this.getDescription().getVersion());
    }
}
