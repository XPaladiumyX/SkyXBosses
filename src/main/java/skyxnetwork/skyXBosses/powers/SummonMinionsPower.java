package skyxnetwork.skyXBosses.powers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.plugin.java.JavaPlugin;
import skyxnetwork.skyXBosses.models.PowerData;
import skyxnetwork.skyXBosses.utils.BossUtils;

public class SummonMinionsPower extends AbstractPower implements Listener {

    private static final int MAX_MINIONS = 12;

    public SummonMinionsPower(JavaPlugin plugin, LivingEntity boss, PowerData data) {
        super(plugin, boss, data);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void execute() {
        Location base = boss.getLocation();
        base.getWorld().spawnParticle(data.getParticle(), base, 50, 1, 1, 1, 0.1);
        base.getWorld().playSound(base, data.getSound(), 1f, 1f);

        // Calcul du type du minion
        EntityType type;
        if (data.getMinionType() == null || data.getMinionType().isEmpty()) {
            type = boss.getType(); // même type que le boss
        } else {
            type = EntityType.valueOf(data.getMinionType().toUpperCase());
        }

        // Vérifier combien de minions sont déjà spawn
        long currentMinions = base.getWorld().getEntitiesByClass(Mob.class).stream()
                .filter(m -> m.getScoreboardTags().contains("BOSS_MINION"))
                .count();

        if (currentMinions >= MAX_MINIONS) return; // ne pas spawn si déjà max

        for (int i = 0; i < data.getMinionCount(); i++) {
            if (currentMinions + i >= MAX_MINIONS) break;

            Location spawnLoc = base.clone().add((Math.random() - 0.5) * 5, 0, (Math.random() - 0.5) * 5);
            var entity = base.getWorld().spawnEntity(spawnLoc, type);

            if (entity instanceof Mob mob) {
                mob.setTarget(null);
                mob.setCustomName("§7" + boss.getName() + " §8Minion");
                mob.setCustomNameVisible(true);
                mob.setHealth(data.getMinionHealth());
                mob.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(data.getMinionDamage());
                mob.addScoreboardTag("BOSS_MINION");

                // Copier les tags du boss
                for (String tag : boss.getScoreboardTags()) {
                    mob.addScoreboardTag(tag);
                }
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent e) {
        if (!(e.getEntity() instanceof LivingEntity attacker)) return;
        if (!(e.getTarget() instanceof LivingEntity target)) return;

        if (!BossUtils.canDamage(attacker, target)) {
            e.setCancelled(true);
        }
    }
}