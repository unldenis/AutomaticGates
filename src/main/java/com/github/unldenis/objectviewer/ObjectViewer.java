package com.github.unldenis.objectviewer;

import com.github.unldenis.util.HiddenStringUtils;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Getter
public class ObjectViewer<T> {

    private static Set<ObjectViewer> objectViewerSet = new HashSet<>();

    private T obj;
    private int id;
    private String title;
    private UUID player;
    private OnFieldClickListener listener;

    public ObjectViewer(T obj) {
        this.obj = obj;
        this.id = objectViewerSet.size();

        objectViewerSet.add(this);
    }


    public void open(String title, Player player, OnFieldClickListener listener) {
        this.title = title;
        this.player = player.getUniqueId();
        this.listener = listener;
        InventoryObject inventoryObject = new InventoryObject(HiddenStringUtils.encodeString(String.valueOf(id))+title, obj);
        player.openInventory(inventoryObject.getInventory());
    }


    public static Optional<ObjectViewer> find(String title) {
        if(!HiddenStringUtils.hasHiddenString(title)) return Optional.empty();
        String _id =  HiddenStringUtils.extractHiddenString(title);
        for(ObjectViewer objectV: objectViewerSet)
            if(objectV.id == Integer.parseInt(_id))
                return Optional.ofNullable(objectV);
        return Optional.empty();
    }


}
