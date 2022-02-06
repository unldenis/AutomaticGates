package com.github.unldenis.task;

import org.bukkit.*;
import org.bukkit.block.*;

public class PlaceableBlock implements Workload{

    private final Block block;
    private final Material type;

    public PlaceableBlock(Block block, Material type) {
        this.block = block;
        this.type = type;
    }

    @Override
    public void compute() {
        block.setType(type);
    }
}
