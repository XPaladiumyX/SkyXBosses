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
        Vector dir = target.getLocation().add(0, 1, 0).toVector().subtract(eye.toVector()).normalize();

        Fireball fireball = boss.launchProjectile(Fireball.class, dir.multiply(data.getSpeed()));
        fireball.setYield(2);
        fireball.setIsIncendiary(false);

        eye.getWorld().spawnParticle(data.getParticle(), eye, 30, 0.2, 0.2, 0.2, 0.05);
        eye.getWorld().playSound(eye, data.getSound(), 1f, 1f);
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
