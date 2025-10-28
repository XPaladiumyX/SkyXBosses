package skyxnetwork.skyXBosses.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import skyxnetwork.skyXBosses.SkyXBosses;
import skyxnetwork.skyXBosses.models.BossData;
import skyxnetwork.skyXBosses.utils.DamageTracker;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BossDeathListener implements Listener {

    @EventHandler
    public void onBossDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof LivingEntity boss)) return;

        BossData data = SkyXBosses.getInstance().getBossManager().getBossFromEntity(boss);
        if (data == null) return;

        Player killer = boss.getKiller(); // ✅ Joueur tueur (peut être null)

        // 🩸 Récupère le top 3 des damageurs
        List<Map.Entry<UUID, Double>> top = DamageTracker.getTopDamagers(boss);

        String damager1name = "None", damager2name = "None", damager3name = "None";
        double damager1damage = 0, damager2damage = 0, damager3damage = 0;

        if (top.size() > 0) {
            var e1 = top.get(0);
            damager1name = Bukkit.getOfflinePlayer(e1.getKey()).getName();
            damager1damage = e1.getValue();
        }
        if (top.size() > 1) {
            var e2 = top.get(1);
            damager2name = Bukkit.getOfflinePlayer(e2.getKey()).getName();
            damager2damage = e2.getValue();
        }
        if (top.size() > 2) {
            var e3 = top.get(2);
            damager3name = Bukkit.getOfflinePlayer(e3.getKey()).getName();
            damager3damage = e3.getValue();
        }

        // 💬 Messages de mort avec placeholders dynamiques
        for (String msg : data.getDeathMessages()) {
            String parsed = msg
                    .replace("$killer", (killer != null ? killer.getName() : "Unknown"))
                    .replace("$damager1name", damager1name)
                    .replace("$damager1damage", String.format("%.1f", damager1damage))
                    .replace("$damager2name", damager2name)
                    .replace("$damager2damage", String.format("%.1f", damager2damage))
                    .replace("$damager3name", damager3name)
                    .replace("$damager3damage", String.format("%.1f", damager3damage));

            Bukkit.getOnlinePlayers().forEach(p ->
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', parsed))
            );
        }

        // ⚙️ Commandes exécutées à la mort
        for (String cmd : data.getOnDeathCommands()) {
            String parsedCmd = cmd
                    .replace("%boss%", boss.getName())
                    .replace("%player%", killer != null ? killer.getName() : "console")
                    .replace("%damager1%", damager1name)
                    .replace("%damager2%", damager2name)
                    .replace("%damager3%", damager3name);

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCmd);
        }

        // 🧹 Nettoyer les données du boss après sa mort
        DamageTracker.clear(boss);
    }
}