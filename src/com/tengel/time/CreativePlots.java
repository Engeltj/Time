/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tengel.time;

import com.mewin.WGRegionEvents.events.RegionEnterEvent;
import com.mewin.WGRegionEvents.events.RegionLeaveEvent;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.coreprotect.CoreProtectAPI.ParseResult;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Tim
 */
public class CreativePlots  implements Listener{
    HashMap<String,TimePlayerInventory> data;
    private final Time plugin;
    public CreativePlots(Time plugin){
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onRegionEnter(RegionEnterEvent e){
        ProtectedRegion pr = e.getRegion();
        Player p = e.getPlayer();
        if (pr.getId().contains("cplot_")  && !p.getGameMode().equals(GameMode.CREATIVE)){ // && pr.isOwner(p.getName())
            p.setGameMode(GameMode.CREATIVE);
        }
    }
    
    @EventHandler
    public void onRegionLeave(RegionLeaveEvent e){
        ProtectedRegion pr = e.getRegion();
        Player p = e.getPlayer();
        if (p != null && pr != null && pr.getId().contains("cplot_") && p.getGameMode().equals(GameMode.CREATIVE)){
            p.setGameMode(GameMode.SURVIVAL);
        }
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        Player p = event.getPlayer();
        Block b = event.getClickedBlock();
        if (b != null && (p.getGameMode() == GameMode.SURVIVAL) && !p.isOp()){
            Set<String> keys = plugin.getRegionControl().getRegions(b.getLocation()).keySet();
            for (String key : keys){
                if (key.contains("cplot_")){
                    p.sendMessage(ChatColor.RED + "You may not interact with blocks outside of plot");
                    event.setCancelled(true);
                }

            }
        } else if ((p.getGameMode() == GameMode.CREATIVE) && !p.isOp()){
            ItemStack is = event.getItem();
            if (is != null && (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))){
                Material mat = is.getType();
                if (mat.equals(Material.EGG) || mat.equals(Material.MONSTER_EGG) || mat.equals(Material.MONSTER_EGGS)){
                    p.sendMessage(ChatColor.RED + mat.toString() + " is not allowed");
                    event.setCancelled(true);
                    return;
                }

            }
            if (b != null){
                boolean plot = false;
                Set<String> keys = plugin.getRegionControl().getRegions(b.getLocation()).keySet();
                for (String key : keys){
                    if (key.contains("cplot_"))
                        plot = true;
                }
                if (!plot){
                    p.sendMessage(ChatColor.RED + "You may not interact with blocks outside of plot");
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler
    public void onDispense(BlockDispenseEvent event){
        Block b = event.getBlock();
        Set<String> keys = plugin.getRegionControl().getRegions(b.getLocation()).keySet();
        for (String key : keys){
            if (key.contains("cplot_"))
                event.setCancelled(true);
        }
    }
    
    
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Player p = event.getPlayer();
        if ((p.getGameMode() == GameMode.CREATIVE) && !p.isOp()){
            Block b = event.getBlock();
            Material mat = b.getType();
            if (mat.equals(Material.TNT) || mat.equals(Material.MOB_SPAWNER)){
                p.sendMessage(ChatColor.RED + mat.toString() + " is not allowed");
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
    
    private String getUnusedPlotName(World w){
        Map<String, ProtectedRegion> map = plugin.worldGuard.getRegionManager(w).getRegions();
        int i = 0;
        for (String key : map.keySet()){
            if (key.contains("cplot_")){
                String [] t = key.split("_");
                try {
                    System.out.println(t[1]);
                    int plot_num = Integer.parseInt(t[1]);
                    if (plot_num > i)
                        i = plot_num;
                    
                }catch(Exception ignored){}
            }
        }
        return "cplot_".concat(plugin.intToString(i+1, 7));
    }
    
    private ProtectedRegion createPlot(Location loc){
        return plugin.getRegionControl().createRegionVert(getUnusedPlotName(loc.getWorld()), loc, 10, 10);
    }
    
    private boolean checkLocation(Location loc){
        Location temp = new Location(loc.getWorld(),loc.getX(), loc.getY(), loc.getZ());
        for (int y=0;y<255;y++){
            temp.setY(y);
            if (!plugin.getRegionControl().getRegions(temp).isEmpty())
                return false;
        }
        temp.setY(64.0);
        temp.setX(temp.getX()-5);
        if (!plugin.getRegionControl().getRegions(temp).isEmpty())
            return false;
        temp.setZ(temp.getZ()-5);
        if (!plugin.getRegionControl().getRegions(temp).isEmpty())
            return false;
        temp.setX(temp.getX()+10);
        if (!plugin.getRegionControl().getRegions(temp).isEmpty())
            return false;
        temp.setZ(temp.getZ()+10);
        if (!plugin.getRegionControl().getRegions(temp).isEmpty())
            return false;
        return true;
    }
    
    private void savePlotSchematic(Player p, ProtectedRegion pr){
        WorldGuardUtil wgu = new WorldGuardUtil(plugin, null);
        wgu.saveSchematic(p, pr, "cplots"+File.separator+p.getWorld().getName(), pr.getId());
    }
    
    private void restorePlotSchematic(Player p, ProtectedRegion pr){
        World w = p.getWorld();
        WorldGuardUtil wgu = new WorldGuardUtil(plugin, w);
        wgu.pasteSchematic(pr, pr.getId(), "cplots" + File.separator + w.getName());
    }
    
    public boolean create(Player p){
        int count = getAmountOwned(p.getName());
        System.out.println(count);
        if (count == 0){
            Location loc = p.getLocation();
            if (!checkLocation(loc)){
                p.sendMessage(ChatColor.RED + "It seems this overlaps another region, try again elsewhere");
                return false;
            }
            if (isQualified(p.getName(), loc)){
                ProtectedRegion pr = createPlot(loc);                
                if (pr != null){
                    plugin.getRegionControl().addRegionOwner(p.getName(),pr);
                    plugin.getRegionControl().saveRegions(p.getWorld());
                    savePlotSchematic(p, pr);
                    p.sendMessage(ChatColor.GREEN + "Plot created!");
                } else
                    p.sendMessage(ChatColor.RED + "Plot creation failed, please speak with an admin");
                    
            } else
                 p.sendMessage(ChatColor.RED + "This area seems to have been modified by another player in the last 60 days. Cannot make plot here");
        } else {
            p.sendMessage(ChatColor.RED + "You may only own up to 1 plot maximum at this time");
            return false;
        }
        
        return true;
    }
    
    public void destroy(Player p){
        Map<String, ProtectedRegion> map = plugin.getRegionControl().getRegions(p.getLocation());
        for (String key : map.keySet()){
            if (key.contains("cplot_")){
                for (String player: map.get(key).getOwners().getPlayers()){
                    if (player.equalsIgnoreCase(p.getName())){
                        ProtectedRegion pr = map.get(key);
                        restorePlotSchematic(p, pr);
                        p.setGameMode(GameMode.SURVIVAL);
                        plugin.getRegionControl().removeRegion(pr.getId(), p.getWorld());
                        p.sendMessage(ChatColor.GREEN + "Plot destroyed!");
                        return;
                    }
                }
            }
        }
        p.sendMessage(ChatColor.RED + "You do not own a plot at this location");
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
        List<Integer> exclude = new ArrayList<Integer>(){{
            add(Material.DIRT.getId());
            add(Material.SAND.getId());
            add(Material.GRAVEL.getId());
            add(Material.GRASS.getId());
            add(Material.STONE.getId());
            
        }};
        CoreProtectAPI cp_plugin = getCoreProtect();
        if (cp_plugin == null)
            return false;
        List<String[]> data = new ArrayList<String[]>();
        for (int i=0;i<255;i=i+5){
            loc.setY(i);
            data.addAll(cp_plugin.performLookup(null, 60*60*60*24*60, 5, loc, null, exclude));
        }
        
        for (String[] value: data){
            ParseResult result = cp_plugin.parseResult(value);
            String temp_player = result.getPlayer();
            if (!temp_player.equalsIgnoreCase(player)){
                if (plugin.getServer().getPlayer(temp_player) != null)
                    return false;
            }
        }
        return true;
    }
}
