package skyxnetwork.skyXBosses.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
        if (!(e.getDamager() instanceof Player player)) return;

        BossData data = SkyXBosses.getInstance().getBossManager().getBossFromEntity(boss);
        if (data == null) return; // pas un boss géré

        DamageTracker.recordDamage(boss, player, e.getFinalDamage());
    }
}