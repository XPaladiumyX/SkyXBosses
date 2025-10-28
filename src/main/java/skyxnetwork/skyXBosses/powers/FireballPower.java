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

import java.util.*;

public class FireballPower extends AbstractPower implements Listener {

    private final UUID bossId;
    private final Random random = new Random();
    private final Set<UUID> stunnedPlayers = new HashSet<>(); // pour éviter le spam

    public FireballPower(JavaPlugin plugin, LivingEntity boss, PowerData data) {
        super(plugin, boss, data);
        this.bossId = boss.getUniqueId();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void execute() {
        List<Player> nearbyPlayers = boss.getNearbyEntities(data.getRadius(), data.getRadius(), data.getRadius())
                .stream().filter(e -> e instanceof Player).map(e -> (Player) e)
                .toList();

        if (nearbyPlayers.isEmpty()) return;

        int fireballCount = 1 + random.nextInt(2); // 1 ou 2 fireballs
        for (int i = 0; i < fireballCount; i++) {

            Player target = nearbyPlayers.get(random.nextInt(nearbyPlayers.size()));

            // Spawn autour du boss pour pas spawn sur le joueur
            double offsetX = (random.nextDouble() - 0.5) * 2; // ±1 block
            double offsetZ = (random.nextDouble() - 0.5) * 2; // ±1 block
            double offsetY = boss.getHeight() / 2; // à la hauteur du boss
            Location spawnLoc = boss.getLocation().clone().add(offsetX, offsetY, offsetZ);

            Fireball fb = boss.getWorld().spawn(spawnLoc, Fireball.class);
            fb.setShooter(boss);
            fb.setYield(0f);
            fb.setIsIncendiary(false);

            // Cacher le nom
            fb.setCustomNameVisible(false);

            // Particles + son
            boss.getWorld().spawnParticle(data.getParticle(), spawnLoc, 10, 0.2, 0.2, 0.2, 0.05);
            boss.getWorld().playSound(spawnLoc, data.getSound(), 1f, 1f);

            // Fireball suit le joueur
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!fb.isValid() || target.isDead()) {
                        cancel();
                        return;
                    }

                    Vector direction = target.getLocation().clone().add(0, target.getEyeHeight() / 2, 0)
                            .toVector().subtract(fb.getLocation().toVector())
                            .normalize()
                            .multiply(data.getSpeed() * 0.3); // vitesse réduite
                    fb.setVelocity(direction);
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Fireball fb)) return;

        if (fb.getShooter() == null || !(fb.getShooter() instanceof LivingEntity bossShooter)) return;
        if (!bossShooter.getUniqueId().equals(bossId)) return; // uniquement les fireballs du boss

        // Appliquer le stun aux joueurs proches
        fb.getNearbyEntities(1.5, 1.5, 1.5).forEach(ent -> {
            if (ent instanceof Player player) {
                if (stunnedPlayers.contains(player.getUniqueId())) return; // éviter le spam

                stunnedPlayers.add(player.getUniqueId());
                player.sendMessage("§cYou have been stunned for 4 seconds!");

                // Bloquer le joueur avec un runnable
                new BukkitRunnable() {
                    final Location initialLoc = player.getLocation();

                    int ticks = 0;

                    @Override
                    public void run() {
                        if (ticks++ >= 80 || player.isDead()) { // 4 sec
                            stunnedPlayers.remove(player.getUniqueId());
                            cancel();
                            return;
                        }
                        // Reset position pour bloquer le joueur
                        player.teleport(initialLoc);
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }
        });

        fb.getWorld().createExplosion(fb.getLocation(), 0f, false, false, null); // explosion sans dégâts
        fb.remove();
    }
}