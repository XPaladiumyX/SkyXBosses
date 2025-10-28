package skyxnetwork.skyXBosses.models;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
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

    public BossData(YamlConfiguration config) {
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
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getSpawnCooldown() {
        return spawnCooldown;
    }

    public LivingEntity spawn() {
        if (spawnLocations.isEmpty()) return null;
        String[] parts = spawnLocations.get(0).split(",");
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;

        Location loc = new Location(world,
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]));

        LivingEntity entity = (LivingEntity) world.spawnEntity(loc, entityType);
        entity.setCustomName(name);
        entity.setCustomNameVisible(true);
        entity.setMaxHealth(health);
        entity.setHealth(health);
        entity.setPersistent(persistent);
        entity.setGlowing(true);

        SkyXBosses.getInstance().getLogger().info("Spawned boss " + name + " at " + loc);
        return entity;
    }

    public List<String> getPowers() {
        return powers;
    }

    public LivingEntity spawnAt(Location loc) {
        LivingEntity entity = (LivingEntity) loc.getWorld().spawnEntity(loc, entityType);
        entity.setCustomName(name);
        entity.setCustomNameVisible(true);
        entity.setMaxHealth(health);
        entity.setHealth(health);
        entity.setPersistent(persistent);
        entity.setGlowing(true);

        SkyXBosses.getInstance().getLogger().info("Spawned boss " + name + " at " + loc);
        return entity;
    }
}
