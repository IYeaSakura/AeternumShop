package net.sakurain.mc.shop.gui;

import net.sakurain.mc.shop.AeternumShop;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class GUIManager {

    private final AeternumShop plugin;
    private final Map<Player, AbstractGUI> openGuis = new HashMap<>();

    public GUIManager(AeternumShop plugin) {
        this.plugin = plugin;
    }

    public void setOpenGui(Player player, AbstractGUI gui) {
        openGuis.put(player, gui);
    }

    public void removeOpenGui(Player player) {
        openGuis.remove(player);
    }

    public AbstractGUI getOpenGui(Player player) {
        return openGuis.get(player);
    }

    public int getGuiRows() {
        return plugin.getConfigManager().getInt("gui.rows", 6);
    }

    public Material getBorderMaterial() {
        String matName = plugin.getConfigManager().getString("gui.border-item", "GRAY_STAINED_GLASS_PANE");
        Material material = Material.matchMaterial(matName);
        return material != null ? material : Material.GRAY_STAINED_GLASS_PANE;
    }

    public ItemStack getGuiItem(String path) {
        YamlConfiguration gui = plugin.getConfigManager().getGui();
        String materialName = gui.getString(path + ".material", "STONE");
        String name = gui.getString(path + ".name", "");
        Material material = Material.matchMaterial(materialName);
        if (material == null) material = Material.STONE;

        ItemStack item = new ItemStack(material);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(net.sakurain.mc.shop.util.StringUtil.toComponent(name));
            java.util.List<String> lore = gui.getStringList(path + ".lore");
            if (!lore.isEmpty()) {
                meta.lore(lore.stream().map(net.sakurain.mc.shop.util.StringUtil::toComponent).toList());
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public int getGuiSlot(String path) {
        return plugin.getConfigManager().getGui().getInt(path + ".slot", -1);
    }
}
