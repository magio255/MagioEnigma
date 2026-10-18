package cz.magio.magioenigma.managers;

import cz.magio.magioenigma.MagioEnigma;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.List;

public class VillagerSecurityManager {

    private final MagioEnigma plugin;
    private final ItemSecurityManager itemManager;

    public VillagerSecurityManager(MagioEnigma plugin, ItemSecurityManager itemManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
    }

    public boolean isIllegalVillager(AbstractVillager villager) {
        if (villager == null || !villager.isValid()) {
            return false;
        }

        List<MerchantRecipe> recipes;
        try {
            recipes = villager.getRecipes();
        } catch (Exception e) {
            return false;
        }

        if (recipes == null || recipes.isEmpty()) {
            return false;
        }

        for (MerchantRecipe recipe : recipes) {
            if (recipe == null) continue;

            // Check trade result (output)
            ItemStack result = recipe.getResult();
            if (itemManager.isBannedItem(result)) {
                return true;
            }

            // Check trade ingredients (buy items)
            List<ItemStack> ingredients = recipe.getIngredients();
            if (ingredients != null) {
                for (ItemStack ingredient : ingredients) {
                    if (itemManager.isBannedItem(ingredient)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean checkAndSanitizeVillager(AbstractVillager villager) {
        if (villager == null || !villager.isValid()) {
            return false;
        }

        if (!plugin.getConfig().getBoolean("villager-security-settings.enabled", true)) {
            return false;
        }

        if (isIllegalVillager(villager)) {
            Location loc = villager.getLocation();
            villager.remove();

            if (plugin.getConfig().getBoolean("villager-security-settings.log-removal", true)) {
                String msg = plugin.getConfig().getString("villager-security-settings.log-message",
                        "&c[MagioEnigma] Nelegalni villager s zakazanymi obchody byl vymazan na souradnicich: %location%");
                String locStr = String.format("%s, %d, %d, %d",
                        loc.getWorld() != null ? loc.getWorld().getName() : "unknown",
                        loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                msg = msg.replace("%location%", locStr);
                plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            }
            return true;
        }

        return false;
    }
}
