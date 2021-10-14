package com.github.unldenis.objectviewer;

import com.github.unldenis.inventory.BaseInventory;
import com.github.unldenis.obj.Pin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;

public class InventoryObject extends BaseInventory<Object> {

    public InventoryObject(String title, Object data) {
        super(6, title, data);
    }

    @Override
    public void init() {
        for (Field field : getData().getClass().getDeclaredFields()) {
            field.setAccessible(true); // You might want to set modifier to public first.
            ItemStack itemStack =  getItemStack(field);
            if(itemStack!=null) getInventory().setItem(getInventory().firstEmpty(), itemStack);
        }

    }


    private ItemStack getItemStack(Field field) {
        try {
            if(field.getType().equals(Boolean.class)) {
                Boolean value = (Boolean) field.get(getData());
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
                Integer value = (Integer) field.get(getData());
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
