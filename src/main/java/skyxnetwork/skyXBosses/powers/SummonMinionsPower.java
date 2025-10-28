package skyxnetwork.skyXBosses.powers;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.plugin.java.JavaPlugin;
import skyxnetwork.skyXBosses.models.PowerData;

public class SummonMinionsPower extends AbstractPower {

    public SummonMinionsPower(JavaPlugin plugin, LivingEntity boss, PowerData data) {
        super(plugin, boss, data);
    }

    @Override
    public void execute() {
        Location base = boss.getLocation();
        base.getWorld().spawnParticle(data.getParticle(), base, 50, 1, 1, 1, 0.1);
        base.getWorld().playSound(base, data.getSound(), 1f, 1f);

        EntityType type = EntityType.valueOf(data.getMinionType().toUpperCase());
        for (int i = 0; i < data.getMinionCount(); i++) {
            Location spawnLoc = base.clone().add((Math.random() - 0.5) * 5, 0, (Math.random() - 0.5) * 5);
            var entity = base.getWorld().spawnEntity(spawnLoc, type);
            if (entity instanceof Mob mob) {
                mob.setTarget(null);
                mob.setCustomName("§7" + boss.getName() + " §8Minion");
                mob.setCustomNameVisible(true);
                mob.setHealth(data.getMinionHealth());
                mob.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(data.getMinionDamage());
            }
        }
    }
}

