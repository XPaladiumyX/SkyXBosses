package skyxnetwork.skyXBosses.powers;

import org.bukkit.Location;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import skyxnetwork.skyXBosses.models.PowerData;

import java.util.*;

public class FireballPower extends AbstractPower implements Listener {

    private final UUID bossId;
    private final Random random = new Random();
    private final Set<UUID> stunnedPlayers = new HashSet<>();
    private final Set<UUID> activeFireballs = new HashSet<>();

    public FireballPower(JavaPlugin plugin, LivingEntity boss, PowerData data) {
        super(plugin, boss, data);
        this.bossId = boss.getUniqueId();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void execute() {
        List<Player> nearbyPlayers = boss.getNearbyEntities(data.getRadius(), data.getRadius(), data.getRadius())
                .stream().filter(e -> e instanceof Player).map(e -> (Player) e).toList();

        if (nearbyPlayers.isEmpty()) return;

        int fireballCount = 1 + random.nextInt(2);
        for (int i = 0; i < fireballCount; i++) {

            Player target = nearbyPlayers.get(random.nextInt(nearbyPlayers.size()));

            double offsetX = (random.nextDouble() - 0.5) * 2;
            double offsetZ = (random.nextDouble() - 0.5) * 2;
            double offsetY = boss.getHeight() / 2;
            Location spawnLoc = boss.getLocation().clone().add(offsetX, offsetY, offsetZ);

            Fireball fb = boss.getWorld().spawn(spawnLoc, Fireball.class);
            fb.setShooter(boss);
            fb.setYield(0f);
            fb.setIsIncendiary(false);
            fb.setCustomNameVisible(false);

            boss.getWorld().spawnParticle(data.getParticle(), spawnLoc, 10, 0.2, 0.2, 0.2, 0.05);
            boss.getWorld().playSound(spawnLoc, data.getSound(), 1f, 1f);

            activeFireballs.add(fb.getUniqueId());

            // Suivi de la fireball
            new BukkitRunnable() {
                final Set<UUID> alreadyHit = new HashSet<>();

                @Override
                public void run() {
                    if (!fb.isValid() || target.isDead() || !activeFireballs.contains(fb.getUniqueId())) {
                        activeFireballs.remove(fb.getUniqueId());
                        cancel();
                        return;
                    }

                    // Stop suivi si la fireball a été renvoyée
                    if (!(fb.getShooter() instanceof LivingEntity l && l.getUniqueId().equals(bossId))) {
                        activeFireballs.remove(fb.getUniqueId());
                        cancel();
                        return;
                    }

                    Vector direction = target.getLocation().add(0, target.getEyeHeight() / 2, 0)
                            .toVector().subtract(fb.getLocation().toVector())
                            .normalize().multiply(data.getSpeed() * 0.3);

                    fb.setVelocity(direction);
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!stunnedPlayers.contains(e.getPlayer().getUniqueId())) return;

        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null) return;

        e.getPlayer().teleport(new Location(
                e.getPlayer().getWorld(),
                from.getX(), to.getY(), from.getZ(),
                to.getYaw(), to.getPitch()
        ));
    }

    // STOP fireball follow si le joueur la frappe
    @EventHandler
    public void onFireballHitByPlayer(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Fireball fb)) return;
        if (!(e.getDamager() instanceof Player)) return;
        if (!activeFireballs.contains(fb.getUniqueId())) return;

        activeFireballs.remove(fb.getUniqueId());
        // On ne met plus de velocity, la fireball se comporte comme normale
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Fireball fb)) return;

        if (!activeFireballs.contains(fb.getUniqueId())) return;
        activeFireballs.remove(fb.getUniqueId());

        if (fb.getShooter() == null || !(fb.getShooter() instanceof LivingEntity bossShooter)) return;
        if (!bossShooter.getUniqueId().equals(bossId)) return;

        fb.getNearbyEntities(1.5, 1.5, 1.5).forEach(ent -> {
            if (ent instanceof Player player && !stunnedPlayers.contains(player.getUniqueId())) {
                stunnedPlayers.add(player.getUniqueId());
                player.sendMessage("§cYou have been stunned for 4 seconds!");

                new BukkitRunnable() {
                    int ticks = 0;

                    @Override
                    public void run() {
                        if (ticks++ >= 80 || player.isDead()) {
                            stunnedPlayers.remove(player.getUniqueId());
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }
        });

        fb.getWorld().createExplosion(fb.getLocation(), 0f, false, false, null);
        fb.remove();
    }
}