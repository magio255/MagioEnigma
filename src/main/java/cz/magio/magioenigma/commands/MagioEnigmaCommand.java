package cz.magio.magioenigma.commands;

import cz.magio.magioenigma.MagioEnigma;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class MagioEnigmaCommand implements CommandExecutor, TabCompleter {

    private final MagioEnigma plugin;

    public MagioEnigmaCommand(MagioEnigma plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("magioenigma.admin")) {
                sender.sendMessage(ChatColor.RED + "Nemas opravneni k pouziti tohoto prikazu!");
                return true;
            }

            plugin.reloadPluginConfig();
            sender.sendMessage(ChatColor.GREEN + "[MagioEnigma] Konfigurace byla uspesne znovu nactena!");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== MagioEnigma v" + plugin.getDescription().getVersion() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "/magioenigma reload " + ChatColor.WHITE + "- Znovu nacte konfiguraci pluginu");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if ("reload".startsWith(args[0].toLowerCase())) {
                completions.add("reload");
            }
        }
        return completions;
    }
}
