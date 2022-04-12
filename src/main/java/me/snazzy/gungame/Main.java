package me.snazzy.gungame;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        System.out.println("GunGame has loaded");
        getServer().getPluginManager().registerEvents(new GunGame(), this);
        getCommand("ready").setExecutor(new GunGame());
        getCommand("unready").setExecutor(new GunGame());
        getCommand("force").setExecutor(new GunGame());
        getCommand("fix").setExecutor(new GunGame());
    }
}
