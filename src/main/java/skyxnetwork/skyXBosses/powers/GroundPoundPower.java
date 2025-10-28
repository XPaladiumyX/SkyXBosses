package skyxnetwork.skyXBosses.powers;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import skyxnetwork.skyXBosses.models.PowerData;

public class GroundPoundPower extends AbstractPower {

    public GroundPoundPower(JavaPlugin plugin, LivingEntity boss, PowerData data) {
        super(plugin, boss, data);
    }

    @Override
    public void execute() {
        Location loc = boss.getLocation();
        loc.getWorld().spawnParticle(data.getParticle(), loc, 100, 1, 0.5, 1, 0.1);
        loc.getWorld().playSound(loc, data.getSound(), 1f, 1f);

        boss.getNearbyEntities(data.getRadius(), data.getRadius(), data.getRadius()).forEach(e -> {
            if (e instanceof LivingEntity le && le != boss) {
                if (le.getScoreboardTags().contains("BOSS_MINION")) return; // ✅ ignore les minions
                le.damage(data.getDamage(), boss);
                Vector knock = le.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(data.getKnockback());
                knock.setY(0.5);
                le.setVelocity(knock);
            }
        });
    }
}

