package com.github.unldenis.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public abstract class BaseInventory<T> implements InventoryHolder {

    private Inventory inv;
    private T data;

    public BaseInventory(int rows, String title, T data) {
        this.inv = Bukkit.createInventory((InventoryHolder)this, rows*9, title);
        this.data = data;
        this.init();
    }
    public Inventory getInventory() {
        return inv;
    }

    public T getData() {
        return data;
    }

    public abstract void init();

    public ItemStack basicItemStack(Material material, String displayName, String...lore) {
        ItemStack itemStack = new ItemStack(material, 1);
        ItemMeta meta = itemStack.getItemMeta();
        meta.addEnchant(Enchantment.LURE, 1, false);
        meta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ENCHANTS });
        meta.setDisplayName(displayName);
        meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
