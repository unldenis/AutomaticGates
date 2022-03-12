package com.github.unldenis.listener;

import com.github.unldenis.*;
import com.github.unldenis.packetmanipulator.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.*;

public class PlayerListener implements Listener {

    private final Gate plugin;

    public PlayerListener(@NotNull Gate plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        new PacketListenerImpl(event.getPlayer(), plugin);

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PacketListener.eject(event.getPlayer());
    }

}
