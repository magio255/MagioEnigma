package cz.magio.magioenigma.listeners;

import cz.magio.magioenigma.MagioEnigma;
import cz.magio.magioenigma.managers.ItemSecurityManager;
import cz.magio.magioenigma.managers.LargeStackSecurityManager;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class BannedItemListener implements Listener {

    private final MagioEnigma plugin;
    private final ItemSecurityManager itemManager;
    private final LargeStackSecurityManager largeStackManager;

    public BannedItemListener(MagioEnigma plugin, ItemSecurityManager itemManager, LargeStackSecurityManager largeStackManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
        this.largeStackManager = largeStackManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!itemManager.checkAndSanitizePlayer(event.getPlayer())) {
            if (largeStackManager != null) {
                largeStackManager.checkAndEnforcePlayer(event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Item itemEntity = event.getItem();
        ItemStack stack = itemEntity.getItemStack();

        if (itemManager.isBannedItem(stack) && !itemManager.shouldBypass(player)) {
            event.setCancelled(true);
            itemEntity.remove();
            itemManager.checkAndSanitizePlayer(player);
            return;
        }

        if (largeStackManager != null && largeStackManager.isMonitoredItem(stack)) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                largeStackManager.checkAndEnforcePlayer(player);
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (itemManager.shouldBypass(player)) {
            return;
        }

        boolean currentBanned = itemManager.isBannedItem(event.getCurrentItem());
        boolean cursorBanned = itemManager.isBannedItem(event.getCursor());

        if (currentBanned || cursorBanned) {
            event.setCancelled(true);
            itemManager.checkAndSanitizePlayer(player);
            return;
        }

        if (largeStackManager != null) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                largeStackManager.checkAndEnforcePlayer(player);
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (itemManager.shouldBypass(player)) {
            return;
        }

        if (itemManager.isBannedItem(event.getOldCursor())) {
            event.setCancelled(true);
            itemManager.checkAndSanitizePlayer(player);
            return;
        }

        if (largeStackManager != null) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                largeStackManager.checkAndEnforcePlayer(player);
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!itemManager.checkAndSanitizePlayer(player)) {
            if (largeStackManager != null) {
                largeStackManager.checkAndEnforcePlayer(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (itemManager.shouldBypass(player)) {
            return;
        }

        Item droppedItem = event.getItemDrop();
        if (itemManager.isBannedItem(droppedItem.getItemStack())) {
            event.setCancelled(true);
            droppedItem.remove();
            itemManager.checkAndSanitizePlayer(player);
        }
    }
}
