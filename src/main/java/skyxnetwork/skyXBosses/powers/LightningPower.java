package skyxnetwork.skyXBosses.powers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import skyxnetwork.skyXBosses.models.PowerData;

public class LightningPower extends AbstractPower {

    public LightningPower(JavaPlugin plugin, LivingEntity boss, PowerData data) {
        super(plugin, boss, data);
    }

    @Override
    public void execute() {
        Player target = getNearestPlayer();
        if (target == null) return;

        Location loc = target.getLocation();
        loc.getWorld().strikeLightningEffect(loc);
        target.damage(data.getDamage(), boss);

        loc.getWorld().spawnParticle(data.getParticle(), loc, 50, 0.3, 0.3, 0.3, 0.1);
        loc.getWorld().playSound(loc, data.getSound(), 1f, 1f);
    }

    private Player getNearestPlayer() {
        double range = data.getRadius();
        Player nearest = null;
        double closest = Double.MAX_VALUE;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld() != boss.getWorld()) continue;
            double dist = p.getLocation().distance(boss.getLocation());
            if (dist < closest && dist <= range) {
                closest = dist;
                nearest = p;
            }
        }
        return nearest;
    }
}

