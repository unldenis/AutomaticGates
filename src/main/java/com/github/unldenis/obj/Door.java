package com.github.unldenis.obj;

import com.github.unldenis.Gate;
import com.github.unldenis.task.*;
import com.github.unldenis.util.*;
import com.sun.jna.platform.win32.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.*;


/**
 * Class representing a gate. The gate can go up or down, etc.
 */
public @Getter class Door {

    private final Gate plugin;
    private final String name;
    @Setter private Location border_1;
    @Setter private Location border_2;
    @Setter private List<Pin> pinList = new ArrayList<>();
    @Setter private Integer closeSeconds = 30;
    @Setter private Integer high = 4;
    @Setter private Boolean preventCollision = true;
    @Setter private Boolean enabled = false;
    @Setter private Boolean randomPinOrder = false;
    private final Set<ItemFrame> itemFrames = new HashSet<>();
    private Vector tempMidpoint;

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
        World world = pinList.get(0).getLocation().getWorld();
        tempMidpoint = pinList.get(0).getLocation().toVector();
        //calculate midpoint
        for(int j=1; j< pinList.size(); j++) {
            tempMidpoint = tempMidpoint.getMidpoint(pinList.get(j).getLocation().toVector());
        }
        // get highest radius from midpoint
        double dis = Double.MIN_VALUE;
        for(Pin pin: pinList) {
            double temp = pin.getLocation().toVector().distanceSquared(tempMidpoint);
            if(temp > dis) {
                dis = temp;
            }
        }
        // square because we used the distanceSquared
        dis = Math.sqrt(dis);

        // get all entities
        for(Entity entity : world.getNearbyEntities(tempMidpoint.toLocation(world)
                , dis, dis, dis)) {
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
            final WorkloadThread workloadThread = plugin.getWorkloadThread();
            @Override
            public void run() {
                if(k==high-1)  {
                    cancel();
                    goDown();
                    return;
                }

                for(int j=0; j<high; j++) {
                    Location loc1 = border_1.clone().add(0, -j+k, 0);
                    Location loc2 = border_2.clone().add(0, -j+k, 0);

                    int topBlockX = Math.max(loc1.getBlockX(), loc2.getBlockX());
                    int bottomBlockX = Math.min(loc1.getBlockX(), loc2.getBlockX());

                    int topBlockZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
                    int bottomBlockZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());

                    for(int x = bottomBlockX; x <= topBlockX; x++) {
                        for(int z = bottomBlockZ; z <= topBlockZ; z++) {
                            Block block = loc1.getWorld().getBlockAt(x, loc1.getBlockY(), z);
                            this.workloadThread.addLoad(new PlaceableBlock(loc1.getWorld().getBlockAt(block.getLocation().add(0, 1, 0)), block.getType()));
                            this.workloadThread.addLoad(new PlaceableBlock(block, Material.AIR));
                        }
                    }
                }
                for(ItemFrame itemFrame: itemFrames) {
                    this.workloadThread.addLoad(new ItemFrameTeleportable(itemFrame, itemFrame.getLocation().add(0, 1, 0)));
                }
                k++;
            }
        }

        .runTaskTimerAsynchronously(plugin, 0L, 30L);


    }

    private void goDown() {
        new BukkitRunnable() {
            int k = 0;
            final WorkloadThread workloadThread = plugin.getWorkloadThread();
            @Override
            public void run() {
                if(k==high-1) {
                    cancel();
                    enabled = true;
                    return;
                }
                if(k==0 && plugin.getCloseGate().isEnabled())
                    plugin.getCloseGate().playSound(tempMidpoint.toLocation(pinList.get(0).getLocation().getWorld()));
                for(int j=high-1; j>=0; j--) {
                    Location loc1 = border_1.clone().add(0, -j-k+high-1, 0);
                    Location loc2 = border_2.clone().add(0, -j-k+high-1, 0);

                    int topBlockX = Math.max(loc1.getBlockX(), loc2.getBlockX());
                    int bottomBlockX = Math.min(loc1.getBlockX(), loc2.getBlockX());

                    int topBlockZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
                    int bottomBlockZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());

                    for(int x = bottomBlockX; x <= topBlockX; x++) {
                        for(int z = bottomBlockZ; z <= topBlockZ; z++) {
                            Block block = loc1.getWorld().getBlockAt(x, loc1.getBlockY(), z);
                            this.workloadThread.addLoad(new PlaceableBlock(loc1.getWorld().getBlockAt(block.getLocation().add(0, -1, 0)), block.getType()));
                            this.workloadThread.addLoad(new PlaceableBlock(block, Material.AIR));
                        }
                    }
                }
                for(ItemFrame itemFrame: itemFrames)
                    this.workloadThread.addLoad(new ItemFrameTeleportable(itemFrame, itemFrame.getLocation().add(0, -1, 0)));
                k++;
            }
        }
        .runTaskTimerAsynchronously(plugin, 20L*closeSeconds, 30L);

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
            if(randomPinOrder) {
                ItemStack[] pinsList =
                        pinList
                        .stream()
                        .map(Pin::getPassword)
                        .toArray(ItemStack[]::new);
                ItemStack[] currentList=
                        itemFrames
                        .stream()
                        .map(ItemFrame::getItem)
                        .toArray(ItemStack[]::new);
                return BukkitUtils.compareArrays(pinsList, currentList);
            } else {
                for(Pin pin: pinList) {
                    if(!pin.getPassword().equals(find(pin.getLocation()).getItem())) {
                        return false;
                    }
                }
                return true;
            }
        }catch (NullPointerException e) {
            Bukkit.getLogger().severe(e+" -> " + e.getMessage() + " -> " + e.getCause());
            return false;
        }

    }

    /**
     * Method that saves the gate in the config
     */
    public void save() {
        String prefix  = "doors." + name + ".";
        FileConfiguration cfg = plugin.getDoors().getConfig();
        cfg.set(prefix + "enabled", enabled);
        cfg.set(prefix + "preventCollision", preventCollision);
        cfg.set(prefix + "randomPinOrder", randomPinOrder);

        cfg.set(prefix+"closeSeconds", closeSeconds);

        cfg.set(prefix+"high", high);

        cfg.set(prefix+"border_1", border_1);
        cfg.set(prefix+"border_2", border_2);

        cfg.set(prefix + "pinList", pinList);


        plugin.getDoors().saveConfig();

    }
}
