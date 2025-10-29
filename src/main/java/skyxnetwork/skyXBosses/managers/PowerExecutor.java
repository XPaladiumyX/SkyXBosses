package skyxnetwork.skyXBosses.managers;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
    private final Map<UUID, Set<Integer>> bossHealthMilestones = new HashMap<>();
    private final Random random = new Random();

    public PowerExecutor(SkyXBosses plugin, PowerManager powerManager) {
        this.plugin = plugin;
        this.powerManager = powerManager;
    }

    public void startForBoss(LivingEntity boss, BossData bossData) {
        if (boss == null || bossData == null || activeTasks.containsKey(boss.getUniqueId())) return;

        List<PowerData> powers = new ArrayList<>();
        for (String name : bossData.getPowers()) {
            PowerData data = powerManager.getPower(name);
            if (data != null) powers.add(data);
        }

        if (powers.isEmpty()) return;

        scheduleNextAttack(boss, powers, 0L); // première attaque immédiate
    }

    private void scheduleNextAttack(LivingEntity boss, List<PowerData> powers, long delay) {
        if (boss == null || powers.isEmpty() || boss.isDead() || !boss.isValid()) {
            stopForBoss(boss);
            return;
        }

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) {
                    stopForBoss(boss);
                    return;
                }

                // Vérifier s'il y a au moins un joueur en survie à proximité
                boolean hasSurvivalPlayer = boss.getNearbyEntities(45, 45, 45).stream()
                        .filter(e -> e instanceof Player)
                        .map(e -> (Player) e)
                        .anyMatch(p -> p.getGameMode() == GameMode.SURVIVAL);

                if (hasSurvivalPlayer) {
                    // Choisir et exécuter un pouvoir aléatoire
                    PowerData chosen = powers.get(random.nextInt(powers.size()));
                    AbstractPower power = createPower(chosen, boss);
                    if (power != null) power.execute();
                }

                // Vérifier et annoncer les paliers de vie
                handleHealthMilestones(boss);

                // Calculer le prochain délai en ticks (1 tick = 50ms)
                double healthPercent = boss.getHealth() / boss.getMaxHealth();
                long baseDelay = 100L; // 5 sec
                long minDelay = 20L;   // 1 sec
                long nextDelay = (long) (minDelay + (baseDelay - minDelay) * healthPercent);

                scheduleNextAttack(boss, powers, nextDelay);
            }
        };

        BukkitTask task = (delay <= 0) ? runnable.runTask(plugin) : runnable.runTaskLater(plugin, delay);
        activeTasks.put(boss.getUniqueId(), task);
    }

    private void handleHealthMilestones(LivingEntity boss) {
        int[] milestones = {75, 50, 25, 10};
        Set<Integer> triggered = bossHealthMilestones.computeIfAbsent(boss.getUniqueId(), k -> new HashSet<>());
        double healthPercent = boss.getHealth() / boss.getMaxHealth();

        for (int milestone : milestones) {
            if (healthPercent * 100 <= milestone && !triggered.contains(milestone)) {
                triggered.add(milestone);
                Bukkit.broadcastMessage("§cThe boss " + boss.getName() + " §r§chas reached " + milestone + "% of its life! It will now attack faster!");
            }
        }
    }

    public void stopForBoss(LivingEntity boss) {
        if (boss == null) return;
        BukkitTask task = activeTasks.remove(boss.getUniqueId());
        if (task != null) task.cancel();
        bossHealthMilestones.remove(boss.getUniqueId());
    }

    private AbstractPower createPower(PowerData data, LivingEntity boss) {
        if (data == null || boss == null) return null;

        return switch (data.getPowerType()) {
            case "EXPLOSION" -> new ExplosionPower(plugin, boss, data);
            case "FIREBALL" -> new FireballPower(plugin, boss, data);
            case "GROUND_POUND" -> new GroundPoundPower(plugin, boss, data);
            case "LIGHTNING" -> new LightningPower(plugin, boss, data);
            case "SUMMON_MINIONS" -> new SummonMinionsPower(plugin, boss, data);
            case "CRUSH_GRAB" -> new CrushGrabPower(plugin, boss, data);
            default -> null;
        };
    }
}