package com.github.unldenis;

import com.github.unldenis.command.MainCommand;
import com.github.unldenis.data.DataManager;
import com.github.unldenis.inventory.*;
import com.github.unldenis.listener.*;
import com.github.unldenis.manager.Doors;
import com.github.unldenis.obj.*;
import com.github.unldenis.packetmanipulator.*;
import com.github.unldenis.task.*;
import com.sk89q.worldguard.bukkit.*;
import lombok.*;
import org.bukkit.*;
import org.bukkit.configuration.serialization.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.*;


@Getter
public class Gate extends JavaPlugin {


    static {
        ConfigurationSerialization.registerClass(Pin.class, "PinSer");
    }


    private DataManager doors;
    private DataManager configYml;
    private Doors doorsManager;

    private CSound openGate;
    private CSound closeGate;

    private WorkloadThread workloadThread;

    private WorldGuardPlugin worldGuardPlugin;

    @Override
    public void onEnable() {

        doors = new DataManager(this, "doors.yml");
        configYml = new DataManager(this, "config.yml");

        getCommand("gate").setExecutor(new MainCommand(this));
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new DoorListener(this), this);

        doorsManager = new Doors(this);
        doorsManager.load();

        //load sounds
        openGate = new CSound(configYml.getConfig().getString("sounds.open.name"),
                (double) configYml.getConfig().get("sounds.open.volume"), (double) configYml.getConfig().get("sounds.open.pitch"),
                configYml.getConfig().getBoolean("sounds.open.enabled"));
        closeGate = new CSound(configYml.getConfig().getString("sounds.close.name"),
                (double) configYml.getConfig().get("sounds.close.volume"),(double) configYml.getConfig().get("sounds.close.pitch"),
                configYml.getConfig().getBoolean("sounds.close.enabled"));


        workloadThread = new WorkloadThread(configYml.getConfig().getInt("MaxMillisPerTick"));
        Bukkit.getScheduler().runTaskTimer(this, workloadThread, 1L, 1L);

        //WorldGuard
        Plugin wgPlugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (wgPlugin instanceof WorldGuardPlugin) {
            worldGuardPlugin = (WorldGuardPlugin) wgPlugin;
            getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        }

        if(hasWorldGuard()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                new PacketListenerImpl(player, this);
            }
        }
    }

    @Override
    public void onDisable() {
        if(hasWorldGuard()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PacketListener.eject(player);
            }
        }
    }

    public @Nullable WorldGuardPlugin getWorldGuardPlugin() {
        return worldGuardPlugin;
    }

    public boolean hasWorldGuard() {
        return worldGuardPlugin != null;
    }
}
