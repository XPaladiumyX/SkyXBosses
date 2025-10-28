package skyxnetwork.skyXBosses.powers;

import org.bukkit.Location;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import skyxnetwork.skyXBosses.models.PowerData;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class FireballPower extends AbstractPower implements Listener {

    private final UUID bossId;
    private final Random random = new Random();

    public FireballPower(JavaPlugin plugin, LivingEntity boss, PowerData data) {
        super(plugin, boss, data);
        this.bossId = boss.getUniqueId();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void execute() {
        // Récupérer tous les joueurs proches
        List<Player> nearbyPlayers = boss.getNearbyEntities(data.getRadius(), data.getRadius(), data.getRadius())
                .stream().filter(e -> e instanceof Player).map(e -> (Player) e)
                .collect(Collectors.toList());

        if (nearbyPlayers.isEmpty()) return;

        int fireballCount = 3 + random.nextInt(3); // 3 à 5 fireballs
        for (int i = 0; i < fireballCount; i++) {

            // Choisir un joueur aléatoire
            Player target = nearbyPlayers.get(random.nextInt(nearbyPlayers.size()));

            // Spawn aléatoire autour du boss
            double offsetX = (random.nextDouble() - 0.5) * 6; // ±3 blocks
            double offsetZ = (random.nextDouble() - 0.5) * 6; // ±3 blocks
            double offsetY = 1 + random.nextDouble() * 2; // 1 à 3 blocks de hauteur

            Location spawnLoc = boss.getLocation().clone().add(offsetX, offsetY, offsetZ);

            Fireball fb = boss.getWorld().spawn(spawnLoc, Fireball.class);
            fb.setShooter(boss);
            fb.setCustomName("CUSTOM_FIREBALL_" + bossId);
            fb.setYield(0f);
            fb.setIsIncendiary(false);

            // Particles + son
            boss.getWorld().spawnParticle(data.getParticle(), spawnLoc, 10, 0.2, 0.2, 0.2, 0.05);
            boss.getWorld().playSound(spawnLoc, data.getSound(), 1f, 1f);

            // Faire suivre le joueur chaque tick
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (fb.isDead() || !fb.isValid() || target.isDead()) {
                        cancel();
                        return;
                    }

                    // Direction vers le joueur (yeux)
                    Vector direction = target.getLocation().clone().add(0, target.getEyeHeight() / 2, 0)
                            .toVector().subtract(fb.getLocation().toVector()).normalize()
                            .multiply(data.getSpeed() * 0.5); // vitesse réduite
                    fb.setVelocity(direction);
                }
            }.runTaskTimer(plugin, 0L, 1L); // update chaque tick
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Fireball fb)) return;
        if (fb.getCustomName() == null || !fb.getCustomName().startsWith("CUSTOM_FIREBALL_")) return;

        // Fireball custom uniquement
        fb.getNearbyEntities(1.5, 1.5, 1.5).forEach(ent -> {
            if (ent instanceof Player player) {
                // Stun de 4 secondes (80 ticks)
                player.setFreezeTicks(80);
                player.sendMessage("§cYou have been stunned by a fireball for 4 seconds!");
            }
        });
        fb.remove();
    }
}