package skyxnetwork.skyXBosses.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import skyxnetwork.skyXBosses.SkyXBosses;
import skyxnetwork.skyXBosses.models.PowerData;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class PowerManager {

    private final SkyXBosses plugin;
    private final Map<String, PowerData> powers = new HashMap<>();

    public PowerManager(SkyXBosses plugin) {
        this.plugin = plugin;
        loadPowers();
    }

    public void loadPowers() {
        powers.clear();

        File folder = new File(plugin.getDataFolder(), "Powers");
        if (!folder.exists()) {
            folder.mkdirs();
            plugin.getLogger().info("Created Powers folder (no power files detected).");
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("No power files found in Powers folder!");
            return;
        }

        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                PowerData data = new PowerData(config);

                String key = file.getName().replace(".yml", "").toUpperCase();
                powers.put(key, data);

                plugin.getLogger().info("Loaded power: " + data.getPowerType() + " (" + key + ")");
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load power file: " + file.getName(), ex);
            }
        }

        plugin.getLogger().info("✅ Loaded " + powers.size() + " powers successfully.");
    }

    public PowerData getPower(String name) {
        return powers.get(name.toUpperCase());
    }

    public Map<String, PowerData> getAllPowers() {
        return powers;
    }
}
