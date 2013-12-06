/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.mewin.WGRegionEvents.events.RegionEnterEvent;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.tengel.time.mysql.Homes;
import com.tengel.time.profs.TimeProfession;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;
/**
 *
 * @author Tim
 */
public class RegionControl implements Listener {
    private final Time plugin;
    private final TimePlayers players;
    
    public RegionControl(Time plugin, TimePlayers players) {
            this.plugin = plugin;
            this.players = players;
    }

    @EventHandler
    public void onRegionEnter(RegionEnterEvent e){
        String rgName = e.getRegion().getId();
        Player p = e.getPlayer();
        if (isWrongTimeZone(p.getName(), getZoneId(rgName)))
            p.sendMessage(ChatColor.RED + "You are in a the wrong time zone! Please leave immediately.");
        Homes h = new Homes(plugin);
        if (h.isHome(rgName)){
            ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(p.getName());
            if (h.isAvailable(rgName)){
                double price = h.getRentPrice(rgName);
                p.sendMessage(ChatColor.GREEN + "This home is available for " + ChatColor.GRAY + TimeCommands.convertSecondsToTime(price) +
                    ChatColor.GREEN + " per day. Type " + ChatColor.GRAY + "/life home rent" + ChatColor.GREEN + " to rent.");
            } else {
                String renter = h.getRenter(rgName);
                if (renter.equalsIgnoreCase(p.getName()))
                    p.sendMessage(ChatColor.GREEN + "Welcome home " + ChatColor.GRAY + p.getName());
                else if (renter.length() > 0)
                    p.sendMessage(ChatColor.GREEN + "Welcome to " + ChatColor.GRAY + renter + "'s" + ChatColor.GREEN+ " home");
            }
            if (cp.getProfession() == TimeProfession.LANDLORD){
                String lord = h.getLandlord(rgName);
                if (lord.length() > 0 && !h.getRenter(rgName).equalsIgnoreCase(p.getName())){
                    if (lord.equalsIgnoreCase(p.getName()))
                        p.sendMessage(ChatColor.GRAY + "You own this apartment");
                    else
                        p.sendMessage(ChatColor.GRAY + "The landlord of this apartment is " + ChatColor.GRAY + lord);
                }
                else if (lord.length() == 0){
                    double price = h.getBuyWorth(rgName);
                    p.sendMessage(ChatColor.GREEN + "This may be owned by you for renting out for " + ChatColor.GRAY + TimeCommands.convertSecondsToTime(price) +
                            ChatColor.GREEN + ". Type " + ChatColor.GRAY + "/life home buy" + ChatColor.GREEN + " to purchase.");
                }
            }
        }
    }
    
    /*public void onRegionLeave(RegionLeaveEvent e){
        String rgName = e.getRegion().getId();
        Player player = e.getPlayer();
        if (rgName.equalsIgnoreCase("Poor") || rgName.equalsIgnoreCase("Wealthy") || rgName.equalsIgnoreCase("Rich")){
            if (!checkPermissions(player,"timezone."+rgName.toLowerCase(),false)){
                //ConfigPlayer playerConfig = plugin.getTimePlayers().getPlayerConfig(player.getName());
                //playerConfig.set("wanted_expirey", System.currentTimeMillis()/1000+120*1);
            }
        }
    }*/
    
    private int getZoneId(String timezone){
        if (timezone.equalsIgnoreCase("Rich"))
            return 2;
        else if (timezone.equalsIgnoreCase("Wealthy"))
            return 1;
        else
            return 0;
    }
    
    public int getZoneId(Location loc){
        RegionManager mgr = plugin.worldGuard.getRegionManager(loc.getWorld());
        for (ProtectedRegion rg : mgr.getApplicableRegions(loc)){
            int id = getZoneId(rg.getId());
            if (id > 0)
                return id;
        }
        return 0;
    }
    
    public int getPlayerTimeZone(Player p){
        ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(p.getName());
        return cp.getPlayerTimeZone();
    }
    
    public boolean isWrongTimeZone(String player, int zone){
        ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(player);
        return zone > cp.getPlayerTimeZone();
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
    
    public boolean checkPermissions(Player p, String permission, boolean sendMessage){
        return (plugin.getPlayerListener().checkPermissions(p, permission, sendMessage));
    }
}
