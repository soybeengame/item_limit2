package com.example.itemlimit;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * config.yml の読み込み・保存・保持を担当するクラス。
 */
public class ConfigManager {

    private final JavaPlugin plugin;
    private final Map<Material, Integer> limits = new EnumMap<>(Material.class);
    private int checkIntervalTicks = 20;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * config.yml を読み込み（再読み込み）します。
     */
    public void load() {
        plugin.reloadConfig();
        limits.clear();

        checkIntervalTicks = plugin.getConfig().getInt("check-interval-ticks", 20);

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("limits");
        if (section == null) {
            plugin.getLogger().warning("config.yml に limits セクションが見つかりません。");
            return;
        }

        for (String key : section.getKeys(false)) {
            Material material = Material.matchMaterial(key);
            if (material == null) {
                plugin.getLogger().log(Level.WARNING, "不明なアイテム名です: {0}", key);
                continue;
            }
            int max = section.getInt(key);
            if (max <= 0) {
                plugin.getLogger().log(Level.WARNING, "{0} の所持制限は1以上にしてください。", key);
                continue;
            }
            limits.put(material, max);
        }
    }

    public Map<Material, Integer> getLimits() {
        return limits;
    }

    public Integer getLimit(Material material) {
        return limits.get(material);
    }

    public void setLimit(Material material, int amount) {
        limits.put(material, amount);
        plugin.getConfig().set("limits." + material.name(), amount);
        plugin.saveConfig();
    }

    public boolean removeLimit(Material material) {
        boolean existed = limits.remove(material) != null;
        plugin.getConfig().set("limits." + material.name(), null);
        plugin.saveConfig();
        return existed;
    }

    public int getCheckIntervalTicks() {
        return checkIntervalTicks;
    }
}
