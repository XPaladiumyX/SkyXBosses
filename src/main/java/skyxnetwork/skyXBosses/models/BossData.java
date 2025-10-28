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
        entity.setPersistent(persistent);
        entity.setGlowing(true);
        entity.addScoreboardTag(name.replaceAll("§", ""));

        try {
            entity.getClass().getMethod("setScale", float.class).invoke(entity, (float) scale);
        } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException ignored) {
            // Si la version de Minecraft ne supporte pas setScale (anciennes versions)
        }

        // Follow distance pour l'IA
        try {
            if (entity instanceof org.bukkit.entity.LivingEntity le) {
                org.bukkit.attribute.Attribute followAttr = null;
                try {
                    // Tentative d'accès direct à l'attribut (1.21+)
                    followAttr = Attribute.FOLLOW_RANGE;
                } catch (NoSuchFieldError ignored) {
                    // Si l'attribut n'existe pas, on peut l'ignorer (anciennes versions)
                }

                if (followAttr != null) {
                    var attrInstance = le.getAttribute(followAttr);
                    if (attrInstance != null) {
                        attrInstance.setBaseValue(followDistance);
                    }
                }
            }
        } catch (Exception ignored) {
            // ignore si non supporté
        }


        // BossBar
        if (bossbarEnabled) {
            BossBar bar = Bukkit.createBossBar(name, BarColor.RED, BarStyle.SOLID);
            Bukkit.getOnlinePlayers().forEach(bar::addPlayer);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (entity.isDead() || !entity.isValid()) {
                        bar.removeAll();
                        cancel();
                        return;
                    }
                    bar.setProgress(entity.getHealth() / entity.getMaxHealth());
                }
            }.runTaskTimer(SkyXBosses.getInstance(), 0, 20);
        }

        // Spawn message
        if (!spawnMessage.isEmpty()) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(spawnMessage));
        }

        // Logger
        SkyXBosses.getInstance().getLogger().info("Spawned boss " + name + " at " + loc);

        return entity;
    }

    public List<String> getDeathMessages() {
        return deathMessages;
    }

    public List<String> getOnDeathCommands() {
        return onDeathCommands;
    }
    
    public String getId() {
        return id;
    }
}