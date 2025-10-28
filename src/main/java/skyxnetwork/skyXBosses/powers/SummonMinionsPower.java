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

    public SummonMinionsPower(JavaPlugin plugin, LivingEntity boss, PowerData data) {
        super(plugin, boss, data);

        // On enregistre le listener pour bloquer les cibles interdites
        Bukkit.getPluginManager().registerEvents(this, plugin);
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
                mob.addScoreboardTag("BOSS_MINION");

                // Ajouter le tag du boss pour le repérer facilement
                for (String tag : boss.getScoreboardTags()) {
                    mob.addScoreboardTag(tag);
                }
            }
        }
    }

    // Listener pour empêcher les minions ou bosses de cibler leurs propres alliés
    @EventHandler
    public void onEntityTarget(EntityTargetEvent e) {
        if (!(e.getEntity() instanceof LivingEntity attacker)) return;
        if (!(e.getTarget() instanceof LivingEntity target)) return;

        if (!BossUtils.canDamage(attacker, target)) {
            e.setCancelled(true);
        }
    }
}
