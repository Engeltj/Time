/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.mewin.WGRegionEvents.events.RegionEnterEvent;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.tengel.time.mysql.Homes;
import com.tengel.time.profs.TimeProfession;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.tengel.time.structures.TimePlayer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.World;
/**
 *
 * @author Tim
 */
public class RegionControl implements Listener {
    private final Time plugin;
    
    public RegionControl(Time plugin) {
            this.plugin = plugin;
    }

    @EventHandler
    public void onRegionEnter(RegionEnterEvent e){
        String rgName = e.getRegion().getId();
        Player p = e.getPlayer();
        if (isWrongZone(p.getName(), getZoneId(rgName)))
            p.sendMessage(ChatColor.RED + "You are in a the wrong time zone! Please leave immediately.");
        Homes h = new Homes(plugin);
        if (h.isHome(rgName)){
            TimePlayer tp = plugin.getPlayer(p.getName());
            if (h.isAvailable(rgName)){
                double price = h.getRentPrice(rgName);
                p.sendMessage(ChatColor.GREEN + "This home is available for " + ChatColor.GRAY + Commands.convertSecondsToTime(price) +
                    ChatColor.GREEN + " per day. Type " + ChatColor.GRAY + "/life home rent" + ChatColor.GREEN + " to rent.");
            } else {
                String renter = h.getRenter(rgName);
                if (renter.equalsIgnoreCase(p.getName()))
                    p.sendMessage(ChatColor.GREEN + "Welcome home " + ChatColor.GRAY + p.getName());
                else if (renter.length() > 0)
                    p.sendMessage(ChatColor.GREEN + "Welcome to " + ChatColor.GRAY + renter + "'s" + ChatColor.GREEN+ " home");
            }
            if (tp.hasJob(TimeProfession.LANDLORD)){
                String lord = h.getLandlord(rgName);
                if (lord.length() > 0 && !h.getRenter(rgName).equalsIgnoreCase(p.getName())){
                    if (lord.equalsIgnoreCase(p.getName()))
                        p.sendMessage(ChatColor.GRAY + "You own this apartment");
                    else
                        p.sendMessage(ChatColor.GRAY + "The landlord of this apartment is " + ChatColor.GRAY + lord);
                }
                else if (lord.length() == 0){
                    double price = h.getBuyWorth(rgName);
                    p.sendMessage(ChatColor.GREEN + "This may be owned by you for renting out for " + ChatColor.GRAY + Commands.convertSecondsToTime(price) +
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
        TimePlayer tp = plugin.getPlayer(p.getName());
        return tp.getZone();
    }
    
    public boolean isWrongZone(String player, int zone){
        TimePlayer tp = plugin.getPlayer(player);
        return zone > tp.getZone();
    }
    
    public boolean isWrongZone(Player player){
        return (isWrongZone(player.getName(), getZoneId(player.getLocation())));
    }
    
    public boolean checkPermissions(Player p, String permission, boolean sendMessage){
        return (plugin.getPlayerListener().checkPermissions(p, permission, sendMessage));
    }
    
    public Map<String, ProtectedRegion> getRegions(Location loc){
        RegionManager mgr = plugin.worldGuard.getRegionManager(loc.getWorld());
        HashMap<String, ProtectedRegion> map = new HashMap<String, ProtectedRegion>();
        for (ProtectedRegion rg : mgr.getApplicableRegions(loc))
            map.put(rg.getId(), rg);
        return map;
    }
    
    public Map<String, ProtectedRegion> getRegionsByOwner(String owner){
        HashMap<String, ProtectedRegion> map = new HashMap<String, ProtectedRegion>();
        for (World w : plugin.getServer().getWorlds()){
            RegionManager mgr = plugin.worldGuard.getRegionManager(w);
            Iterator it = mgr.getRegions().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                ProtectedRegion rg = (ProtectedRegion) pairs.getValue();
                for (String key : rg.getOwners().getPlayers()){
                    if (key.equalsIgnoreCase(owner))
                        map.put(rg.getId(), rg);  
                }
                    
            }
        }
        return map;
    }
    
    public ProtectedRegion createRegion(String name, Location start, Location end){
        World w = start.getWorld();
        WorldGuardUtil wgu = new WorldGuardUtil(plugin, w);
        try {
           return wgu.createRegion(name, start, end);
        } catch (Exception ex) {
           Logger.getLogger(RegionControl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public ProtectedRegion createRegion(String name, Location loc, int length, int width, int height){
        World w = loc.getWorld();
        Location start,end;
        start = new Location(w,loc.getX()-length/2.0, loc.getY()-height/2.0, loc.getZ()-width/2.0);
        end = new Location(w,loc.getX()+length/2.0, loc.getY()+height/2.0, loc.getZ()+width/2.0);
        
        return createRegion(name,start, end);
    }
    
    public ProtectedRegion createRegion(String name, Location loc, int radius){
        return createRegion(name, loc, radius*2, radius*2, radius*2);
    }
    
    public ProtectedRegion createRegion(String name, Location loc, int h_radius, int v_radius){
        return createRegion(name, loc, h_radius*2, h_radius*2, v_radius*2);
    }
    
    public ProtectedRegion createRegionVert(String name, Location loc, int length, int width){
        World w = loc.getWorld();
        Location start,end;
        start = new Location(w,loc.getX()-length/2.0, 0L, loc.getZ()-width/2.0);
        end = new Location(w,loc.getX()+length/2.0, 255L, loc.getZ()+width/2.0);
        
        return createRegion(name,start, end);
    }
    
    public void removeRegion(String region, World w){
        WorldGuardUtil wgu = new WorldGuardUtil(plugin, w);
        wgu.deleteRegion(region);
    }
    
    public void saveRegions(World w){
        try {
            plugin.worldGuard.getRegionManager(w).save();
        } catch (ProtectionDatabaseException ex) {
            Logger.getLogger(RegionControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addRegionOwner(String owner, ProtectedRegion pr){
        DefaultDomain dd = pr.getOwners();
        dd.addPlayer(owner);
        pr.setOwners(dd);
    }
}
