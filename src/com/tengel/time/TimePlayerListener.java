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
    private Time plugin;
    
    public TimePlayerListener(Time plugin) {
            this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        p.remove();
        Location loc = p.getLocation();
        String msg = event.getMessage();
        if (msg.equalsIgnoreCase("I love Depths")){
            p.sendMessage("Depths loves you!");
            loc.setX(loc.getX()+5);
            //p.getWorld().
            p.setDisplayName("Depths Lover");
            p.setPlayerListName("Depths Lover");
        }
        else if (msg.equalsIgnoreCase("balance"))
            p.sendMessage(Double.toString(plugin.getEconomy().getBalance(p.getName())));
    }   
}
