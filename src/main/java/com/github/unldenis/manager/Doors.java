package com.github.unldenis.manager;

import com.github.unldenis.Gate;
import com.github.unldenis.obj.Door;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
     * Deprecated method used to search for a gate
     * @param location location of a gate pin
     * @return an optional of the Door class if found, otherwise an empty optional
     */
    @Deprecated
    public @NonNull Optional<Door> find(@NonNull Location location) {
        for(Door door: doors) {
            if(door.getEnabled() && (door.getPin_1().getLocation().equals(location) ||
                    door.getPin_2().getLocation().equals(location) ||
                    door.getPin_3().getLocation().equals(location) ||
                    door.getPin_4().getLocation().equals(location))) {
                return Optional.ofNullable(door);
            }
        }
        return Optional.empty();
    }
    /**
     * Method used to search for a gate
     * @param id id of a gate pin
     * @return an optional of the Door class if found, otherwise an empty optional
     */
    public @NonNull Optional<Door> find(@NonNull int id) {
        for(Door door: doors) {
            if(door.getEnabled() && (door.getItemFrames().get(0).getEntityId()==id ||
                    door.getItemFrames().get(1).getEntityId()==id ||
                    door.getItemFrames().get(2).getEntityId()==id ||
                    door.getItemFrames().get(3).getEntityId()==id)) {
                return Optional.ofNullable(door);
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
            if(!door.getEnabled() && (door.getItemFrames().get(0).getEntityId()==id ||
                    door.getItemFrames().get(1).getEntityId()==id ||
                    door.getItemFrames().get(2).getEntityId()==id ||
                    door.getItemFrames().get(3).getEntityId()==id)) {
                return Optional.ofNullable(door);
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

                door.setCloseSeconds(config.getInt(prefix+"closeSeconds"));

                door.setHigh(config.getInt(prefix+"high"));

                door.setBorder_1((Location) config.get(prefix+"border_1"));
                door.setBorder_2((Location) config.get(prefix+"border_2"));


                door.getPin_1().setLocation((Location) config.get(prefix+"pin_1.location"));
                door.getPin_1().setPassword((ItemStack) config.get(prefix+"pin_1.password"));

                door.getPin_2().setLocation((Location) config.get(prefix+"pin_2.location"));
                door.getPin_2().setPassword((ItemStack) config.get(prefix+"pin_2.password"));

                door.getPin_3().setLocation((Location) config.get(prefix+"pin_3.location"));
                door.getPin_3().setPassword((ItemStack) config.get(prefix+"pin_3.password"));

                door.getPin_4().setLocation((Location) config.get(prefix+"pin_4.location"));
                door.getPin_4().setPassword((ItemStack) config.get(prefix+"pin_4.password"));


                if(door.getEnabled())
                    door.loadItemFrames();
                doors.add(door);
            }
        }
        plugin.getLogger().warning("Loaded " + doors.size()+ " doors");
    }



}
