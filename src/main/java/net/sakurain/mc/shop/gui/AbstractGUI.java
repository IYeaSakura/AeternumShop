package net.sakurain.mc.shop.gui;

import net.kyori.adventure.text.Component;
import net.sakurain.mc.shop.AeternumShop;
import net.sakurain.mc.shop.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractGUI implements InventoryHolder {

    protected final AeternumShop plugin;
    protected final Player player;
    protected Inventory inventory;
    protected final Component title;
    protected final int rows;

    public AbstractGUI(Player player, String titleKey, int rows) {
        this.plugin = AeternumShop.getInstance();
        this.player = player;
        this.title = plugin.getMessageManager().getTitle(titleKey);
        this.rows = rows;
        this.inventory = Bukkit.createInventory(this, rows * 9, title);
    }

    public abstract void initialize();

    public abstract void onClick(InventoryClickEvent event);

    public void open() {
        player.openInventory(inventory);
    }

    public void close() {
        player.closeInventory();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }

    protected void fillBorder(Material material) {
        String name = plugin.getConfigManager().getString("gui.border-name", " ");
        ItemStack border = createGuiItem(material, name);
        for (int i = 0; i < rows * 9; i++) {
            if (i < 9 || i >= (rows - 1) * 9 || i % 9 == 0 || i % 9 == 8) {
                if (inventory.getItem(i) == null) {
                    inventory.setItem(i, border);
                }
            }
        }
    }

    protected void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    protected ItemStack createGuiItem(Material material, String name, String... lore) {
        return createGuiItem(material, name, Arrays.asList(lore));
    }

    protected ItemStack createGuiItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(StringUtil.toComponent(name));
            if (lore != null && !lore.isEmpty()) {
                meta.lore(lore.stream().map(StringUtil::toComponent).collect(Collectors.toList()));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
