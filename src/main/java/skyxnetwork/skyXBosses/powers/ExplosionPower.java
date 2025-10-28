package skyxnetwork.skyXBosses.powers;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import skyxnetwork.skyXBosses.models.PowerData;

public class ExplosionPower extends AbstractPower {

    public ExplosionPower(JavaPlugin plugin, LivingEntity boss, PowerData data) {
        super(plugin, boss, data);
    }

    @Override
    public void execute() {
        Location loc = boss.getLocation();
        loc.getWorld().createExplosion(loc, 0F, false, data.isDestructive(), boss);
        loc.getWorld().spawnParticle(data.getParticle(), loc, 80, 1, 1, 1, 0.1);
        loc.getWorld().playSound(loc, data.getSound(), 1f, 1f);

        boss.getNearbyEntities(data.getRadius(), data.getRadius(), data.getRadius()).forEach(e -> {
            if (e instanceof LivingEntity le && le != boss) {
                le.damage(data.getDamage(), boss);
            }
        });
    }
}

