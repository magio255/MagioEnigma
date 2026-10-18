package cz.magio.magioenigma;

import cz.magio.magioenigma.managers.ItemSecurityManager;
import cz.magio.magioenigma.managers.VillagerSecurityManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MagioEnigmaTest {

    private MagioEnigma plugin;
    private FileConfiguration config;
    private ItemSecurityManager itemManager;
    private VillagerSecurityManager villagerManager;

    @BeforeEach
    void setUp() {
        plugin = mock(MagioEnigma.class);
        config = new YamlConfiguration();
        config.set("banned-items", Arrays.asList("BEDROCK", "BARRIER", "COMMAND_BLOCK"));
        config.set("banned-items-settings.enabled", true);
        config.set("banned-items-settings.notify-player", true);
        config.set("banned-items-settings.notification-message", "Zakazany predmet (%item%) byl odstranen.");
        config.set("villager-security-settings.enabled", true);
        config.set("villager-security-settings.log-removal", false);

        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());

        itemManager = new ItemSecurityManager(plugin);
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
}
