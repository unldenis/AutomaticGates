package com.github.unldenis.command;

import com.github.unldenis.Gate;
import com.github.unldenis.inventory.*;
import com.github.unldenis.obj.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public final class MainCommand implements CommandExecutor {

    private final Gate plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only playes can run this command");
            return false;
        }
        Player player = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("gate")) {
            if (!player.hasPermission("gate.admin"))
                return true;
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("help")) {
                    if (args.length == 1) {

                        sendMessage(player, "&4Admin commands");
                        sendMessage(player, "&7/gate create <name> - &fTo create a gate");
                        sendMessage(player, "&7/gate edit <name> - &fTo edit a gate settings");
                        sendMessage(player, "&7/gate save <name>  - &fTo save a gate in config");
                        sendMessage(player, "&7/gate delete <name>  - &fTo delete a gate");
                        sendMessage(player, "&7/gate list - &fGet list of all gates");
                        /*
                        String helpMex = """
                                &4Admin commands
                                &7/gate create <name> - &fTo create a gate
                                &7/gate edit <name> - &fTo edit a gate settings
                                &7/gate save <name>  - &fTo save a gate in config
                                &7/gate delete <name>  - &fTo delete a gate
                                &7/gate list - &fGet list of all gates
                                """;
                        */
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("create")) {
                    if (args.length == 2) {
                        String name = args[1];
                        if (plugin.getDoorsManager().find(name).isPresent()) {
                            player.sendMessage(ChatColor.RED + "This door already exist");
                            return true;
                        }
                        Door door = new Door(plugin, name, player.getLocation());
                        plugin.getDoorsManager().getDoors().add(door);
                        player.sendMessage(ChatColor.GREEN + "Door created");
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("edit")) {
                    if (args.length == 2) {
                        String name = args[1];
                        Optional<Door> doorOptional = plugin.getDoorsManager().find(name);
                        if (!doorOptional.isPresent()) {
                            player.sendMessage(ChatColor.RED + "This door doesn't exist");
                            return true;
                        }
                        Door door = doorOptional.get();
                        DoorMenu doorMenu = new DoorMenu(player, door);
                        doorMenu.open();
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("save")) {
                    if (args.length == 2) {
                        String name = args[1];
                        Optional<Door> doorOptional = plugin.getDoorsManager().find(name);
                        if (!doorOptional.isPresent()) {
                            player.sendMessage(ChatColor.RED + "This door doesn't exist");
                            return true;
                        }
                        Door door = doorOptional.get();
                        door.save();
                        player.sendMessage(ChatColor.GREEN + "Saved");
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("delete")) {
                    if (args.length == 2) {
                        String name = args[1];
                        Optional<Door> doorOptional = plugin.getDoorsManager().find(name);
                        if (!doorOptional.isPresent()) {
                            player.sendMessage(ChatColor.RED + "This door doesn't exist");
                            return true;
                        }
                        Door door = doorOptional.get();
                        plugin.getDoorsManager().getDoors().remove(door);
                        plugin.getDoors().getConfig().set("doors." + door.getName(), null);
                        plugin.getDoors().saveConfig();
                        player.sendMessage(ChatColor.GREEN + "Deleted");
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("list")) {
                    if (args.length == 1) {
                        Set<Door> doorSet = plugin.getDoorsManager().getDoors();
                        player.sendMessage("Gates size(" + ChatColor.GREEN + doorSet.size() + ChatColor.RESET + ")");

                        for (Door door : doorSet) {
                            String name = door.getName();

                            TextComponent message = new TextComponent(name);
                            message.setBold(true);
                            message.setUnderlined(true);

                            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gate tp " + name));

                            player.spigot().sendMessage(message);
                        }
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("tp")) {
                    if (args.length == 2) {
                        String name = args[1];
                        Optional<Door> doorOptional = plugin.getDoorsManager().find(name);
                        if (!doorOptional.isPresent()) {
                            player.sendMessage(ChatColor.RED + "This door doesn't exist");
                            return true;
                        }
                        Door door = doorOptional.get();

                        /*
                        Vector fir = door.getPin_1().getLocation().clone().add(door.getPin_1().getLocation().getDirection()).toVector();
                        Vector sec = door.getPin_4().getLocation().clone().add(door.getPin_4().getLocation().getDirection()).toVector();
                        Vector midpoint = fir.midpoint(sec);
                        Location midPointLoc = midpoint.toLocation(door.getPin_1().getLocation().getWorld());
                        midPointLoc.setDirection(midPointLoc.getDirection().multiply(-1));
                        player.teleport(midPointLoc);
                         */
                        new BukkitRunnable() {
                            Pin tempPin = door.getPinList().get(0);
                            double cos = tempPin.getLocation().getDirection().getZ();
                            double sen = tempPin.getLocation().getDirection().getX();
                            double angle = Math.toDegrees(Math.atan2(sen, cos)) - 60;

                            double tick = 0.0;
                            double dist = 5;

                            Location loc;

                            public void run() {
                                ++tick;

                                loc = tempPin.getLocation().clone().add(sen, 0, cos);
                                loc.setDirection(tempPin.getLocation().toVector().subtract(player.getLocation().toVector()));
                                player.teleport(loc);

                                angle += tick / 10;
                                cos = Math.cos(Math.toRadians(angle)) * dist;
                                sen = Math.sin(Math.toRadians(angle)) * dist;

                                if (tick == 50)
                                    cancel();
                            }
                        }.runTaskTimer(plugin, 0L, 1L);


                        return true;
                    }
                }

            }
            player.sendMessage(ChatColor.RED + "Invalid format. Usage: /gate help");
            return true;
        }
        return false;
    }


    private void sendMessage(@NonNull Player player, @NonNull String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
