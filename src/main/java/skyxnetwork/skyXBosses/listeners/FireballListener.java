package skyxnetwork.skyXBosses.listeners;

import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;
import skyxnetwork.skyXBosses.SkyXBosses;
import skyxnetwork.skyXBosses.models.BossData;

public class FireballListener implements Listener {

    @EventHandler
    public void onFireballHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Fireball fireball)) return;

        ProjectileSource shooter = fireball.getShooter();
        if (!(shooter instanceof LivingEntity livingShooter)) return;

        BossData bossData = SkyXBosses.getInstance().getBossManager().getBossFromEntity(livingShooter);
        if (bossData == null) return; // pas un boss -> ignorer

        // 🔥 Empêche le feu au sol
        fireball.setIsIncendiary(false);

        // 🔥 Empêche l’explosion de casser ou brûler des blocs
        fireball.setYield(0F);
    }
}

