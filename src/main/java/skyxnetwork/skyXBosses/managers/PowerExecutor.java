package skyxnetwork.skyXBosses.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
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

        // Récupère la liste des pouvoirs configurés
        List<String> powerNames = bossData.getPowers();
        if (powerNames.isEmpty()) return;

        List<PowerData> powers = new ArrayList<>();
        for (String name : powerNames) {
            PowerData data = powerManager.getPower(name);
            if (data != null) powers.add(data);
        }

        if (powers.isEmpty()) return;

        // Création d'une tâche répétée
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (boss.isDead() || !boss.isValid()) {
                stopForBoss(boss);
                return;
            }

            // Sélection aléatoire d'un pouvoir
            PowerData chosen = powers.get(new Random().nextInt(powers.size()));
            AbstractPower power = createPower(chosen, boss);

            if (power != null) {
                plugin.getLogger().info("⚡ Boss " + boss.getName() + " used power " + chosen.getPowerType());
                power.execute();
            }

        }, 20L, 100L); // tous les 5s → tu peux ajuster ici

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
