package skyxnetwork.skyXBosses.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import skyxnetwork.skyXBosses.SkyXBosses;
import skyxnetwork.skyXBosses.models.BossData;

public class BossJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(SkyXBosses.getInstance(), () -> {
            var player = event.getPlayer();

            for (LivingEntity entity : player.getWorld().getLivingEntities()) {
                BossData boss = SkyXBosses.getInstance().getBossManager().getBossFromEntity(entity);
                if (boss != null && boss.isEnabled()) {
                    SkyXBosses.getInstance().getLogger().info("🔄 Restoring bossbar for " + boss.getId() + " to " + player.getName());
                    boss.attachBossBarToExisting(entity); // méthode qu’on va créer juste en dessous
                }
            }
        }, 40L); // attendre 2 secondes après la connexion pour éviter les lag spikes de login
    }
}

