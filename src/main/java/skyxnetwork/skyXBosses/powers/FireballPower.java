package skyxnetwork.skyXBosses.powers;

import org.bukkit.Location;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import skyxnetwork.skyXBosses.models.PowerData;

import java.util.UUID;

public class FireballPower extends AbstractPower implements Listener {

    private final UUID bossId;

    public FireballPower(JavaPlugin plugin, LivingEntity boss, PowerData data) {
        super(plugin, boss, data);
        this.bossId = boss.getUniqueId();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void execute() {
        boss.getNearbyEntities(data.getRadius(), data.getRadius(), data.getRadius()).stream()
                .filter(e -> e instanceof Player)
                .map(e -> (Player) e)
                .findAny()
                .ifPresent(player -> {

                    // Spawn légèrement devant le boss pour éviter collision avec le boss
                    Vector direction = player.getLocation().toVector().subtract(boss.getLocation().toVector()).normalize();
                    Location spawnLoc = boss.getEyeLocation().clone().add(direction.multiply(1.5)); // 1.5 blocks devant

                    Fireball fb = boss.getWorld().spawn(spawnLoc, Fireball.class);
                    fb.setShooter(boss);
                    fb.setCustomName("CUSTOM_FIREBALL_" + bossId);
                    fb.setDirection(direction.multiply(data.getSpeed()));
                    fb.setYield(0f);
                    fb.setIsIncendiary(false);

                    // Particles + son
                    boss.getWorld().spawnParticle(data.getParticle(), spawnLoc, 10, 0.2, 0.2, 0.2, 0.05);
                    boss.getWorld().playSound(spawnLoc, data.getSound(), 1f, 1f);
                });
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Fireball fb)) return;
        if (fb.getCustomName() == null || !fb.getCustomName().startsWith("CUSTOM_FIREBALL_")) return;

        // Fireball custom uniquement
        fb.getNearbyEntities(1.5, 1.5, 1.5).forEach(ent -> {
            if (ent instanceof Player player) {
                // Stun de 2 secondes (40 ticks)
                player.setFreezeTicks(40);
                player.sendMessage("§cYou have been stunned by a fireball for 2 seconds!");
            }
        });
        fb.remove();
    }
}