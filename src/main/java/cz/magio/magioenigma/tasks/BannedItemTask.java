package cz.magio.magioenigma.tasks;

import cz.magio.magioenigma.MagioEnigma;
import cz.magio.magioenigma.managers.ItemSecurityManager;
import cz.magio.magioenigma.managers.LargeStackSecurityManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BannedItemTask extends BukkitRunnable {

    private final MagioEnigma plugin;
    private final ItemSecurityManager itemManager;
    private final LargeStackSecurityManager largeStackManager;

    public BannedItemTask(MagioEnigma plugin, ItemSecurityManager itemManager, LargeStackSecurityManager largeStackManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
        this.largeStackManager = largeStackManager;
    }

    @Override
    public void run() {
        if (!plugin.getConfig().getBoolean("banned-items-settings.enabled", true)
                && !plugin.getConfig().getBoolean("large-stack-settings.enabled", true)) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean sanitized = itemManager.checkAndSanitizePlayer(player);
            if (!sanitized && largeStackManager != null) {
                largeStackManager.checkAndEnforcePlayer(player);
            }
        }
    }
}
