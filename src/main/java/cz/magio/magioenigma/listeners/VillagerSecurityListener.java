package cz.magio.magioenigma.listeners;

import cz.magio.magioenigma.MagioEnigma;
import cz.magio.magioenigma.managers.VillagerSecurityManager;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class VillagerSecurityListener implements Listener {

    private final MagioEnigma plugin;
    private final VillagerSecurityManager villagerManager;

    public VillagerSecurityListener(MagioEnigma plugin, VillagerSecurityManager villagerManager) {
        this.plugin = plugin;
        this.villagerManager = villagerManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof AbstractVillager villager) {
            if (villagerManager.checkAndSanitizeVillager(villager)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof AbstractVillager villager) {
            if (villagerManager.checkAndSanitizeVillager(villager)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof AbstractVillager villager) {
                villagerManager.checkAndSanitizeVillager(villager);
            }
        }
    }
}
