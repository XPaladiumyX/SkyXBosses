package skyxnetwork.skyXBosses.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
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
    private final Map<String, BossData> bosses = new HashMap<>();
    private final PowerExecutor powerExecutor;
    private final List<Integer> scheduledTaskIds = new ArrayList<>();
    private final Map<String, Integer> remainingTimes = new HashMap<>();

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
            String bossId = file.getName().replace(".yml", "").toUpperCase();
            BossData boss = new BossData(config, bossId);
            bosses.put(bossId, boss);
        }

        plugin.getLogger().info("Loaded " + bosses.size() + " bosses.");
    }

    public void scheduleBossSpawns() {
        // Annuler les anciennes tâches si elles existent
        for (int id : scheduledTaskIds) {
            Bukkit.getScheduler().cancelTask(id);
        }
        scheduledTaskIds.clear();

        for (BossData boss : bosses.values()) {
            if (!boss.isEnabled()) continue;

            int cooldownSeconds = boss.getSpawnCooldown();

            int taskId = new BukkitRunnable() {
                private int timer = cooldownSeconds;

                @Override
                public void run() {
                    // ✅ Si aucun joueur n'est connecté, on ne fait rien (on ne réduit pas le timer)
                    if (Bukkit.getOnlinePlayers().isEmpty()) {
                        return;
                    }

                    // Décrémenter le timer
                    timer--;
                    remainingTimes.put(boss.getId(), timer);

                    if (timer <= 0) {
                        LivingEntity spawned = boss.spawn();
                        if (spawned != null) {
                            powerExecutor.startForBoss(spawned, boss);
                            plugin.getLogger().info("✅ Spawned boss " + boss.getId() + " after cooldown.");
                        }

                        // Reset le timer
                        timer = cooldownSeconds;
                        remainingTimes.put(boss.getId(), timer);
                    }
                }
            }.runTaskTimer(plugin, 20L, 20L).getTaskId(); // Vérifie chaque seconde

            scheduledTaskIds.add(taskId);
        }
    }

    public BossData getBoss(String id) {
        return bosses.get(id.toUpperCase());
    }

    public BossData getBossFromEntity(LivingEntity entity) {
        for (String tag : entity.getScoreboardTags()) {
            BossData boss = getBoss(tag.toUpperCase());
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
        scheduleBossSpawns(); // ← Important : relancer les spawns après reload
        getLogger().info("✅ Bosses reloaded successfully.");
    }

    public List<String> getAllBossNames() {
        return new ArrayList<>(bosses.keySet());
    }

    public int getRemainingTime(String bossId) {
        return remainingTimes.getOrDefault(bossId.toUpperCase(), 0);
    }
}