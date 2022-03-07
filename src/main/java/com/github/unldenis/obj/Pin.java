package com.github.unldenis.obj;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Class representing a pin of door. Pin has a password(itemstack) and a location
 */
@Data
@SerializableAs("Pin")
public class Pin implements ConfigurationSerializable {

    private ItemStack password;
    private Location location;

    public Pin() {
    }

    public Pin(Map<String, Object> args) {
        this.password = (ItemStack) args.get("password");
        this.location = (Location) args.get("location");
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("password", password);
        result.put("location", location);
        return result;
    }

    public static Pin deserialize(Map<String, Object> args) {
        return new Pin(args);
    }
}
