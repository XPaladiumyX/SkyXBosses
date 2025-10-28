package skyxnetwork.skyXBosses.powers;

import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import skyxnetwork.skyXBosses.models.PowerData;

public abstract class AbstractPower {

    protected final JavaPlugin plugin;
    protected final LivingEntity boss;
    protected final PowerData data;

    public AbstractPower(JavaPlugin plugin, LivingEntity boss, PowerData data) {
        this.plugin = plugin;
        this.boss = boss;
        this.data = data;
    }

    public abstract void execute();

    public int getCooldown() {
        return data.getCooldown();
    }
}

