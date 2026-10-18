package cz.magio.magioenigma.managers;

import cz.magio.magioenigma.MagioEnigma;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LargeStackSecurityManager {

    private final MagioEnigma plugin;
    private final ItemSecurityManager itemManager;
    private final Set<Material> monitoredMaterials = new HashSet<>();

    public LargeStackSecurityManager(MagioEnigma plugin, ItemSecurityManager itemManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
        reloadConfig();
    }

    public void reloadConfig() {
        monitoredMaterials.clear();
        List<String> list = plugin.getConfig().getStringList("large-stack-settings.monitored-items");
        for (String name : list) {
            Material mat = Material.matchMaterial(name);
            if (mat != null) {
                monitoredMaterials.add(mat);
            } else {
                plugin.getLogger().warning("Neznamy material v large-stack-settings.monitored-items: " + name);
            }
        }
    }

    public Set<Material> getMonitoredMaterials() {
        return monitoredMaterials;
    }

    public boolean isMonitoredItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        return monitoredMaterials.contains(item.getType());
    }

    public boolean checkAndEnforcePlayer(Player player) {
        if (player == null || itemManager.shouldBypass(player)) {
            return false;
        }

        if (!plugin.getConfig().getBoolean("large-stack-settings.enabled", true)) {
            return false;
        }

        int threshold = plugin.getConfig().getInt("large-stack-settings.slot-threshold", 20);
        PlayerInventory inv = player.getInventory();
        int matchingSlots = 0;
        Set<String> detectedTypes = new HashSet<>();

        ItemStack[] contents = inv.getContents();
        for (ItemStack item : contents) {
            if (isMonitoredItem(item)) {
                matchingSlots++;
                detectedTypes.add(item.getType().name());
            }
        }

        ItemStack cursor = player.getItemOnCursor();
        if (isMonitoredItem(cursor)) {
            matchingSlots++;
            detectedTypes.add(cursor.getType().name());
        }

        if (matchingSlots >= threshold) {
            String banCmdTemplate = plugin.getConfig().getString("large-stack-settings.ban-command",
                    "ban %player% [MagioEnigma] Detekovano prilis mnoho cennosti (%count% slotu %items%)");
            String itemsStr = String.join(", ", detectedTypes);
            String cmd = banCmdTemplate
                    .replace("%player%", player.getName())
                    .replace("%count%", String.valueOf(matchingSlots))
                    .replace("%items%", itemsStr);

            if (plugin.getServer() != null) {
                if (plugin.getServer().getScheduler() != null) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
                    });
                } else {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
                }

                if (plugin.getConfig().getBoolean("large-stack-settings.broadcast", true)) {
                    String broadcastMsg = plugin.getConfig().getString("large-stack-settings.broadcast-message",
                            "&c[MagioEnigma] Hrac %player% byl zabanovan za Large Stack Detection (%count% slotu %items%).");
                    broadcastMsg = broadcastMsg
                            .replace("%player%", player.getName())
                            .replace("%count%", String.valueOf(matchingSlots))
                            .replace("%items%", itemsStr);
                    plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcastMsg));
                }
            }

            return true;
        }

        return false;
    }
}
