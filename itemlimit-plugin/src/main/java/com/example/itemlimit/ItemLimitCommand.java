package com.example.itemlimit;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * /itemlimit コマンドの処理（reload / set / remove / list）。
 */
public class ItemLimitCommand implements CommandExecutor, TabCompleter {

    private final ConfigManager configManager;

    public ItemLimitCommand(ItemLimitPlugin plugin, ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e/itemlimit reload §f- 設定を再読み込み");
            sender.sendMessage("§e/itemlimit set <アイテム名> <上限数> §f- 上限を設定");
            sender.sendMessage("§e/itemlimit remove <アイテム名> §f- 上限を解除");
            sender.sendMessage("§e/itemlimit list §f- 現在の設定一覧を表示");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                configManager.load();
                sender.sendMessage("§a[ItemLimit] 設定を再読み込みしました。");
            }
            case "list" -> {
                Map<Material, Integer> limits = configManager.getLimits();
                if (limits.isEmpty()) {
                    sender.sendMessage("§e[ItemLimit] 現在、制限は設定されていません。");
                } else {
                    sender.sendMessage("§a[ItemLimit] 現在の所持制限:");
                    limits.forEach((material, max) ->
                            sender.sendMessage("§f- " + material.name() + ": " + max));
                }
            }
            case "set" -> handleSet(sender, args);
            case "remove" -> handleRemove(sender, args);
            default -> sender.sendMessage("§c不明なサブコマンドです。/itemlimit で使い方を確認してください。");
        }
        return true;
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§c使い方: /itemlimit set <アイテム名> <上限数>");
            return;
        }
        Material material = Material.matchMaterial(args[1]);
        if (material == null) {
            sender.sendMessage("§c不明なアイテム名です: " + args[1]);
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c上限数は数値で指定してください。");
            return;
        }
        if (amount <= 0) {
            sender.sendMessage("§c上限数は1以上にしてください。");
            return;
        }
        configManager.setLimit(material, amount);
        sender.sendMessage("§a[ItemLimit] " + material.name() + " の上限を " + amount + " に設定しました。");
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§c使い方: /itemlimit remove <アイテム名>");
            return;
        }
        Material material = Material.matchMaterial(args[1]);
        if (material == null) {
            sender.sendMessage("§c不明なアイテム名です: " + args[1]);
            return;
        }
        boolean removed = configManager.removeLimit(material);
        sender.sendMessage(removed
                ? "§a[ItemLimit] " + material.name() + " の制限を解除しました。"
                : "§e[ItemLimit] " + material.name() + " には制限が設定されていません。");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.addAll(List.of("reload", "set", "remove", "list"));
            return filter(options, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            options.addAll(configManager.getLimits().keySet().stream()
                    .map(Material::name).collect(Collectors.toList()));
            return filter(options, args[1]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            for (Material m : Material.values()) {
                if (m.isItem()) options.add(m.name());
            }
            return filter(options, args[1]);
        }
        return options;
    }

    private List<String> filter(List<String> options, String prefix) {
        String upper = prefix.toUpperCase();
        return options.stream()
                .filter(o -> o.toUpperCase().startsWith(upper))
                .collect(Collectors.toList());
    }
}
