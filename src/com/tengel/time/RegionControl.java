/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import org.bukkit.event.EventHandler;
import com.mewin.WGRegionEvents.events.RegionEnterEvent;
import com.mewin.WGRegionEvents.events.RegionLeaveEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
/**
 *
 * @author Tim
 */
public class RegionControl implements Listener {
    private Time plugin;
    private TimePlayers players;
    
    public RegionControl(Time plugin, TimePlayers players) {
            this.plugin = plugin;
            this.players = players;
    }

    @EventHandler
    public void onRegionEnter(RegionEnterEvent e){
        String rgName = e.getRegion().getId();
        Player player = e.getPlayer();
        if (rgName.equalsIgnoreCase("Poor") || rgName.equalsIgnoreCase("Medium") || rgName.equalsIgnoreCase("Rich")){
            plugin.sendConsole("ENTERED POOR");
            if (!checkPermissions(player,"timezone."+rgName.toLowerCase(),false)){
                player.sendMessage(plugin.getPluginName() + ChatColor.RED + "You are in a the wrong time zone! Please leave immediately.");
                BukkitTask task = new RunLater(player, plugin.getPluginName() + "You've been added to the wanted list!", "").runTaskLater(plugin, 20*1);
                ConfigWanted cw = new ConfigWanted(plugin);
                cw.addPlayer(player, 150);
            }
            
            
        }
    }
    
    public void onRegionLeave(RegionLeaveEvent e){
        String rgName = e.getRegion().getId();
        Player player = e.getPlayer();
        if (rgName.equalsIgnoreCase("Poor") || rgName.equalsIgnoreCase("Medium") || rgName.equalsIgnoreCase("Rich")){
            if (!checkPermissions(player,"timezone."+rgName.toLowerCase(),false)){
                ConfigPlayer playerConfig = plugin.getTimePlayers().getPlayerConfig(player.getName());
                playerConfig.set("wanted_expirey", System.currentTimeMillis()/1000+120*1);
            }
        }
    }
    
    //public void getPlayerBounty
    
    public boolean checkPermissions(Player p, String permission, boolean sendMessage){
        return (plugin.getPlayerListener().checkPermissions(p, permission, sendMessage));
    }
}
