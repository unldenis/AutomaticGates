package com.github.unldenis.inventory;

import com.github.unldenis.obj.*;
import com.github.unldenis.util.*;
import lombok.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.util.*;

import java.lang.reflect.*;

public class DoorMenu extends Menu {
    private final Door door;

    /**
     * Constructor for Menu. Pass in a PlayerMenuUtility so that
     * we have information on who's menu this is and what
     * info is to be transfered.
     *
     * @param player
     * @param door
     */
    public DoorMenu(@NonNull Player player, Door door) {
        super(player);
        this.door = door;
    }

    /**
     * let each menu decide their name
     */
    @Override
    public String getMenuName() {
        return door.getName() + " setup";
    }

    /**
     * let each menu decide their slot amount
     */
    @Override
    public int getSlots() {
        return 27;
    }

    /**
     * let each menu decide how the items in the menu will be handled when clicked
     *
     * @param e
     */
    @Override
    public void handleMenu(InventoryClickEvent e) {
        String displayName = e.getCurrentItem().getItemMeta().getDisplayName().replace("§f§n", "");
        Field field = ReflectionUtils.findField(door.getClass(), displayName);
        ReflectionUtils.makeAccessible(field);
        Object value = ReflectionUtils.getField(field, door);

        if (value.getClass().equals(Boolean.class)) {
            if(field.getName().equals("enabled")) {
                door.setEnabled(!door.getEnabled());
                if (door.getEnabled()) {
                    try {
                        door.loadItemFrames();
                        for (ItemFrame itemFrame : door.getItemFrames())
                            itemFrame.setItem(null);
                        player.sendMessage(field.getName() + " set to " + ChatColor.GREEN + door.getEnabled());

                        if (door.getPlugin().getConfigYml().getConfig().getBoolean("auto-save")) {
                            door.save();
                            player.sendMessage(ChatColor.GREEN + "Saved");
                        }
                    }catch (NullPointerException exception) {
                        player.sendMessage(ChatColor.RED +"First you need to set all the pins");
                        door.setEnabled(false);
                    }
                }
            }else{
                door.setPreventCollision(!door.getPreventCollision());
                player.sendMessage(field.getName() + " set to " + ChatColor.GREEN + door.getEnabled());
            }
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
            this.open(); //Open again this menu

        } else if (value.getClass().equals(Location.class)) {
            Vector dirToDestination = player.getEyeLocation().getDirection().normalize();
            RayTraceResult rts = player.getWorld().rayTraceBlocks(player.getEyeLocation(), dirToDestination, 5);
            if (rts != null) {
                if (rts.getHitBlock() != null) {
                    Location rtsLoc = rts.getHitBlock().getLocation();

                    ReflectionUtils.setField(field, door, rtsLoc);
                    player.sendMessage(field.getName() + " set to " + ChatColor.GREEN + rtsLoc.toVector());
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                }
            }
        } else if (value.getClass().equals(Integer.class)) {
            Integer valueC = (Integer) value;
            if(e.getClick().equals(ClickType.LEFT)) {
                valueC++;
            }else if(e.getClick().equals(ClickType.RIGHT)) {
                valueC--;
            }
            if(valueC > 0) {
                ReflectionUtils.setField(field, door, valueC);
                player.sendMessage(field.getName()+" set to "  + ChatColor.GREEN + valueC);
                this.open(); //Open again this menu
            }
        } else {
            Vector dirToDestination = player.getEyeLocation().getDirection().normalize();
            RayTraceResult rts = player.getWorld().rayTraceEntities(player.getEyeLocation(), dirToDestination, 5, entity -> entity instanceof ItemFrame);
            if (rts != null) {
                if (rts.getHitEntity() != null) {
                    ItemFrame itemFrame = (ItemFrame) rts.getHitEntity();

                    Pin pin = (Pin) value;
                    pin.setLocation(itemFrame.getLocation());
                    pin.setPassword(itemFrame.getItem());

                    player.sendMessage(field.getName() + " set to " + ChatColor.GREEN + itemFrame.getLocation().toVector());
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                }
            }
        }
    }

    /**
     * let each menu decide what items are to be placed in the inventory menu
     */
    @Override
    public void setMenuItems() {
        for (Field field : door.getClass().getDeclaredFields()) {
            field.setAccessible(true); // You might want to set modifier to public first.
            ItemStack itemStack =  getItemStack(field);
            if(itemStack!=null) getInventory().addItem(itemStack);
        }
    }

    private ItemStack getItemStack(Field field) {
        try {
            if(field.getType().equals(Boolean.class)) {
                Boolean value = (Boolean) field.get(door);
                if(value==null)
                    return null;
                ItemStack itemStack;
                if(value) {
                    itemStack = new ItemStack(Material.LIME_DYE, 1);
                    ItemMeta meta = itemStack.getItemMeta();
                    meta.setDisplayName("§f§n" + field.getName());
                    itemStack.setItemMeta(meta);
                }else{
                    itemStack = new ItemStack(Material.GRAY_DYE, 1);
                    ItemMeta meta = itemStack.getItemMeta();
                    meta.setDisplayName("§f§n" + field.getName());
                    itemStack.setItemMeta(meta);
                }
                return itemStack;
            }else if(field.getType().equals(Integer.class)) {
                Integer value = (Integer) field.get(door);
                if(value==null)
                    return null;
                ItemStack itemStack = new ItemStack(Material.FIREWORK_ROCKET, value);
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName("§f§n" + field.getName());
                itemStack.setItemMeta(meta);
                return itemStack;
            }
            else if(field.getType().equals(Location.class)) {
                ItemStack itemStack = new ItemStack(Material.NAME_TAG);
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName("§f§n" + field.getName());
                itemStack.setItemMeta(meta);
                return itemStack;
            }
            else if(field.getType().equals(Pin.class)) {
                ItemStack itemStack = new ItemStack(Material.ITEM_FRAME);
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName("§f§n" + field.getName());
                itemStack.setItemMeta(meta);
                return itemStack;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
