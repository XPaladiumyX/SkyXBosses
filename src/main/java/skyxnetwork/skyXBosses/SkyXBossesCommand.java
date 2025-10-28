package skyxnetwork.skyXBosses;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SkyXBossesCommand implements CommandExecutor {

    private final SkyXBosses plugin;

    public SkyXBossesCommand(SkyXBosses plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /skyxbosses reload");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("skyxbosses.admin")) {
                sender.sendMessage("§cYou don't have permission to do that.");
                return true;
            }

            // Reload main config
            plugin.reloadConfig();

            // Reload Powers
            plugin.getPowerManager().loadPowers();

            // Reload Bosses
            plugin.getBossManager().reloadBosses();

            sender.sendMessage("§aSkyXBosses has been reloaded successfully!");
            plugin.getLogger().info(sender.getName() + " reloaded SkyXBosses.");
            return true;
        }

        sender.sendMessage("§cUnknown argument. Usage: /skyxbosses reload");
        return true;
    }
}
