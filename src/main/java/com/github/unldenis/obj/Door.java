package com.github.unldenis.obj;

import com.github.unldenis.Gate;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


/**
 * Class representing a gate. The gate can go up or down, etc.
 */
public @Getter class Door {

    private final Gate plugin;
    private final String name;
    @Setter private Location border_1;
    @Setter private Location border_2;
    private Pin pin_1 = new Pin();
    private Pin pin_2 = new Pin();
    private Pin pin_3 = new Pin();
    private Pin pin_4 = new Pin();
    @Setter private Integer closeSeconds = 30;
    @Setter private Boolean enabled = false;
    private List<ItemFrame> itemFrames = new ArrayList<>();

    /**
     * Constructor used in the create command of the Door class
     * @param plugin equals to main class plugin
     * @param name name of the door
     * @param testLoc this location is set to border_1/2
     */
    public Door(@NonNull Gate plugin, @NonNull String name, @NonNull Location testLoc) {
        this.plugin = plugin;
        this.name = name;
        border_1 = testLoc;
        border_2 = testLoc;
    }

    /**
     * Constructor used in the loading from config of the Door class
     * @param plugin equals to main class plugin
     * @param name name of the door
     */
    public Door(@NonNull Gate plugin, @NonNull String name) {
        this.plugin = plugin;
        this.name = name;
    }

    /**
     * Method used to obtain the entity of type ItemFrame between pin 1 and 4
     */
    public void loadItemFrames() {
       /*
          MOVE ITEM FRAMES TOO
       */
        Vector midpoint = pin_1.getLocation().toVector().getMidpoint(pin_4.getLocation().toVector());
        double dis = pin_1.getLocation().distance(pin_4.getLocation());
        for(Entity entity : pin_1.getLocation().getWorld().getNearbyEntities(midpoint.toLocation(pin_4.getLocation().getWorld())
                , dis/2, dis/2, dis/2)) {
            if(entity instanceof ItemFrame) {
                ItemFrame itemFrame = (ItemFrame) entity;
                itemFrames.add(itemFrame);
            }
        }
    }

    /**
     * Method that makes the door go up and automatically comes back down
     */
    public void goUp() {
        new BukkitRunnable() {
            int k = 0;
            @Override
            public void run() {
                if(k==3)  {
                    cancel();
                    goDown();
                    return;
                }

                for(int j=0; j<4; j++) {
                    Location loc1 = border_1.clone().add(0, -j+k, 0);
                    Location loc2 = border_2.clone().add(0, -j+k, 0);

                    int topBlockX = (loc1.getBlockX() < loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());
                    int bottomBlockX = (loc1.getBlockX() > loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());

                    int topBlockZ = (loc1.getBlockZ() < loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());
                    int bottomBlockZ = (loc1.getBlockZ() > loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());

                    for(int x = bottomBlockX; x <= topBlockX; x++) {
                        for(int z = bottomBlockZ; z <= topBlockZ; z++) {
                            Block block = loc1.getWorld().getBlockAt(x, loc1.getBlockY(), z);
                            loc1.getWorld().getBlockAt(block.getLocation().add(0, 1, 0)).setType(block.getType());
                            block.setType(Material.AIR);
                        }
                    }
                }
                for(ItemFrame itemFrame: itemFrames)
                    itemFrame.teleport(itemFrame.getLocation().add(0, 1, 0));
                k++;
            }
        }

        .runTaskTimer(plugin, 0L, 30L);


    }

    private void goDown() {
        new BukkitRunnable() {
            int k = 0;
            @Override
            public void run() {
                if(k==3) {
                    cancel();
                    enabled = true;
                    return;
                }
                if(k==0 && plugin.getCloseGate().isEnabled())
                    plugin.getCloseGate().playSound(pin_1.getLocation().toVector().getMidpoint(pin_4.getLocation().toVector()).toLocation(pin_1.getLocation().getWorld()));
                for(int j=3; j>=0; j--) {
                    Location loc1 = border_1.clone().add(0, -j-k+3, 0);
                    Location loc2 = border_2.clone().add(0, -j-k+3, 0);

                    int topBlockX = (loc1.getBlockX() < loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());
                    int bottomBlockX = (loc1.getBlockX() > loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());

                    int topBlockZ = (loc1.getBlockZ() < loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());
                    int bottomBlockZ = (loc1.getBlockZ() > loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());

                    for(int x = bottomBlockX; x <= topBlockX; x++) {
                        for(int z = bottomBlockZ; z <= topBlockZ; z++) {
                            Block block = loc1.getWorld().getBlockAt(x, loc1.getBlockY(), z);
                            loc1.getWorld().getBlockAt(block.getLocation().add(0, -1, 0)).setType(block.getType());
                            block.setType(Material.AIR);
                        }
                    }
                }
                for(ItemFrame itemFrame: itemFrames)
                    itemFrame.teleport(itemFrame.getLocation().add(0, -1, 0));
                k++;
            }
        }
        .runTaskTimer(plugin, 20L*closeSeconds, 30L);

    }


    private @Nullable ItemFrame find(Location location) {
        for(ItemFrame itemFrame: itemFrames) {
            if(itemFrame.getLocation().equals(location))
                return itemFrame;
        }
        return null;
    }

    /**
     * Method that checks if the entered password is correct
     * @return true if password code is right
     */
    public boolean isRigth() {
        try {
            return (pin_1.getPassword().equals(find(pin_1.getLocation()).getItem()) &&
                    pin_2.getPassword().equals(find(pin_2.getLocation()).getItem()) &&
                    pin_3.getPassword().equals(find(pin_3.getLocation()).getItem()) &&
                    pin_4.getPassword().equals(find(pin_4.getLocation()).getItem()));
        }catch (NullPointerException e) {
            System.out.println(e+" -> " + e.getMessage() + " -> " + e.getCause());
        }
        return false;

    }

    /**
     * Method that saves the gate in the config
     */
    public void save() {
        String prefix  = "doors." + name + ".";

        plugin.getDoors().getConfig().set(prefix+"enabled", enabled.booleanValue());

        plugin.getDoors().getConfig().set(prefix+"closeSeconds", closeSeconds);

        plugin.getDoors().getConfig().set(prefix+"border_1", border_1);
        plugin.getDoors().getConfig().set(prefix+"border_2", border_2);


        plugin.getDoors().getConfig().set(prefix+"pin_1.password", pin_1.getPassword());
        plugin.getDoors().getConfig().set(prefix+"pin_1.location", pin_1.getLocation());

        plugin.getDoors().getConfig().set(prefix+"pin_2.password", pin_2.getPassword());
        plugin.getDoors().getConfig().set(prefix+"pin_2.location", pin_2.getLocation());

        plugin.getDoors().getConfig().set(prefix+"pin_3.password", pin_3.getPassword());
        plugin.getDoors().getConfig().set(prefix+"pin_3.location", pin_3.getLocation());

        plugin.getDoors().getConfig().set(prefix+"pin_4.password", pin_4.getPassword());
        plugin.getDoors().getConfig().set(prefix+"pin_4.location", pin_4.getLocation());

        plugin.getDoors().saveConfig();

    }
}
