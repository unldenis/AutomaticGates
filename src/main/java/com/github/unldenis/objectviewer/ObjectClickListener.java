package com.github.unldenis.objectviewer;


import com.github.unldenis.util.HiddenStringUtils;
import com.github.unldenis.util.ReflectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.lang.reflect.Field;
import java.util.Optional;


public class ObjectClickListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        final Player player = (Player)e.getWhoClicked();

        if(e.getClickedInventory()==null) return;

        if(e.getClickedInventory().getHolder() instanceof InventoryObject) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null || e.getCurrentItem().getType().isAir()) return;

            Optional<ObjectViewer> objectViewer = ObjectViewer.find(e.getView().getTitle());
            if(!objectViewer.isPresent())
                return;

            ObjectViewer objViewer = objectViewer.get();

            Field field = ReflectionUtils.findField(objViewer.getObj().getClass(), e.getCurrentItem().getItemMeta().getDisplayName().replace("§f§n", ""));
            ReflectionUtils.makeAccessible(field);
            Object objField = ReflectionUtils.getField(field, objViewer.getObj());

            //callback
            objViewer.getListener().onFieldClick(field, objField, e.getClick());

            //update
            InventoryObject inventoryObject = new InventoryObject(HiddenStringUtils.encodeString(String.valueOf(objViewer.getId()))+objViewer.getTitle(), objViewer.getObj());
            player.openInventory(inventoryObject.getInventory());
        }

    }
}
