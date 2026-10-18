package cz.magio.magioenigma.tasks;

import cz.magio.magioenigma.MagioEnigma;
import cz.magio.magioenigma.managers.VillagerSecurityManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

public class VillagerSecurityTask extends BukkitRunnable {

    private final MagioEnigma plugin;
    private final VillagerSecurityManager villagerManager;

    public VillagerSecurityTask(MagioEnigma plugin, VillagerSecurityManager villagerManager) {
        this.plugin = plugin;
        this.villagerManager = villagerManager;
    }

    @Override
    public void run() {
        if (!plugin.getConfig().getBoolean("villager-security-settings.enabled", true)) {
            return;
        }

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof AbstractVillager villager) {
                    villagerManager.checkAndSanitizeVillager(villager);
                }
            }
        }
    }
}
