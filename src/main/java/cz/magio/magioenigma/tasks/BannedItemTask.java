package cz.magio.magioenigma.tasks;

import cz.magio.magioenigma.MagioEnigma;
import cz.magio.magioenigma.managers.ItemSecurityManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BannedItemTask extends BukkitRunnable {

    private final MagioEnigma plugin;
    private final ItemSecurityManager itemManager;

    public BannedItemTask(MagioEnigma plugin, ItemSecurityManager itemManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
    }

    @Override
    public void run() {
        if (!plugin.getConfig().getBoolean("banned-items-settings.enabled", true)) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            itemManager.checkAndSanitizePlayer(player);
        }
    }
}
