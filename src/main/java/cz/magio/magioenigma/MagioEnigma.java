package cz.magio.magioenigma;

import cz.magio.magioenigma.commands.MagioEnigmaCommand;
import cz.magio.magioenigma.listeners.BannedItemListener;
import cz.magio.magioenigma.listeners.VillagerSecurityListener;
import cz.magio.magioenigma.managers.ItemSecurityManager;
import cz.magio.magioenigma.managers.VillagerSecurityManager;
import cz.magio.magioenigma.tasks.BannedItemTask;
import cz.magio.magioenigma.tasks.VillagerSecurityTask;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class MagioEnigma extends JavaPlugin {

    private ItemSecurityManager itemSecurityManager;
    private VillagerSecurityManager villagerSecurityManager;

    private BukkitTask itemTask;
    private BukkitTask villagerTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.itemSecurityManager = new ItemSecurityManager(this);
        this.villagerSecurityManager = new VillagerSecurityManager(this, itemSecurityManager);

        // Register listeners
        getServer().getPluginManager().registerEvents(new BannedItemListener(this, itemSecurityManager), this);
        getServer().getPluginManager().registerEvents(new VillagerSecurityListener(this, villagerSecurityManager), this);

        // Register commands
        MagioEnigmaCommand commandExecutor = new MagioEnigmaCommand(this);
        if (getCommand("magioenigma") != null) {
            getCommand("magioenigma").setExecutor(commandExecutor);
            getCommand("magioenigma").setTabCompleter(commandExecutor);
        }

        // Start tasks
        startTasks();

        getLogger().info("MagioEnigma byl uspesne aktivovan!");
    }

    @Override
    public void onDisable() {
        stopTasks();
        getLogger().info("MagioEnigma byl deaktivovan.");
    }

    public void reloadPluginConfig() {
        reloadConfig();
        if (itemSecurityManager != null) {
            itemSecurityManager.reloadConfig();
        }
        stopTasks();
        startTasks();
    }

    private void startTasks() {
        long itemInterval = getConfig().getLong("banned-items-settings.check-interval-ticks", 20L);
        if (itemInterval > 0) {
            itemTask = new BannedItemTask(this, itemSecurityManager)
                    .runTaskTimer(this, itemInterval, itemInterval);
        }

        long villagerInterval = getConfig().getLong("villager-security-settings.check-interval-ticks", 100L);
        if (villagerInterval > 0) {
            villagerTask = new VillagerSecurityTask(this, villagerSecurityManager)
                    .runTaskTimer(this, villagerInterval, villagerInterval);
        }
    }

    private void stopTasks() {
        if (itemTask != null) {
            itemTask.cancel();
            itemTask = null;
        }
        if (villagerTask != null) {
            villagerTask.cancel();
            villagerTask = null;
        }
    }

    public ItemSecurityManager getItemSecurityManager() {
        return itemSecurityManager;
    }

    public VillagerSecurityManager getVillagerSecurityManager() {
        return villagerSecurityManager;
    }
}
