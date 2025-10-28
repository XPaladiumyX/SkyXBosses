package skyxnetwork.skyXBosses.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import skyxnetwork.skyXBosses.SkyXBosses;
import skyxnetwork.skyXBosses.models.BossData;
import skyxnetwork.skyXBosses.utils.DamageTracker;

public class BossDamageListener implements Listener {

    @EventHandler
    public void onBossDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof LivingEntity boss)) return;

        // Vérifie si la cible est un boss connu
        BossData data = SkyXBosses.getInstance().getBossManager().getBossFromEntity(boss);
        if (data == null) return; // pas un boss géré
        

        Player damager = null;

        if (e.getDamager() instanceof Player p) {
            damager = p;
        } else if (e.getDamager() instanceof Projectile proj) {
            if (proj.getShooter() instanceof Player shooter) {
                damager = shooter;
            }
        } else if (e.getDamager() instanceof Tameable tameable) {
            if (tameable.getOwner() instanceof Player owner) {
                damager = owner;
            }
        }

        if (damager == null) return;

        DamageTracker.recordDamage(boss, damager, e.getFinalDamage());
    }
}