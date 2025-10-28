package skyxnetwork.skyXBosses.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import skyxnetwork.skyXBosses.SkyXBosses;
import skyxnetwork.skyXBosses.models.BossData;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.bukkit.Bukkit.getLogger;

public class BossManager {
    private final SkyXBosses plugin;
    private final Map<String, BossData> bosses = new HashMap<>();
    private final PowerExecutor powerExecutor;

    public BossManager(SkyXBosses plugin) {
        this.plugin = plugin;
        this.powerExecutor = new PowerExecutor(plugin, plugin.getPowerManager());
        loadBosses();
        scheduleBossSpawns();
    }

    private void loadBosses() {
        File folder = new File(plugin.getDataFolder(), "Bosses");
        if (!folder.exists()) folder.mkdirs();

        for (File file : folder.listFiles()) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            BossData boss = new BossData(config);
            bosses.put(file.getName().replace(".yml", "").toUpperCase(), boss);
        }

        plugin.getLogger().info("Loaded " + bosses.size() + " bosses.");
    }

    private void scheduleBossSpawns() {
        for (BossData boss : bosses.values()) {
            if (!boss.isEnabled()) continue;
            Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                LivingEntity spawned = boss.spawn();
                if (spawned != null)
                    powerExecutor.startForBoss(spawned, boss);
            }, 20L, boss.getSpawnCooldown() * 20L);
        }
    }

    public BossData getBoss(String name) {
        return bosses.get(name.toUpperCase());
    }

    public PowerExecutor getPowerExecutor() {
        return powerExecutor;
    }

    public void reloadBosses() {
        bosses.clear();
        loadBosses();
        getLogger().info("✅ Bosses reloaded successfully.");
    }
}
