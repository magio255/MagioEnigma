package cz.magio.magioenigma;

import cz.magio.magioenigma.managers.ItemSecurityManager;
import cz.magio.magioenigma.managers.LargeStackSecurityManager;
import cz.magio.magioenigma.managers.VillagerSecurityManager;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MagioEnigmaTest {

    private MagioEnigma plugin;
    private FileConfiguration config;
    private ItemSecurityManager itemManager;
    private LargeStackSecurityManager largeStackManager;
    private VillagerSecurityManager villagerManager;

    @BeforeEach
    void setUp() {
        plugin = mock(MagioEnigma.class);
        Server server = mock(Server.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        ConsoleCommandSender consoleSender = mock(ConsoleCommandSender.class);

        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);
        when(server.getConsoleSender()).thenReturn(consoleSender);

        config = new YamlConfiguration();
        config.set("banned-items", Arrays.asList("BEDROCK", "BARRIER", "COMMAND_BLOCK"));
        config.set("banned-items-settings.enabled", true);
        config.set("banned-items-settings.notify-player", true);
        config.set("banned-items-settings.notification-message", "Zakazany predmet (%item%) byl odstranen.");

        config.set("villager-security-settings.enabled", true);
        config.set("villager-security-settings.log-removal", false);

        config.set("large-stack-settings.enabled", true);
        config.set("large-stack-settings.slot-threshold", 3);
        config.set("large-stack-settings.monitored-items", Arrays.asList("NETHERITE_INGOT", "NETHERITE_BLOCK", "DIAMOND_BLOCK"));
        config.set("large-stack-settings.ban-command", "ban %player% High value stack threshold");
        config.set("large-stack-settings.broadcast", false);

        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());

        itemManager = new ItemSecurityManager(plugin);
        largeStackManager = new LargeStackSecurityManager(plugin, itemManager);
        villagerManager = new VillagerSecurityManager(plugin, itemManager);
    }

    @Test
    void testIsBannedItem() {
        assertTrue(itemManager.isBannedMaterial(Material.BEDROCK));
        assertTrue(itemManager.isBannedMaterial(Material.BARRIER));
        assertTrue(itemManager.isBannedMaterial(Material.COMMAND_BLOCK));
        assertFalse(itemManager.isBannedMaterial(Material.DIRT));
        assertFalse(itemManager.isBannedMaterial(Material.EMERALD));

        assertTrue(itemManager.isBannedItem(new ItemStack(Material.BEDROCK)));
        assertFalse(itemManager.isBannedItem(new ItemStack(Material.DIRT)));
        assertFalse(itemManager.isBannedItem(null));
    }

    @Test
    void testPlayerSanitization() {
        Player player = mock(Player.class);
        PlayerInventory inv = mock(PlayerInventory.class);

        when(player.isOp()).thenReturn(false);
        when(player.hasPermission("magioenigma.bypass")).thenReturn(false);
        when(player.getInventory()).thenReturn(inv);

        ItemStack bedrockItem = new ItemStack(Material.BEDROCK);
        ItemStack dirtItem = new ItemStack(Material.DIRT);
        ItemStack[] contents = new ItemStack[]{ dirtItem, bedrockItem, null };

        when(inv.getContents()).thenReturn(contents);
        when(player.getItemOnCursor()).thenReturn(null);

        boolean sanitized = itemManager.checkAndSanitizePlayer(player);

        assertTrue(sanitized);
        verify(inv).setItem(1, null);
        verify(player).sendMessage(contains("BEDROCK"));
    }

    @Test
    void testContainerSanitization() {
        Player player = mock(Player.class);
        Inventory containerInv = mock(Inventory.class);

        when(player.isOp()).thenReturn(false);
        when(player.hasPermission("magioenigma.bypass")).thenReturn(false);

        ItemStack barrierItem = new ItemStack(Material.BARRIER);
        ItemStack dirtItem = new ItemStack(Material.DIRT);
        ItemStack[] contents = new ItemStack[]{ dirtItem, barrierItem };

        when(containerInv.getContents()).thenReturn(contents);

        boolean sanitized = itemManager.checkAndSanitizeContainerInventory(containerInv, player);

        assertTrue(sanitized);
        verify(containerInv).setItem(1, null);
        verify(player).sendMessage(contains("BARRIER"));
    }

    @Test
    void testOpPlayerBypass() {
        Player opPlayer = mock(Player.class);
        when(opPlayer.isOp()).thenReturn(true);

        boolean sanitized = itemManager.checkAndSanitizePlayer(opPlayer);
        assertFalse(sanitized);
    }

    @Test
    void testIllegalVillagerDetection() {
        AbstractVillager villager = mock(AbstractVillager.class);
        when(villager.isValid()).thenReturn(true);

        // MerchantRecipe trading Emerald for Barrier
        MerchantRecipe illegalRecipe = mock(MerchantRecipe.class);
        when(illegalRecipe.getResult()).thenReturn(new ItemStack(Material.BARRIER));
        when(illegalRecipe.getIngredients()).thenReturn(Collections.singletonList(new ItemStack(Material.EMERALD)));

        when(villager.getRecipes()).thenReturn(Collections.singletonList(illegalRecipe));

        assertTrue(villagerManager.isIllegalVillager(villager));
    }

    @Test
    void testLegalVillagerDetection() {
        AbstractVillager villager = mock(AbstractVillager.class);
        when(villager.isValid()).thenReturn(true);

        // MerchantRecipe trading Emerald for Bread
        MerchantRecipe legalRecipe = mock(MerchantRecipe.class);
        when(legalRecipe.getResult()).thenReturn(new ItemStack(Material.BREAD));
        when(legalRecipe.getIngredients()).thenReturn(Collections.singletonList(new ItemStack(Material.EMERALD)));

        when(villager.getRecipes()).thenReturn(Collections.singletonList(legalRecipe));

        assertFalse(villagerManager.isIllegalVillager(villager));
    }

    @Test
    void testLargeStackDetectionTrigger() {
        Player player = mock(Player.class);
        PlayerInventory inv = mock(PlayerInventory.class);

        when(player.getName()).thenReturn("BadPlayer");
        when(player.isOp()).thenReturn(false);
        when(player.hasPermission("magioenigma.bypass")).thenReturn(false);
        when(player.getInventory()).thenReturn(inv);

        ItemStack netheriteBlock = new ItemStack(Material.NETHERITE_BLOCK);
        ItemStack diamondBlock = new ItemStack(Material.DIAMOND_BLOCK);
        ItemStack dirt = new ItemStack(Material.DIRT);

        // 3 slots of monitored items against threshold of 3
        ItemStack[] contents = new ItemStack[]{ netheriteBlock, netheriteBlock, diamondBlock, dirt };
        when(inv.getContents()).thenReturn(contents);
        when(player.getItemOnCursor()).thenReturn(null);

        boolean enforced = largeStackManager.checkAndEnforcePlayer(player);
        assertTrue(enforced);
    }
}
