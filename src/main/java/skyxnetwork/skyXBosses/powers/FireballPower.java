package skyxnetwork.skyXBosses.powers;

import org.bukkit.Location;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import skyxnetwork.skyXBosses.models.PowerData;

public class FireballPower extends AbstractPower implements Listener {

    public FireballPower(JavaPlugin plugin, LivingEntity boss, PowerData data) {
        super(plugin, boss, data);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void execute() {
        // Choisir un joueur proche
        boss.getNearbyEntities(data.getRadius(), data.getRadius(), data.getRadius()).stream()
                .filter(e -> e instanceof Player)
                .map(e -> (Player) e)
                .findAny()
                .ifPresent(player -> {

                    Location spawnLoc = boss.getLocation().clone().add(0, boss.getHeight() / 2, 0);
                    Fireball fb = boss.getWorld().spawn(spawnLoc, Fireball.class);
                    fb.setShooter(boss);
                    fb.setDirection(player.getLocation().toVector().subtract(spawnLoc.toVector()).normalize().multiply(data.getSpeed()));
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
        if (!(fb.getShooter() instanceof LivingEntity boss)) return;

        // Si c’est une fireball de boss
        if (fb.getWorld() == null) return;
        fb.getNearbyEntities(1.5, 1.5, 1.5).forEach(ent -> {
            if (ent instanceof Player player) {
                player.setFreezeTicks(40);
                player.sendMessage("§cYou have been stunned by a fireball for 2 seconds!");
            }
        });
        fb.remove();
    }
}