package skyxnetwork.skyXBosses;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import skyxnetwork.skyXBosses.listeners.BossDamageListener;
import skyxnetwork.skyXBosses.listeners.BossDeathListener;
import skyxnetwork.skyXBosses.listeners.BossJoinListener;
import skyxnetwork.skyXBosses.listeners.FireballListener;
import skyxnetwork.skyXBosses.managers.BossManager;
import skyxnetwork.skyXBosses.managers.PowerManager;
import skyxnetwork.skyXBosses.models.BossData;

import java.util.List;

public class SkyXBosses extends JavaPlugin {

    private static SkyXBosses instance;
    private PowerManager powerManager;
    private BossManager bossManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        powerManager = new PowerManager(this);
        bossManager = new BossManager(this);

        getLogger().info("✅ SkyXBosses enabled successfully!");

        // Enregistrement des listeners
        getServer().getPluginManager().registerEvents(new FireballListener(), this);
        getServer().getPluginManager().registerEvents(new BossDamageListener(), this);
        getServer().getPluginManager().registerEvents(new BossDeathListener(), this);
        getServer().getPluginManager().registerEvents(new BossJoinListener(), this);

        // /spawnboss command
        this.getCommand("spawnboss").setExecutor((sender, command, label, args) -> {
            if (args.length == 0) {
                sender.sendMessage("§cUsage: /spawnboss <bossName>");
                return true;
            }

            String bossName = args[0].toUpperCase();
            BossData bossData = bossManager.getBoss(bossName);
            if (bossData == null) {
                sender.sendMessage("§cBoss not found!");
                return true;
            }

            if (!(sender instanceof org.bukkit.entity.Player player)) {
                sender.sendMessage("§cYou must be a player to spawn a boss.");
                return true;
            }

            LivingEntity entity = bossData.spawnAt(player.getLocation());
            if (entity != null) {
                sender.sendMessage("§aBoss spawned!");
                bossManager.getPowerExecutor().startForBoss(entity, bossData);
            }

            return true;
        });

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new skyxnetwork.skyXBosses.placeholders.SkyXBossesExpansion(this).register();
            getLogger().info("✅ Hooked into PlaceholderAPI!");
        } else {
            getLogger().warning("⚠ PlaceholderAPI not found, placeholders won't work!");
        }

        // Tab completion
        this.getCommand("spawnboss").setTabCompleter((sender, command, alias, args) -> {
            if (args.length == 1) {
                return bossManager.getAllBossNames();
            }
            return List.of();
        });

        // /skyxbosses
        getCommand("skyxbosses").setExecutor(new skyxnetwork.skyXBosses.SkyXBossesCommand(this));
    }

    @Override
    public void onDisable() {
        getLogger().info("SkyXBosses disabled.");
    }

    public static SkyXBosses getInstance() {
        return instance;
    }

    public PowerManager getPowerManager() {
        return powerManager;
    }

    public BossManager getBossManager() {
        return bossManager;
    }
}