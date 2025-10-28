package skyxnetwork.skyXBosses.utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

public class DamageTracker {

    private static final Map<UUID, Map<UUID, Double>> damageMap = new HashMap<>();

    public static void recordDamage(Entity boss, Player player, double damage) {
        damageMap.putIfAbsent(boss.getUniqueId(), new HashMap<>());
        Map<UUID, Double> bossDamage = damageMap.get(boss.getUniqueId());
        bossDamage.put(player.getUniqueId(), bossDamage.getOrDefault(player.getUniqueId(), 0.0) + damage);
    }

    public static List<Map.Entry<UUID, Double>> getTopDamagers(Entity boss) {
        Map<UUID, Double> bossDamage = damageMap.getOrDefault(boss.getUniqueId(), new HashMap<>());
        List<Map.Entry<UUID, Double>> list = new ArrayList<>(bossDamage.entrySet());
        list.sort((a, b) -> Double.compare(b.getValue(), a.getValue())); // tri descendant
        return list;
    }

    public static void clear(Entity boss) {
        damageMap.remove(boss.getUniqueId());
    }
}