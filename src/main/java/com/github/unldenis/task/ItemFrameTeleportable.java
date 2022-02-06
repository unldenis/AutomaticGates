package com.github.unldenis.task;

import org.bukkit.*;
import org.bukkit.entity.*;

public class ItemFrameTeleportable implements Workload {

    private final ItemFrame itemFrame;
    private final Location dest;

    public ItemFrameTeleportable(ItemFrame itemFrame, Location dest) {
        this.itemFrame = itemFrame;
        this.dest = dest.clone();
    }

    @Override
    public void compute() {
        itemFrame.teleport(dest);
    }
}
