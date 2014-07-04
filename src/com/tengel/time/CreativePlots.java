/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tengel.time;

import com.mewin.WGRegionEvents.events.RegionEnterEvent;
import com.mewin.WGRegionEvents.events.RegionLeaveEvent;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.tengel.time.serialization.SPlayerInventory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.minecraft.server.v1_7_R3.Material;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Tim
 */
public class CreativePlots  implements Listener{
    HashMap<String,SPlayerInventory> data;
    public CreativePlots(){
        
    }
    
    @EventHandler
    public void onRegionEnter(RegionEnterEvent e){
        ProtectedRegion pr = e.getRegion();
        Player p = e.getPlayer();
        if (pr.getId().contains("cplot_") && pr.isOwner(p.getName())){
            p.setGameMode(GameMode.CREATIVE);
        }
    }
    
    @EventHandler
    public void onRegionLeave(RegionLeaveEvent e){
        ProtectedRegion pr = e.getRegion();
        Player p = e.getPlayer();
        if (p != null && pr != null && pr.getId().contains("cplot_") && pr.isOwner(p.getName())){
            p.setGameMode(GameMode.SURVIVAL);
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Player p = event.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE){
            Time t = (Time) Bukkit.getServer().getPluginManager().getPlugin("Time");
            if (!p.getWorld().getName().equals("Time")){
                Set<String> keys = t.getRegionControl().getRegions(event.getBlock().getLocation()).keySet();
                for (String key : keys){
                    if (key.contains("cplot_"))
                        return;
                }
                p.sendMessage(ChatColor.RED + "You cannot build outside of plot!");
                event.setCancelled(true);
            }
        }
    }
    
    public boolean create(Player p){
        Location loc = p.getLocation();
        return true;
    }
    
    private CoreProtectAPI getCoreProtect() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("CoreProtect");

        // Check that CoreProtect is loaded
        if (plugin == null || !(plugin instanceof CoreProtect))
            return null;

        // Check that the API is enabled
        CoreProtectAPI CoreProtect = ((CoreProtect)plugin).getAPI();
        if (CoreProtect.isEnabled()==false)
            return null;

        // Check that a compatible version of the API is loaded
        if (CoreProtect.APIVersion() < 2)
            return null;

        return CoreProtect;
    }
    
    private boolean isNatural(Location loc){
        CoreProtectAPI cp_plugin = getCoreProtect();
//        Location start = new Location(loc.getWorld(),loc.getBlockX()-5, 0D, loc.getBlockZ()-5);
//        Location end = new Location(loc.getWorld(),loc.getBlockX()+5, 255D, loc.getBlockZ()+5);
        if (cp_plugin == null)
            return false;
        
        return (cp_plugin.performLookup(null, 60*60*60*24*90, 5, loc, null, null).isEmpty());
//        for (int x=start.getBlockX(); x < end.getBlockX(); x++){
//            for (int y=start.getBlockY(); y < end.getBlockY(); y++){
//                for (int z=start.getBlockZ(); z < end.getBlockZ(); z++){
//                    Location loc_temp = new Location(loc.getWorld(), x, y, z);
//                    Block b = p.getWorld().getBlockAt(loc_temp);
//                    if (b.getType().equals(Material.AIR))
//                        continue;
//                    List<String[]> result = cp_plugin.blockLookup(b, 60*60*60*24*90);
//                    if (result.isEmpty())
//                        return false;
//                }
//            }
//        }
    }
}
