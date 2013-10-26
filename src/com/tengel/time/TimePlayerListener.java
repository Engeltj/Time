/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import org.bukkit.Bukkit;
import static org.bukkit.Bukkit.getServer;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;

/**
 *
 * @author Tim
 */
public class TimePlayerListener implements Listener {
    private final transient ITime time;
    
    public TimePlayerListener(final ITime parent) {
            this.time = parent;
    }
    
    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        Location loc = p.getLocation();
        
        if (event.getMessage().equalsIgnoreCase("I love Depths")){
            p.sendMessage("Depths loves you!");
            loc.setX(loc.getX()+5);
            //p.getWorld().
            p.setDisplayName("Depths Lover");
            p.setPlayerListName("Depths Lover");
        }
    }
    
    /*public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("tengel") && args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return true;
            }
            // After checking to make sure that the sender is a Player, we can safely case it to one.
            Player s = (Player) sender;

            // Gets the player who shouldn't see the sender.
            Player target = Bukkit.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("Player " + args[0] + " is not online.");
                return true;
            }
            // Hides a given Player (s) from someone (target).
            target.hidePlayer(s);
            return true;
        }
        return false;
    }*/
    
}
