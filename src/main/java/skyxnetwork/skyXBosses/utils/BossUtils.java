package skyxnetwork.skyXBosses.utils;

import org.bukkit.entity.LivingEntity;
import skyxnetwork.skyXBosses.SkyXBosses;

public class BossUtils {

    public static boolean canDamage(LivingEntity attacker, LivingEntity target) {
        if (attacker == target) return false;

        SkyXBosses plugin = SkyXBosses.getInstance();

        // Si le target est un minion
        if (target.getScoreboardTags().contains("BOSS_MINION")) {
            // minion ne peut pas toucher son boss
            for (String tag : attacker.getScoreboardTags()) {
                if (tag.equalsIgnoreCase(getBossId(target))) return false;
            }
        }

        boolean attackerIsBoss = isBoss(attacker);
        boolean targetIsBoss = isBoss(target);

        // Si les deux sont des boss, ils ne peuvent pas s'attaquer
        if (attackerIsBoss && targetIsBoss) return false;

        // Si les deux ont le même boss tag, éviter d'attaquer (minions ou boss associés)
        for (String tagA : attacker.getScoreboardTags()) {
            for (String tagB : target.getScoreboardTags()) {
                if (tagA.equalsIgnoreCase(tagB)) return false;
            }
        }

        return true;
    }

    // Retourne true si l'entité est un boss connu
    private static boolean isBoss(LivingEntity entity) {
        for (String tag : entity.getScoreboardTags()) {
            if (SkyXBosses.getInstance().getBossManager().getBoss(tag) != null) {
                return true;
            }
        }
        return false;
    }

    // Retourne l'ID du boss si l'entité est un minion
    private static String getBossId(LivingEntity entity) {
        for (String tag : entity.getScoreboardTags()) {
            if (SkyXBosses.getInstance().getBossManager().getBoss(tag) != null) {
                return tag;
            }
        }
        return "";
    }
}
