package skyxnetwork.skyXBosses.powers;

import org.bukkit.Location;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import skyxnetwork.skyXBosses.models.PowerData;

public class FireballPower extends AbstractPower {

    public FireballPower(JavaPlugin plugin, LivingEntity boss, PowerData data) {
        super(plugin, boss, data);
    }

    @Override
    public void execute() {
        boss.getNearbyEntities(data.getRadius(), data.getRadius(), data.getRadius()).forEach(e -> {
            if (e instanceof Player player) {

                // spawn 5 blocks au-dessus de la tête
                Location spawnLoc = player.getLocation().clone().add(0, 5 + player.getEyeHeight(), 0);

                Fireball fb = boss.getWorld().spawn(spawnLoc, Fireball.class);
                fb.setShooter(boss);

                // Direction vers le joueur
                fb.setDirection(player.getLocation().toVector().subtract(spawnLoc.toVector()).normalize());
                fb.setYield(0f);
                fb.setIsIncendiary(false);

                // Tâche pour vérifier collision avec le joueur
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (fb.isDead() || !fb.isValid()) {
                            cancel();
                            return;
                        }

                        // Si la fireball touche le joueur
                        if (fb.getLocation().distanceSquared(player.getLocation()) < 1) {

                            // ✅ stun pendant 2 secondes
                            player.setFreezeTicks(40); // 40 ticks = 2 sec
                            player.sendMessage("§cYou have been stunned by a fireball for 2 seconds!");

                            fb.remove();
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L); // vérifie chaque tick
            }
        });
    }
}