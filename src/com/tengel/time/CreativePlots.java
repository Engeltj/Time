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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.coreprotect.CoreProtectAPI.ParseResult;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Tim
 */
public class CreativePlots  implements Listener{
    HashMap<String,SPlayerInventory> data;
    private final Time plugin;
    public CreativePlots(Time plugin){
        this.plugin = plugin;
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
        if ((p.getGameMode() == GameMode.CREATIVE) && !p.isOp()){
            Block b = event.getBlock();
            if (b.getType().equals(Material.TNT)){
                p.sendMessage(ChatColor.RED + "TNT is not allowed in creative");
                event.setCancelled(true);
            }
                
            if (!p.getWorld().getName().equals("Time")){
                Set<String> keys = plugin.getRegionControl().getRegions(b.getLocation()).keySet();
                for (String key : keys){
                    if (key.contains("cplot_"))
                        return;
                }
                p.sendMessage(ChatColor.RED + "You cannot build outside of plot!");
                event.setCancelled(true);
            }
        } else if ((p.getGameMode() == GameMode.SURVIVAL) && !p.isOp()){
            if (!p.getWorld().getName().equals("Time")){
                Set<String> keys = plugin.getRegionControl().getRegions(event.getBlock().getLocation()).keySet();
                for (String key : keys){
                    if (key.contains("cplot_")){
                        p.sendMessage(ChatColor.RED + "Please enter plot area first");
                        event.setCancelled(true);
                    }
                        
                }
            }
        }
    }
    
    @EventHandler
    public void onBlockRemove(BlockBreakEvent event){
        Player p = event.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE){
            Block b = event.getBlock();
            if (b.getLocation().getBlockY() < 5 && b.getType().equals(Material.BEDROCK)){
                p.sendMessage(ChatColor.RED + "You may not break bedrock at this depth");
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event){
        Player p = event.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE){
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBlockDamage(BlockDamageEvent event){
        Player p = event.getPlayer();
        Set<String> keys = plugin.getRegionControl().getRegions(event.getBlock().getLocation()).keySet();
        for (String key : keys){
            if (key.contains("cplot_")){
                p.sendMessage(ChatColor.RED + "You may not mine a creative plot");
                event.setCancelled(true);
                return;
            }
        }
    }
    
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event){
        List<Block> blocks = event.blockList();
        Iterator<Block> i = blocks.iterator();
        while (i.hasNext()) {
            Block b = i.next();
            Set<String> keys = plugin.getRegionControl().getRegions(b.getLocation()).keySet();
            for (String key : keys){
                if (key.contains("cplot_")){
                    i.remove();
                    break;
                }
            }
        }
    }
    
    public boolean create(Player p){
        int count = getAmountOwned(p.getName());
        if (count == 0){
            Location loc = p.getLocation();
            if (isQualified(p.getName(), loc)){
                p.sendMessage("You seem to qualify!");
            } else
                 p.sendMessage(ChatColor.RED + "You don't qualify!");
        }
        
        return true;
    }
    
    private int getAmountOwned(String player){
        int count = 0;
        Set<String> keys = plugin.getRegionControl().getRegionsByOwner(player).keySet();
        for (String key : keys){
            if (key.contains("cplot_"))
                count++;
        }
       return count;
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
    
    private boolean isQualified(String player, Location loc){
        CoreProtectAPI cp_plugin = getCoreProtect();
        if (cp_plugin == null)
            return false;
        List<String[]> data = cp_plugin.performLookup(null, 60*60*60*24*60, 5, loc, null, null);
        
        for (String[] value: data){
            ParseResult result = cp_plugin.parseResult(value);
            String temp_player = result.getPlayer();
            System.out.println(temp_player);
            if (!temp_player.equalsIgnoreCase(player)){
                if (plugin.getServer().getPlayer(temp_player) != null)
                    return false;
            }
        }
        return true;
    }
}
