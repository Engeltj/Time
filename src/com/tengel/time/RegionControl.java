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
import com.tengel.time.homes.Homes;
import com.tengel.time.profs.TimeProfession;
import org.bukkit.Material;
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
        Player p = e.getPlayer();
        if (isWrongTimeZone(p.getName(), getZoneId(rgName)))
            p.sendMessage(plugin.getPluginName() + ChatColor.RED + "You are in a the wrong time zone! Please leave immediately.");
        Homes h = new Homes(plugin);
        if (h.isHome(rgName)){
            ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(p.getName());
            TimeCommands tc = new TimeCommands();
            if (h.isAvailable(rgName)){
                double price = h.getPrice(rgName);
                p.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "This home is available for " + ChatColor.GRAY + tc.convertSecondsToTime(price) +
                    ChatColor.GREEN + " per day. Type " + ChatColor.GRAY + "/life home rent" + ChatColor.GREEN + " to rent.");
            } else {
                String renter = h.getRenter(rgName);
                if (renter.equalsIgnoreCase(p.getName()))
                    p.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "Welcome home " + p.getName());
                else if (renter.length() > 0)
                    p.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "Welcome to " + renter + "'s" + " home");
            }
            if (cp.getProfession() == TimeProfession.LANDLORD){
                String lord = h.getLandlord(rgName);
                if (lord.length() > 0 && !h.getRenter(rgName).equalsIgnoreCase(p.getName()))
                    p.sendMessage(plugin.getPluginName() + ChatColor.GRAY + "The landlord of this apartment is " + lord);
                else if (lord.length() == 0){
                    double price = h.getPrice(rgName) * 14;
                    p.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "This home may be owned by you for renting out for " + ChatColor.GRAY + tc.convertSecondsToTime(price) +
                            ChatColor.GREEN + ". Type " + ChatColor.GRAY + "/life home buy" + ChatColor.GREEN + " to purchase.");
                }
            }
        }
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
