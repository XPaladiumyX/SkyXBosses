package skyxnetwork.skyXBosses.models;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import skyxnetwork.skyXBosses.SkyXBosses;

import java.util.List;

public class BossData {

    private final String name;
    private final EntityType entityType;
    private final double health;
    private final double damage;
    private final boolean enabled;
    private final boolean persistent;
    private final double speed;
    private final List<String> powers;
    private final List<String> spawnLocations;
    private final int spawnCooldown;
    private final boolean bossbarEnabled;
    private final double followDistance;
    private final String spawnMessage;
    private final List<String> deathMessages;
    private final List<String> onDeathCommands;
    private final double scale;
    private final String id;

    public BossData(YamlConfiguration config, String id) {
        this.id = id.toUpperCase();
        this.name = ChatColor.translateAlternateColorCodes('&', config.getString("name", "Unknown Boss"));
        this.entityType = EntityType.valueOf(config.getString("entityType", "ZOMBIE").toUpperCase());
        this.health = config.getDouble("health", 20);
        this.damage = config.getDouble("damage", 2);
        this.enabled = config.getBoolean("isEnabled", true);
        this.persistent = config.getBoolean("isPersistent", true);
        this.speed = config.getDouble("movementSpeedAttribute", 0.25);
        this.powers = config.getStringList("powers");
        this.spawnLocations = config.getStringList("spawnLocations");
        this.spawnCooldown = config.getInt("spawnCooldown", 1800);
        this.bossbarEnabled = config.getBoolean("IsBossbarEnabled", true);
        this.followDistance = config.getDouble("followDistance", 100);
        this.spawnMessage = ChatColor.translateAlternateColorCodes('&', config.getString("spawnMessage", ""));
        this.deathMessages = config.getStringList("deathMessages");
        this.onDeathCommands = config.getStringList("onDeathCommands");
        this.scale = config.getDouble("scale", 1.0);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getSpawnCooldown() {
        return spawnCooldown;
    }

    public List<String> getPowers() {
        return powers;
    }

    public String getId() {
        return id;
    }

    // Spawn simple au premier emplacement configuré
    public LivingEntity spawn() {
        if (spawnLocations.isEmpty()) return null;
        String[] parts = spawnLocations.get(0).split(",");
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;

        Location loc = new Location(world,
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]));

        return spawnAt(loc);
    }

    // Spawn sur une location précise
    public LivingEntity spawnAt(Location loc) {
        LivingEntity entity = (LivingEntity) loc.getWorld().spawnEntity(loc, entityType);
        entity.setCustomName(name);
        entity.setCustomNameVisible(true);
        entity.setMaxHealth(health);
        entity.setHealth(health);
        entity.setGlowing(true);

        entity.setRemoveWhenFarAway(false);
        entity.setPersistent(true);

        // Utiliser l'ID du boss comme tag
        entity.addScoreboardTag(id);

        // ✅ Nouvelle gestion du "scale" (fonctionne sur Breeze, Slime, Phantom, etc.)
        applyEntityScale(entity, scale);

        // Follow distance pour l'IA
        try {
            if (entity instanceof LivingEntity le) {
                var attrInstance = le.getAttribute(Attribute.FOLLOW_RANGE);
                if (attrInstance != null) attrInstance.setBaseValue(followDistance);
            }
        } catch (Exception ignored) {
        }

        // BossBar
        if (bossbarEnabled) {
            BossBar bar = Bukkit.createBossBar(name, BarColor.GREEN, BarStyle.SOLID);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (entity.isDead() || !entity.isValid()) {
                        bar.removeAll();
                        cancel();
                        return;
                    }

                    double health = entity.getHealth();
                    double maxHealth = entity.getMaxHealth();
                    double progress = health / maxHealth;

                    // Actualiser le texte avec la vie
                    bar.setTitle(ChatColor.translateAlternateColorCodes('&',
                            name + " &c[" + (int) health + "/" + (int) maxHealth + " HP]"));
                    bar.setProgress(Math.max(0, Math.min(1, progress)));

                    // Changer la couleur selon la vie
                    if (progress > 0.6) bar.setColor(BarColor.GREEN);
                    else if (progress > 0.3) bar.setColor(BarColor.YELLOW);
                    else bar.setColor(BarColor.RED);

                    // Ajouter/retirer les joueurs selon leur distance (50 blocks)
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        if (player.getWorld().equals(entity.getWorld()) &&
                                player.getLocation().distanceSquared(entity.getLocation()) <= 50 * 50) {
                            if (!bar.getPlayers().contains(player)) bar.addPlayer(player);
                        } else {
                            if (bar.getPlayers().contains(player)) bar.removePlayer(player);
                        }
                    });

                }
            }.runTaskTimer(SkyXBosses.getInstance(), 0, 40); // 40 ticks = 2 secondes
        }

        // Spawn message
        if (!spawnMessage.isEmpty()) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(spawnMessage));
        }

        SkyXBosses.getInstance().getLogger().info("Spawned boss " + name + " at " + loc);
        return entity;
    }

    public List<String> getDeathMessages() {
        return deathMessages;
    }

    public List<String> getOnDeathCommands() {
        return onDeathCommands;
    }

    private void applyEntityScale(LivingEntity entity, double scale) {
        if (scale == 1.0) return; // inutile d'appliquer une scale par défaut

        try {
            // Utilise l'attribut officiel Paper 1.21.3+ : GENERIC_SCALE
            var instance = entity.getAttribute(Attribute.SCALE);
            if (instance != null) {
                instance.setBaseValue(scale);
                SkyXBosses.getInstance().getLogger().info("✔ Scale attribute applied for " + entity.getType() + " (x" + scale + ")");
                return;
            }
        } catch (NoSuchFieldError err) {
            // L'attribut n'existe pas sur cette version de Bukkit → fallback
            SkyXBosses.getInstance().getLogger().warning("⚠ GENERIC_SCALE attribute not supported on this server version.");
        } catch (Exception e) {
            SkyXBosses.getInstance().getLogger().warning("⚠ Error applying scale: " + e.getMessage());
        }

        // Aucun fallback NMS → juste ignorer proprement (pour rester compatible multi-version)
        SkyXBosses.getInstance().getLogger().warning("⚠ Could not apply scale for " + entity.getType() + " (unsupported server version)");
    }

    private boolean isPersistenceAllowed(EntityType type) {
        return switch (type) {
            case WARDEN, ENDER_DRAGON -> false;
            default -> true;
        };
    }
}
