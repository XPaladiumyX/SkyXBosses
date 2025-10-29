package skyxnetwork.skyXBosses.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import skyxnetwork.skyXBosses.SkyXBosses;
import skyxnetwork.skyXBosses.models.BossData;

public class SkyXBossesExpansion extends PlaceholderExpansion {

    private final SkyXBosses plugin;

    public SkyXBossesExpansion(SkyXBosses plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "skyxbosses";
    }

    @Override
    public @NotNull String getAuthor() {
        return "SkyXNetwork";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // ne se désactive pas au reload
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        // Exemple de placeholder attendu : %skyxbosses_tempest_warden_cooldown%
        if (params.toLowerCase().endsWith("_cooldown")) {
            String bossName = params.substring(0, params.length() - "_cooldown".length()).toUpperCase();
            BossData boss = plugin.getBossManager().getBoss(bossName);

            if (boss == null) return "§cUnknown boss!";

            int remaining = plugin.getBossManager().getRemainingTime(bossName);
            return "§cThe §b§l" + boss.getId().replace("_", " ") + " §r§cspawns in " + remaining + " seconds!";
        }
        return null;
    }
}