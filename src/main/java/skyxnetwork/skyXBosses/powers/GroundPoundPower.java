package skyxnetwork.skyXBosses.powers;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
            if (!(e instanceof LivingEntity le) || le == boss) return;
            if (le.getScoreboardTags().contains("BOSS_MINION")) return;
            if (le instanceof Player player && player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

            le.damage(data.getDamage(), boss);

            Vector knock = le.getLocation().toVector().subtract(loc.toVector());
            if (knock.length() > 0) {
                knock = knock.normalize().multiply(data.getKnockback());
                knock.setY(0.5);
                if (Double.isFinite(knock.getX()) && Double.isFinite(knock.getY()) && Double.isFinite(knock.getZ())) {
                    le.setVelocity(knock);
                }
            }
        });
    }
}