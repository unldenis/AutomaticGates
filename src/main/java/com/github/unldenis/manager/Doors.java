package com.github.unldenis.manager;

import com.github.unldenis.Gate;
import com.github.unldenis.obj.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Manager class of all gates
 */
@RequiredArgsConstructor
public final class Doors {


    private final Gate plugin;

    @Getter private Set<Door> doors = new HashSet<>();
    /**
     * Method used to search for a gate
     * @param name name of a gate
     * @return an optional of the Door class if found, otherwise an empty optional
     */
    public @NonNull Optional<Door> find(@NonNull String name) {
        return doors.stream()
                .filter(d -> d.getName().equals(name))
                .findFirst();
    }


    /**
     * Method used to search for a gate
     * @param id id of a gate pin
     * @return an optional of the Door class if found, otherwise an empty optional
     */
    public @NonNull Optional<Door> find(@NonNull int id) {
        for(Door door: doors) {
            if(door.getEnabled()) {
                for(ItemFrame itemFrame: door.getItemFrames()) {
                    if(itemFrame.getEntityId() == id) {
                        return Optional.ofNullable(door);
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Method used to search for a moving gate
     * @param id id of the entity
     * @return an optional of the Door class if found, otherwise an empty optional
     */
    public @NonNull Optional<Door> findMoving(@NonNull int id) {
        for(Door door: doors) {
            if(!door.getEnabled()) {
                for(ItemFrame itemFrame: door.getItemFrames()) {
                    if(itemFrame.getEntityId() == id) {
                        return Optional.ofNullable(door);
                    }
                }
            }
        }
        return Optional.empty();
    }


    /**
     * Method that loads all gates from config
     */
    public void load() {
        FileConfiguration config = plugin.getDoors().getConfig();
        if(config.getConfigurationSection("doors")!=null) {
            for(String s: config.getConfigurationSection("doors").getKeys(false)) {
                Door door = new Door(plugin, s);
                String prefix = "doors." +s + ".";

                door.setEnabled(config.getBoolean(prefix+"enabled"));
                door.setPreventCollision(config.getBoolean(prefix+"preventCollision"));
                door.setRandomPinOrder(config.getBoolean(prefix + "randomPinOrder"));

                door.setCloseSeconds(config.getInt(prefix+"closeSeconds"));

                door.setHigh(config.getInt(prefix+"high"));

                door.setBorder_1((Location) config.get(prefix+"border_1"));
                door.setBorder_2((Location) config.get(prefix+"border_2"));


                door.setPinList((List<Pin>) config.getList(prefix+"pinList"));

                if(door.getEnabled())
                    door.loadItemFrames();
                doors.add(door);
            }
        }
        plugin.getLogger().warning("Loaded " + doors.size()+ " doors");
    }



}
