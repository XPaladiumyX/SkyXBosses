package skyxnetwork.skyXBosses.models;

import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;

public class PowerData {

    private final String powerType;
    private final int cooldown;
    private final double damage;
    private final double radius;
    private final double knockback;
    private final double speed;
    private final boolean destructive;
    private final String minionType;
    private final int minionCount;
    private final double minionHealth;
    private final double minionDamage;
    private final Particle particle;
    private final Sound sound;
    private final YamlConfiguration config;

    public PowerData(YamlConfiguration config) {
        this.config = config;

        this.powerType = config.getString("powerType", "UNKNOWN").toUpperCase();
        this.cooldown = config.getInt("cooldown", 10);
        this.damage = config.getDouble("damage", 5.0);
        this.radius = config.getDouble("radius", 5.0);
        this.knockback = config.getDouble("knockback", 0.0);
        this.speed = config.getDouble("speed", 1.0);
        this.destructive = config.getBoolean("destructive", false);
        this.minionType = config.getString("minionType", "ZOMBIE");
        this.minionCount = config.getInt("minionCount", 0);
        this.minionHealth = config.getDouble("minionHealth", 10.0);
        this.minionDamage = config.getDouble("minionDamage", 2.0);

        // Load particle safely (modern API)
        Particle tmpParticle = Particle.EXPLOSION;
        try {
            String particleName = config.getString("particle", "minecraft:explosion");
            NamespacedKey key = NamespacedKey.fromString(particleName.toLowerCase());
            if (key != null) {
                Particle found = Registry.PARTICLE_TYPE.get(key);
                if (found != null) tmpParticle = found;
            }
        } catch (Exception ignored) {
        }
        this.particle = tmpParticle;

        // Load sound safely (modern API)
        Sound tmpSound = Sound.ENTITY_GENERIC_EXPLODE;
        try {
            String soundName = config.getString("sound", "minecraft:entity.generic.explode");
            NamespacedKey key = NamespacedKey.fromString(soundName.toLowerCase());
            if (key != null) {
                Sound found = Registry.SOUNDS.get(key);
                if (found != null) tmpSound = found;
            }
        } catch (Exception ignored) {
        }
        this.sound = tmpSound;
    }

    public String getPowerType() {
        return powerType;
    }

    public int getCooldown() {
        return cooldown;
    }

    public double getDamage() {
        return damage;
    }

    public double getRadius() {
        return radius;
    }

    public double getKnockback() {
        return knockback;
    }

    public double getSpeed() {
        return speed;
    }

    public boolean isDestructive() {
        return destructive;
    }

    public String getMinionType() {
        return minionType;
    }

    public int getMinionCount() {
        return minionCount;
    }

    public double getMinionHealth() {
        return minionHealth;
    }

    public double getMinionDamage() {
        return minionDamage;
    }

    public Particle getParticle() {
        return particle;
    }

    public Sound getSound() {
        return sound;
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return "PowerData{" +
                "powerType='" + powerType + '\'' +
                ", cooldown=" + cooldown +
                ", damage=" + damage +
                ", radius=" + radius +
                ", knockback=" + knockback +
                ", speed=" + speed +
                ", destructive=" + destructive +
                ", minionType='" + minionType + '\'' +
                ", minionCount=" + minionCount +
                ", minionHealth=" + minionHealth +
                ", minionDamage=" + minionDamage +
                ", particle=" + particle +
                ", sound=" + sound +
                '}';
    }
}