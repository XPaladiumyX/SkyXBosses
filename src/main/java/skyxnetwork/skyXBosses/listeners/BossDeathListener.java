package skyxnetwork.skyXBosses.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import skyxnetwork.skyXBosses.SkyXBosses;
import skyxnetwork.skyXBosses.models.BossData;

public class BossDeathListener implements Listener {

    @EventHandler
    public void onBossDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof LivingEntity boss)) return;

        BossData data = SkyXBosses.getInstance().getBossManager().getBossFromEntity(boss);
        if (data == null) return;

        // Death messages
        for (String msg : data.getDeathMessages()) {
            Bukkit.getOnlinePlayers().forEach(p ->
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg))
            );
        }

        // On-death commands
        for (String cmd : data.getOnDeathCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%boss%", boss.getName()));
        }
    }
}