package com.github.unldenis.listener;

import com.github.unldenis.Gate;
import com.github.unldenis.obj.Door;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;


@RequiredArgsConstructor
public final class DoorListener implements Listener {

    private final Gate plugin;

    @EventHandler
    public void onPut(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame)) return;
        ItemFrame itemFrame = (ItemFrame) event.getRightClicked();

        Optional<Door> doorOptional = plugin.getDoorsManager().find(itemFrame.getLocation());
        if (!doorOptional.isPresent()) return;
        Door door = doorOptional.get();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (door.isRigth()) {
                    for (ItemFrame itemFrame : door.getItemFrames())
                        itemFrame.setItem(null);
                    door.setEnabled(false);
                    if (plugin.getOpenGate().isEnabled()) plugin.getOpenGate().playSound(itemFrame.getLocation());
                    door.goUp();
                }
            }
        }
                .runTaskLater(plugin, 1L);

    }


    @EventHandler
    public void preventBreaking(HangingBreakByEntityEvent event) {
        if (!(event.getEntity() instanceof ItemFrame) ) return;
        ItemFrame itemFrame = (ItemFrame) event.getEntity();
        Optional<Door> doorOptional = plugin.getDoorsManager().find(itemFrame.getLocation());
        if (doorOptional.isPresent()) {
            event.setCancelled(true);
            if(event.getRemover() instanceof Player)  {
                Player player = (Player) event.getRemover();
                if(player.hasPermission("gate.admin"))
                    player.sendMessage(ChatColor.RED+"You can't break this pin when door is enabled");
            }

        }

    }

}
