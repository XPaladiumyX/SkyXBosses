package skyxnetwork.skyXBosses.powers;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import skyxnetwork.skyXBosses.SkyXBosses;
import skyxnetwork.skyXBosses.models.PowerData;

import java.util.List;
import java.util.Random;

public class CrushGrabPower extends AbstractPower {

    private final Random random = new Random();

    public CrushGrabPower(SkyXBosses plugin, LivingEntity boss, PowerData data) {
        super(plugin, boss, data);
    }

    @Override
    public void execute() {
        if (boss == null || boss.isDead() || !boss.isValid()) return;

        List<Player> nearby = boss.getLocation().getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(boss.getLocation()) <= 100)
                .toList();

        if (nearby.isEmpty()) return;

        Player target = nearby.get(random.nextInt(nearby.size()));

        // Effets visuels et sonores d'attaque
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1f, 0.8f);
        boss.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, boss.getLocation().add(0, 2, 0), 20, 0.4, 0.4, 0.4);

        target.sendMessage(ChatColor.GREEN + "§l>> " + ChatColor.YELLOW + "The boss grabbed you!");

        // Téléportation unique vers le boss
        Location grabLoc = boss.getLocation().add(0, 2.0, 0);
        target.teleport(grabLoc);

        // Effets de blocage et de vision
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 255, false, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, 128, false, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1, false, false, false));

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (target.isDead() || !target.isOnline() || boss.isDead()) {
                    cancel();
                    return;
                }

                // Pendant 2 secondes → effets visuels "nature" autour du joueur
                if (ticks < 40) {
                    Location loc = target.getLocation().add(0, 1, 0);
                    boss.getWorld().spawnParticle(Particle.COMPOSTER, loc, 10, 0.5, 0.5, 0.5, 0.05);
                    boss.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 2, 0.3, 0.3, 0.3, 0.01);
                    ticks++;
                } else {
                    // Fin du grab
                    target.removePotionEffect(PotionEffectType.SLOWNESS);
                    target.removePotionEffect(PotionEffectType.JUMP_BOOST);
                    target.removePotionEffect(PotionEffectType.BLINDNESS);

                    target.damage(data.getDamage(), boss);
                    target.sendMessage(ChatColor.RED + "§l>> " + ChatColor.DARK_GREEN + "You were crushed by the boss!");
                    boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1f, 0.9f);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}