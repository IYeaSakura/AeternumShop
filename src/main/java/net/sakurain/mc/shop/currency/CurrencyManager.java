package net.sakurain.mc.shop.currency;

import net.sakurain.mc.shop.AeternumShop;
import net.sakurain.mc.shop.model.MailboxEntry;
import net.sakurain.mc.shop.model.MailboxType;
import net.sakurain.mc.shop.util.InventoryUtil;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CurrencyManager {

    private final AeternumShop plugin;
    private CurrencyUnit baseCurrency;
    private List<CurrencyBundle> bundles;

    public CurrencyManager(AeternumShop plugin) {
        this.plugin = plugin;
        this.bundles = new ArrayList<>();
    }

    public void loadFromConfig(YamlConfiguration config) {
        this.bundles.clear();

        String baseId = config.getString("currency.base.id", "diamond");
        Material baseMaterial = Material.matchMaterial(config.getString("currency.base.material", "DIAMOND"));
        if (baseMaterial == null) {
            baseMaterial = Material.DIAMOND;
        }
        String baseName = config.getString("currency.base.display-name", "钻石");
        this.baseCurrency = new CurrencyUnit(baseId, baseMaterial, baseName);

        List<Map<?, ?>> bundleList = config.getMapList("currency.bundles");
        if (bundleList != null) {
            for (Map<?, ?> map : bundleList) {
                @SuppressWarnings("unchecked")
                Map<String, Object> bundleMap = (Map<String, Object>) map;
                String id = String.valueOf(bundleMap.get("id"));
                Material material = Material.matchMaterial(String.valueOf(bundleMap.get("material")));
                String displayName = String.valueOf(bundleMap.get("display-name"));
                int baseEquivalent;
                try {
                    baseEquivalent = Integer.parseInt(String.valueOf(bundleMap.get("base-equivalent")));
                } catch (NumberFormatException e) {
                    baseEquivalent = 1;
                }
                Object baseUnitObj = bundleMap.get("base-unit");
                String baseUnitId = baseUnitObj != null ? String.valueOf(baseUnitObj) : baseId;

                if (material == null || material == Material.AIR || "null".equals(id)) {
                    continue;
                }
                if (!baseUnitId.equalsIgnoreCase(baseId)) {
                    plugin.getLogger().warning("Currency bundle " + id + " references unknown base unit " + baseUnitId);
                    continue;
                }
                bundles.add(new CurrencyBundle(id, material, displayName, baseEquivalent, baseCurrency));
            }
        }

        bundles.sort(Comparator.comparingInt(CurrencyBundle::getBaseValue).reversed());
        plugin.getLogger().info("Loaded currency: base=" + baseCurrency.getId() + ", bundles=" + bundles.size());
    }

    public CurrencyUnit getBaseCurrency() {
        return baseCurrency;
    }

    public List<CurrencyBundle> getBundlesDescending() {
        return Collections.unmodifiableList(bundles);
    }

    public long getPlayerBalance(Player player) {
        long balance = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) continue;
            if (baseCurrency.matches(item)) {
                balance += item.getAmount();
            } else {
                for (CurrencyBundle bundle : bundles) {
                    if (bundle.matches(item)) {
                        balance += (long) item.getAmount() * bundle.getBaseValue();
                        break;
                    }
                }
            }
        }
        return balance;
    }

    public boolean withdraw(Player player, long amount) {
        long balance = getPlayerBalance(player);
        if (balance < amount) {
            return false;
        }

        long remaining = amount;

        // 阶段1: 扣除基础货币
        int baseInInv = countBaseInInventory(player);
        int deductBase = (int) Math.min(remaining, baseInInv);
        if (deductBase > 0) {
            removeBaseCurrency(player, deductBase);
            remaining -= deductBase;
        }

        // 阶段2: 分解 bundle（从大到小）
        if (remaining > 0) {
            for (CurrencyBundle bundle : bundles) {
                if (remaining <= 0) break;
                int bundleCount = countBundleInInventory(player, bundle);
                while (bundleCount > 0 && remaining > 0) {
                    removeBundle(player, bundle, 1);
                    remaining -= bundle.getBaseValue();
                    bundleCount--;
                }
            }
        }

        // 阶段3: 如果分解过头了，把多余的基础货币压缩返还
        if (remaining < 0) {
            deposit(player, Math.abs(remaining));
        }

        return true;
    }

    public long deposit(Player player, long amount) {
        long remaining = amount;
        long mailboxAmount = 0;

        // 给予 bundle（从大到小）
        for (CurrencyBundle bundle : bundles) {
            if (remaining <= 0) break;
            long bundleValue = bundle.getBaseValue();
            int maxCanGive = (int) (remaining / bundleValue);
            if (maxCanGive <= 0) continue;

            int actualGive = calculateCanHold(player, bundle, maxCanGive);
            if (actualGive > 0) {
                giveBundle(player, bundle, actualGive);
                remaining -= (long) actualGive * bundleValue;
            }
        }

        // 给予基础货币
        if (remaining > 0) {
            int baseCanGive = calculateCanHoldBase(player, (int) remaining);
            if (baseCanGive > 0) {
                giveBaseCurrency(player, baseCanGive);
                remaining -= baseCanGive;
            }
        }

        // 剩余存入信箱
        if (remaining > 0) {
            mailboxAmount = remaining;
            MailboxEntry entry = new MailboxEntry(
                    player.getUniqueId(),
                    MailboxType.CURRENCY,
                    null,
                    0,
                    remaining,
                    false,
                    LocalDateTime.now()
            );
            try {
                plugin.getDatabaseManager().getMailboxDAO().insert(entry);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to deposit currency to mailbox: " + e.getMessage());
            }
        }

        return mailboxAmount;
    }

    public boolean canHoldCurrency(Player player, long amount) {
        // 计算如果给予 amount 基础货币价值，需要多少背包空间
        long remaining = amount;
        int requiredSlots = 0;

        for (CurrencyBundle bundle : bundles) {
            long bundleValue = bundle.getBaseValue();
            int count = (int) (remaining / bundleValue);
            if (count <= 0) continue;
            int stackSize = bundle.getMaxStackSize();
            int canStack = countExistingStackable(player, bundle.getMaterial(), stackSize);
            int needed = Math.max(0, count - canStack);
            requiredSlots += (needed + stackSize - 1) / stackSize;
            remaining -= (long) count * bundleValue;
        }

        if (remaining > 0) {
            int stackSize = baseCurrency.getMaxStackSize();
            int canStack = countExistingStackable(player, baseCurrency.getMaterial(), stackSize);
            int needed = Math.max(0, (int) remaining - canStack);
            requiredSlots += (needed + stackSize - 1) / stackSize;
        }

        return InventoryUtil.getEmptySlots(player) >= requiredSlots;
    }

    private int countExistingStackable(Player player, Material material, int stackSize) {
        int available = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item != null && item.getType() == material && item.getAmount() < stackSize) {
                available += stackSize - item.getAmount();
            }
        }
        return available;
    }

    private int countBaseInInventory(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && baseCurrency.matches(item)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private int countBundleInInventory(Player player, CurrencyBundle bundle) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && bundle.matches(item)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void removeBaseCurrency(Player player, int amount) {
        removeItems(player, baseCurrency.getMaterial(), amount);
    }

    private void removeBundle(Player player, CurrencyBundle bundle, int amount) {
        removeItems(player, bundle.getMaterial(), amount);
    }

    private void removeItems(Player player, Material material, int amount) {
        Inventory inv = player.getInventory();
        int remaining = amount;
        for (int i = 0; i < inv.getSize() && remaining > 0; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() == material) {
                int remove = Math.min(remaining, item.getAmount());
                item.setAmount(item.getAmount() - remove);
                if (item.getAmount() <= 0) {
                    inv.setItem(i, null);
                }
                remaining -= remove;
            }
        }
    }

    private void giveBaseCurrency(Player player, int amount) {
        giveItems(player, baseCurrency.createItem(amount));
    }

    private void giveBundle(Player player, CurrencyBundle bundle, int amount) {
        giveItems(player, bundle.createItem(amount));
    }

    private void giveItems(Player player, ItemStack item) {
        player.getInventory().addItem(item);
    }

    private int calculateCanHold(Player player, CurrencyBundle bundle, int maxGive) {
        int stackSize = bundle.getMaxStackSize();
        int existingSpace = 0;
        int emptySlots = 0;

        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType().isAir()) {
                emptySlots++;
            } else if (bundle.matches(item) && item.getAmount() < stackSize) {
                existingSpace += stackSize - item.getAmount();
            }
        }

        long totalSpace = (long) existingSpace + (long) emptySlots * stackSize;
        return (int) Math.min(maxGive, totalSpace);
    }

    private int calculateCanHoldBase(Player player, int maxGive) {
        int stackSize = baseCurrency.getMaxStackSize();
        int existingSpace = 0;
        int emptySlots = 0;

        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType().isAir()) {
                emptySlots++;
            } else if (baseCurrency.matches(item) && item.getAmount() < stackSize) {
                existingSpace += stackSize - item.getAmount();
            }
        }

        long totalSpace = (long) existingSpace + (long) emptySlots * stackSize;
        return (int) Math.min(maxGive, totalSpace);
    }
}
