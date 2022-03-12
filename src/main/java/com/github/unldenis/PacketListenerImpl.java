package com.github.unldenis;

import com.github.unldenis.obj.*;
import com.github.unldenis.packetmanipulator.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.regex.*;

/**
 * This class is used only if WorldGuard is present on your server.
 * Since the events of a region are all blocked by WG, to hear the left click on the itemframe I need to listen to the packets sent by the client and server.
 * I need to intercept packets, and to do that I use my library <a href="https://github.com/unldenis/PacketManipulator">PacketManipulator</a>
 */
public class PacketListenerImpl extends PacketListener {

    private static final Pattern REGEX_PACKET_ACTION = Pattern.compile("([a-zA-Z.]+PacketPlayInUseEntity\\$)(\\d)(.+)");

    private final Gate plugin;

    public PacketListenerImpl(@NotNull Player player, @NotNull Gate gate) {
        super(player);
        this.plugin = gate;
    }

    @Override
    public boolean onPacketOut(@NotNull WrappedPacket wrappedPacket) {
        return true;
    }

    @Override
    public boolean onPacketIn(@NotNull WrappedPacket wrappedPacket) {
        if(wrappedPacket.getPacketClass().getName().endsWith("PacketPlayInUseEntity")) {
            Object action = wrappedPacket.readObject(1);
            String actionStr = action.toString();
            // IS LEFT CLICK
            if(REGEX_PACKET_ACTION.matcher(actionStr).matches()) {
                int entityID = wrappedPacket.readInt(0);
                Optional<Door> doorOptional = plugin.getDoorsManager().find(entityID);
                if(doorOptional.isPresent()) {
                    Door door = doorOptional.get();
                    for(ItemFrame itemFrame: door.getItemFrames()) {
                        if(itemFrame.getEntityId() == entityID) {
                            if(itemFrame.getItem().getType() == Material.AIR) {
                                break;
                            }
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                getPlayer().getWorld().dropItemNaturally(itemFrame.getLocation(), itemFrame.getItem());
                                itemFrame.setItem(new ItemStack(Material.AIR));
                            });
                            return false;
                        }
                    }
                }
            }
            // RIGHT CLICK HANDLED DOORLISTENER
        }
        return true;
    }

}
