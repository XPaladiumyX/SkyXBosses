package skyxnetwork.skyXBosses.powers;

import org.bukkit.Location;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import skyxnetwork.skyXBosses.models.PowerData;

public class FireballPower extends AbstractPower {

    public FireballPower(JavaPlugin plugin, LivingEntity boss, PowerData data) {
        super(plugin, boss, data);
    }

    @Override
    public void execute() {
        Player target = getNearestPlayer();
        if (target == null) return;

        Location eye = boss.getEyeLocation();

        Vector dir = target.getLocation().add(0, target.getEyeHeight() / 2, 0)
                .toVector().subtract(eye.toVector()).normalize();

        // ✅ spawn la fireball à 2 blocs devant le boss
        Location spawnLoc = eye.add(dir.clone().multiply(2));

        Fireball fireball = boss.getWorld().spawn(spawnLoc, Fireball.class);
        fireball.setShooter(boss);
        fireball.setDirection(dir);
        fireball.setYield(1.0F);
        fireball.setIsIncendiary(false);
        fireball.setVelocity(dir.multiply(data.getSpeed() * 0.5)); // ✅ plus lente

        spawnLoc.getWorld().spawnParticle(data.getParticle(), spawnLoc, 30, 0.2, 0.2, 0.2, 0.05);
        spawnLoc.getWorld().playSound(spawnLoc, data.getSound(), 1f, 1f);
    }

    private Player getNearestPlayer() {
        double range = data.getRadius();
        Player nearest = null;
        double closest = Double.MAX_VALUE;
        for (Player p : boss.getWorld().getPlayers()) {
            double dist = p.getLocation().distance(boss.getLocation());
            if (dist < closest && dist <= range) {
                closest = dist;
                nearest = p;
            }
        }
        return nearest;
    }
}