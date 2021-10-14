package com.github.unldenis.obj;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

/**
 * Class representing a pin of door. Pin has a password(itemstack) and a location
 */
@Data
public class Pin {

    private ItemStack password;
    private Location location;
}
