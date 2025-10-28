package skyxnetwork.skyXBosses.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import skyxnetwork.skyXBosses.SkyXBosses;
import skyxnetwork.skyXBosses.models.BossData;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.Bukkit.getLogger;

public class BossManager {
    private final SkyXBosses plugin;
    private final Map<String, BossData> bosses = new HashMap<>(); // clé = ID simple
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
            // Stocker par un ID simple, sans couleurs ni espaces
            String bossId = file.getName().replace(".yml", "").toUpperCase();
            bosses.put(bossId, boss);
        }

        plugin.getLogger().info("Loaded " + bosses.size() + " bosses.");
    }

    private void scheduleBossSpawns() {
        for (BossData boss : bosses.values()) {
            if (!boss.isEnabled()) continue;
            Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                LivingEntity spawned = boss.spawn();
                if (spawned != null) {
                    // Ajouter un tag unique pour identifier le boss
                    spawned.addScoreboardTag(boss.getName().replaceAll("§", "").toUpperCase());
                    powerExecutor.startForBoss(spawned, boss);
                }
            }, 20L, boss.getSpawnCooldown() * 20L);
        }
    }

    public BossData getBoss(String id) {
        return bosses.get(id.toUpperCase());
    }

    // Méthode pour retrouver un boss via l'entité (listener)
    public BossData getBossFromEntity(LivingEntity entity) {
        for (String tag : entity.getScoreboardTags()) {
            BossData boss = getBoss(tag);
            if (boss != null) return boss;
        }
        return null;
    }

    public PowerExecutor getPowerExecutor() {
        return powerExecutor;
    }

    public void reloadBosses() {
        bosses.clear();
        loadBosses();
        getLogger().info("✅ Bosses reloaded successfully.");
    }

    public List<String> getAllBossNames() {
        return new ArrayList<>(bosses.keySet());
    }
}