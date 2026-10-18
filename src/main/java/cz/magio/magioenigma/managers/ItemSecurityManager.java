package cz.magio.magioenigma.managers;

import cz.magio.magioenigma.MagioEnigma;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemSecurityManager {

    private final MagioEnigma plugin;
    private final Set<Material> bannedMaterials = new HashSet<>();

    public ItemSecurityManager(MagioEnigma plugin) {
        this.plugin = plugin;
        reloadConfig();
    }

    public void reloadConfig() {
        bannedMaterials.clear();
        List<String> list = plugin.getConfig().getStringList("banned-items");
        for (String name : list) {
            Material mat = Material.matchMaterial(name);
            if (mat != null) {
                bannedMaterials.add(mat);
            } else {
                plugin.getLogger().warning("Neznamy material v banned-items: " + name);
            }
        }
    }

    public Set<Material> getBannedMaterials() {
        return bannedMaterials;
    }

    public boolean isBannedMaterial(Material material) {
        if (material == null) return false;
        return bannedMaterials.contains(material);
    }

    public boolean isBannedItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        return isBannedMaterial(item.getType());
    }

    public boolean shouldBypass(Player player) {
        if (player == null) return false;
        return player.isOp() || player.hasPermission("magioenigma.bypass");
    }

    public boolean checkAndSanitizePlayer(Player player) {
        if (player == null || shouldBypass(player)) {
            return false;
        }

        if (!plugin.getConfig().getBoolean("banned-items-settings.enabled", true)) {
            return false;
        }

        PlayerInventory inv = player.getInventory();
        boolean removedAny = false;
        Set<String> removedItemNames = new HashSet<>();

        // Check main storage contents
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (isBannedItem(item)) {
                removedItemNames.add(item.getType().name());
                inv.setItem(i, null);
                removedAny = true;
            }
        }

        // Check cursor item
        ItemStack cursor = player.getItemOnCursor();
        if (isBannedItem(cursor)) {
            removedItemNames.add(cursor.getType().name());
            player.setItemOnCursor(null);
            removedAny = true;
        }

        if (removedAny && plugin.getConfig().getBoolean("banned-items-settings.notify-player", true)) {
            String msg = plugin.getConfig().getString("banned-items-settings.notification-message",
                    "&c[MagioEnigma] Zakazany predmet (%item%) byl odstranen z tveho inventare!");
            String itemsStr = String.join(", ", removedItemNames);
            msg = msg.replace("%item%", itemsStr);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }

        return removedAny;
    }

    public boolean checkAndSanitizeContainerInventory(Inventory inventory, Player triggerPlayer) {
        if (inventory == null) return false;
        if (!plugin.getConfig().getBoolean("banned-items-settings.enabled", true)) {
            return false;
        }

        if (triggerPlayer != null && shouldBypass(triggerPlayer)) {
            return false;
        }

        boolean removedAny = false;
        Set<String> removedItemNames = new HashSet<>();

        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (isBannedItem(item)) {
                removedItemNames.add(item.getType().name());
                inventory.setItem(i, null);
                removedAny = true;
            }
        }

        if (removedAny && triggerPlayer != null && plugin.getConfig().getBoolean("banned-items-settings.notify-player", true)) {
            String msg = plugin.getConfig().getString("banned-items-settings.notification-message",
                    "&c[MagioEnigma] Zakazany predmet (%item%) byl odstranen z tveho inventare!");
            String itemsStr = String.join(", ", removedItemNames);
            msg = msg.replace("%item%", itemsStr);
            triggerPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }

        return removedAny;
    }
}
