package com.github.unldenis;

import com.github.unldenis.command.MainCommand;
import com.github.unldenis.data.DataManager;
import com.github.unldenis.inventory.*;
import com.github.unldenis.listener.DoorListener;
import com.github.unldenis.manager.Doors;
import com.github.unldenis.obj.CSound;
import com.github.unldenis.task.*;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class Gate extends JavaPlugin {

    private DataManager doors;
    private DataManager configYml;
    private Doors doorsManager;

    private CSound openGate;
    private CSound closeGate;

    private WorkloadThread workloadThread;

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

    }

}
