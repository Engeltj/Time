/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import org.bukkit.event.EventHandler;
import com.mewin.WGRegionEvents.events.RegionEnterEvent;
import com.mewin.WGRegionEvents.events.RegionLeaveEvent;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import static com.sk89q.worldguard.bukkit.BukkitUtil.*;
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
        if (isWrongTimeZone(player.getName(), getZoneId(rgName)))
            player.sendMessage(plugin.getPluginName() + ChatColor.RED + "You are in a the wrong time zone! Please leave immediately.");
        //if (rgName.equalsIgnoreCase("Poor") || rgName.equalsIgnoreCase("Wealthy") || rgName.equalsIgnoreCase("Rich")){
            //plugin.sendConsole("ENTERED POOR");
            //if (!checkPermissions(player,"timezone."+rgName.toLowerCase(),false)){
                
                //BukkitTask task = new RunLater(player, plugin.getPluginName() + "You've been added to the wanted list!", "").runTaskLater(plugin, 20*1);
                //ConfigWanted cw = new ConfigWanted(plugin);
                //cw.addPlayer(player, 150);
            //}
       // }
    }
    
    public void onRegionLeave(RegionLeaveEvent e){
        String rgName = e.getRegion().getId();
        Player player = e.getPlayer();
        if (rgName.equalsIgnoreCase("Poor") || rgName.equalsIgnoreCase("Wealthy") || rgName.equalsIgnoreCase("Rich")){
            if (!checkPermissions(player,"timezone."+rgName.toLowerCase(),false)){
                //ConfigPlayer playerConfig = plugin.getTimePlayers().getPlayerConfig(player.getName());
                //playerConfig.set("wanted_expirey", System.currentTimeMillis()/1000+120*1);
            }
        }
    }
    
    private int getZoneId(String timezone){
        if (timezone.equalsIgnoreCase("Rich"))
            return 2;
        else if (timezone.equalsIgnoreCase("Wealthy"))
            return 1;
        else
            return 0;
    }
    
    public int getPlayerTimeZone(Player p){
        ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(p.getName());
        return cp.getPlayerTimeZone();
    }
    
    public boolean isWrongTimeZone(String player, int zone){
        ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(player);
        if (zone > cp.getPlayerTimeZone())
            return true;
        return false;
    }
    
    public boolean isWrongTimeZone(Player p){
        Location loc = p.getLocation();
        Vector v = toVector(loc);
        RegionManager manager = plugin.worldGuard.getRegionManager(p.getWorld());
        ApplicableRegionSet set = manager.getApplicableRegions(v);
        for (ProtectedRegion each : set){
            if (getZoneId(each.getId()) > getPlayerTimeZone(p))
                return true;
        }
        return false;
    }
    //public void getPlayerBounty
    
    public boolean checkPermissions(Player p, String permission, boolean sendMessage){
        return (plugin.getPlayerListener().checkPermissions(p, permission, sendMessage));
    }
}
