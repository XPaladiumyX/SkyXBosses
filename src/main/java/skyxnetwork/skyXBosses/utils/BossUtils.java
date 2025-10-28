package skyxnetwork.skyXBosses.utils;

import org.bukkit.entity.LivingEntity;
import skyxnetwork.skyXBosses.SkyXBosses;

public class BossUtils {

    public static boolean canDamage(LivingEntity attacker, LivingEntity target) {
        if (attacker == target) return false;

        // Si le target est un minion du même boss
        if (target.getScoreboardTags().contains("BOSS_MINION")) {
            // minion ne peut pas toucher son boss
            for (String tag : attacker.getScoreboardTags()) {
                if (tag.equalsIgnoreCase(getBossId(target))) return false;
            }
        }

        // Si les deux sont des boss
        if (!attacker.getScoreboardTags().isEmpty() && !target.getScoreboardTags().isEmpty()) {
            for (String tagA : attacker.getScoreboardTags()) {
                for (String tagB : target.getScoreboardTags()) {
                    if (tagA.equalsIgnoreCase(tagB)) return false; // même boss, ne s’attaque pas
                }
            }
        }

        return true;
    }

    private static String getBossId(LivingEntity entity) {
        for (String tag : entity.getScoreboardTags()) {
            if (SkyXBosses.getInstance().getBossManager().getBoss(tag) != null) {
                return tag;
            }
        }
        return "";
    }
}