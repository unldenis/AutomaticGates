package com.github.unldenis.listener;

import com.github.unldenis.*;
import com.github.unldenis.obj.Door;
import com.sk89q.worldedit.bukkit.*;
import com.sk89q.worldguard.*;
import com.sk89q.worldguard.protection.*;
import com.sk89q.worldguard.protection.regions.*;
import lombok.RequiredArgsConstructor;
import org.bukkit.*;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;

import java.util.*;


@RequiredArgsConstructor
public final class DoorListener implements Listener {

    private final Gate plugin;


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPut(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame)) return;
        ItemFrame itemFrame = (ItemFrame) event.getRightClicked();

        Optional<Door> doorOptional = plugin.getDoorsManager().find(itemFrame.getEntityId());
        if (!doorOptional.isPresent()) return;

        // WorldGuard Support
        if(plugin.hasWorldGuard()) {
            Player player = event.getPlayer();
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
            if(set.size() > 0) {
                if(itemFrame.getItem().getType() == Material.AIR) {
                    ItemStack itemInMainHand = event.getPlayer().getInventory().getItemInMainHand();
                    if(itemInMainHand.getType() != Material.AIR) {
                        event.setCancelled(true);
                        itemFrame.setItem(itemInMainHand);
                        itemInMainHand.setAmount(itemInMainHand.getAmount() - 1);
                    }
                }
            }
        }
        final Door door = doorOptional.get();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (door.isRigth()) {
                for (ItemFrame itmFrame : door.getItemFrames()) {
                    itmFrame.setItem(null);
                }
                door.setEnabled(false);
                if (plugin.getOpenGate().isEnabled()) plugin.getOpenGate().playSound(itemFrame.getLocation());
                door.goUp();
            }
        }, 1L);

    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void preventBreaking(HangingBreakByEntityEvent event) {
        if (!(event.getEntity() instanceof ItemFrame) ) return;
        ItemFrame itemFrame = (ItemFrame) event.getEntity();
        Optional<Door> doorOptional = plugin.getDoorsManager().find(itemFrame.getEntityId());
        if (doorOptional.isPresent()) {
            event.setCancelled(true);
            if(event.getRemover() instanceof Player)  {
                Player player = (Player) event.getRemover();
                if(player.hasPermission("gate.admin"))
                    player.sendMessage(ChatColor.RED+"You can't break this pin when door is enabled");
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHangingBreak(HangingBreakEvent event) {
        if(event.getEntity() instanceof ItemFrame && event.getCause().equals(HangingBreakEvent.RemoveCause.OBSTRUCTION)) {
            ItemFrame itemFrame = (ItemFrame) event.getEntity();
            Optional<Door> doorOptional = plugin.getDoorsManager().findMoving(itemFrame.getEntityId());
            if (doorOptional.isPresent() && doorOptional.get().getPreventCollision()) {
                event.setCancelled(true);
            }
        }

    }
}
