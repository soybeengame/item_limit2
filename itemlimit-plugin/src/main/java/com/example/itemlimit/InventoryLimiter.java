package com.example.itemlimit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

/**
 * プレイヤーのインベントリを走査し、所持制限を超えているアイテムがあれば
 * 超過分を取り除いてその場にドロップする処理を行うクラス。
 */
public class InventoryLimiter {

    private final ConfigManager configManager;

    public InventoryLimiter(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void enforce(Player player) {
        if (configManager.getLimits().isEmpty()) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        Map<Material, Integer> totals = new HashMap<>();

        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            totals.merge(item.getType(), item.getAmount(), Integer::sum);
        }

        for (Map.Entry<Material, Integer> entry : totals.entrySet()) {
            Material material = entry.getKey();
            int total = entry.getValue();
            Integer limit = configManager.getLimit(material);
            if (limit == null || total <= limit) continue;

            int toRemove = total - limit;
            int notRemoved = removeAmount(inventory, material, toRemove);
            int actuallyDropped = toRemove - notRemoved;

            if (actuallyDropped > 0) {
                dropExcess(player, material, actuallyDropped);
            }
        }
    }

    /**
     * インベントリ後方のスロットから優先して amount 分だけ取り除きます。
     * 取り除けなかった残数（通常は0）を返します。
     */
    private int removeAmount(Inventory inventory, Material material, int amount) {
        ItemStack[] contents = inventory.getContents();
        for (int i = contents.length - 1; i >= 0 && amount > 0; i--) {
            ItemStack item = contents[i];
            if (item == null || item.getType() != material) continue;

            int stackAmount = item.getAmount();
            if (stackAmount <= amount) {
                amount -= stackAmount;
                inventory.setItem(i, null);
            } else {
                item.setAmount(stackAmount - amount);
                amount = 0;
            }
        }
        return amount;
    }

    private void dropExcess(Player player, Material material, int amount) {
        Location loc = player.getLocation();
        int maxStack = Math.max(1, material.getMaxStackSize());

        while (amount > 0) {
            int stackSize = Math.min(amount, maxStack);
            Item dropped = player.getWorld().dropItem(loc, new ItemStack(material, stackSize));
            // すぐに拾い直して無限ループにならないよう、少し拾えない時間を設ける
            dropped.setPickupDelay(40);
            amount -= stackSize;
        }

        player.sendMessage("§e[ItemLimit] §f" + material.name()
                + " が所持制限を超えたため、超過分をその場にドロップしました。");
    }
}
