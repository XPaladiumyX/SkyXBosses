package skyxnetwork.skyXBosses.managers;

import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import skyxnetwork.skyXBosses.SkyXBosses;
import skyxnetwork.skyXBosses.models.BossData;
import skyxnetwork.skyXBosses.models.PowerData;
import skyxnetwork.skyXBosses.powers.*;

import java.util.*;

public class PowerExecutor {

    private final SkyXBosses plugin;
    private final PowerManager powerManager;
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();

    public PowerExecutor(SkyXBosses plugin, PowerManager powerManager) {
        this.plugin = plugin;
        this.powerManager = powerManager;
    }

    public void startForBoss(LivingEntity boss, BossData bossData) {
        if (activeTasks.containsKey(boss.getUniqueId())) return;

        List<String> powerNames = bossData.getPowers();
        if (powerNames.isEmpty()) return;

        List<PowerData> powers = new ArrayList<>();
        for (String name : powerNames) {
            PowerData data = powerManager.getPower(name);
            if (data != null) powers.add(data);
        }

        if (powers.isEmpty()) return;

        // Démarrer la première exécution
        scheduleNextAttack(boss, powers);
    }

    private void scheduleNextAttack(LivingEntity boss, List<PowerData> powers) {
        if (boss.isDead() || !boss.isValid()) {
            stopForBoss(boss);
            return;
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) {
                    stopForBoss(boss);
                    return;
                }

                // Choisir et exécuter un pouvoir aléatoire
                PowerData chosen = powers.get(new Random().nextInt(powers.size()));
                AbstractPower power = createPower(chosen, boss);
                if (power != null) power.execute();

                // Planifier la prochaine attaque selon la vie
                double healthPercent = boss.getHealth() / boss.getMaxHealth();
                long baseDelay = 100L; // 5 sec
                long minDelay = 20L;   // 1 sec
                long delay = (long) (minDelay + (baseDelay - minDelay) * healthPercent);

                scheduleNextAttack(boss, powers); // récursion propre
            }
        }.runTask(plugin); // planifie la tâche immédiate

        activeTasks.put(boss.getUniqueId(), task);
    }

    public void stopForBoss(LivingEntity boss) {
        BukkitTask task = activeTasks.remove(boss.getUniqueId());
        if (task != null) task.cancel();
    }

    private AbstractPower createPower(PowerData data, LivingEntity boss) {
        return switch (data.getPowerType()) {
            case "EXPLOSION" -> new ExplosionPower(plugin, boss, data);
            case "FIREBALL" -> new FireballPower(plugin, boss, data);
            case "GROUND_POUND" -> new GroundPoundPower(plugin, boss, data);
            case "LIGHTNING" -> new LightningPower(plugin, boss, data);
            case "SUMMON_MINIONS" -> new SummonMinionsPower(plugin, boss, data);
            default -> null;
        };
    }
}
