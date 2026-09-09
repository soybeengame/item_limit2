package com.example.itemlimit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemLimitPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private InventoryLimiter inventoryLimiter;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        configManager.load();

        inventoryLimiter = new InventoryLimiter(configManager);

        getServer().getPluginManager().registerEvents(new LimitListener(this, inventoryLimiter), this);

        ItemLimitCommand commandExecutor = new ItemLimitCommand(this, configManager);
        getCommand("itemlimit").setExecutor(commandExecutor);
        getCommand("itemlimit").setTabCompleter(commandExecutor);

        // イベントで拾いきれないケースへの保険として、定期的に全員をチェック
        int interval = Math.max(20, configManager.getCheckIntervalTicks());
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                inventoryLimiter.enforce(player);
            }
        }, interval, interval);

        getLogger().info("ItemLimit を有効化しました。");
    }

    @Override
    public void onDisable() {
        getLogger().info("ItemLimit を無効化しました。");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public InventoryLimiter getInventoryLimiter() {
        return inventoryLimiter;
    }
}
