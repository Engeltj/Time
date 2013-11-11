/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.homes;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.tengel.time.Config;
import com.tengel.time.Time;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 *
 * @author Tim
 */
public class Homes {
    private Time plugin;
    private Config configFile;
    
    public Homes(Time plugin){
        configFile = new Config(plugin, "homes.yml");
        this.plugin = plugin;
    }
    
    //UNDER DEVELOPMENT
    private boolean saveSchematic(Player p, String region) throws CommandException{
        WorldEditPlugin worldEdit = plugin.worldGuard.inst().getWorldEdit();
        ProtectedRegion  rg = plugin.worldGuard.getRegionManager(p.getWorld()).getRegion(region);
        final LocalSession session = worldEdit.getSession(p);
        final BukkitPlayer lPlayer = worldEdit.wrapPlayer(p);
        EditSession editSession = session.createEditSession(lPlayer);
        
        File folder = new File(plugin.getDataFolder(), "homes");
        if (!folder.exists())
                folder.mkdirs();
        if (rg instanceof ProtectedCuboidRegion) {
            ProtectedCuboidRegion cuboid = (ProtectedCuboidRegion) rg;
            Vector pt1 = cuboid.getMinimumPoint();
            Vector pt2 = cuboid.getMaximumPoint();
            CuboidSelection selection = new CuboidSelection(p.getWorld(), pt1, pt2);
            //CuboidClipboard cc = new CuboidClipboard(selection);
            //worldEdit.setSelection(p, selection);
        } else if (rg instanceof ProtectedPolygonalRegion) {
            ProtectedPolygonalRegion poly2d = (ProtectedPolygonalRegion) rg;
            Polygonal2DSelection selection = new Polygonal2DSelection(
                    p.getWorld(), poly2d.getPoints(),
                    poly2d.getMinimumPoint().getBlockY(),
                    poly2d.getMaximumPoint().getBlockY() );
            //worldEdit.setSelection(p, selection);
        } else {
            return false;
        }
        
        //plugin.worldGuard.
        return true;
    }
    
    public void create(Player p, String region, double price, boolean farm){
        //ProtectedRegion rg = plugin.worldGuard.getRegionManager(p.getWorld()).getRegion(region);
        if (!plugin.worldGuard.getRegionManager(p.getWorld()).hasRegion(region)){
            p.sendMessage(plugin.getPluginName() + ChatColor.RED + "Cannot create home, region '" + region + "' does not exist on this world!");
            return;
        }
        ConfigurationSection section = configFile.getConfigurationSection(region);
        if (section == null)
            section = configFile.createSection(region);
        section.set("price", price);
        if (farm)
            section.set("farm", farm);
        configFile.save();
        p.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "Successfully create home");
    }
    
    public boolean isHome(String region){
        ConfigurationSection section = configFile.getConfigurationSection(region);
        return (section != null);
    }
    
    public boolean isAvailable(String home){
        if (!isHome(home))
            return false;
        String renter = configFile.getString(home + ".renter");
        if ((renter == null) || (renter.length() == 0))
            return true;
        return false;
    }
    
    private boolean hasLandlord(String home){
        String renter = configFile.getString(home + ".landlord");
        if ((renter == null) || (renter.length() == 0))
            return false;
        return true;
    }
    
    public double getPrice(String home){
        double price = configFile.getDouble(home + ".price");
        return price;
    }
    
    public Config getConfig(){
        return this.configFile;
    }
    
    public Set getHomes(){
        Set<String> homes = configFile.getKeys(false);
        return homes;
    }
    
    public long getLastPay(String home){
        return configFile.getLong(home + ".lastpay");
    }
    
    public String getRenter(String home){
        String renter = configFile.getString(home + ".renter");
        if (renter == null)
            return "";
        return renter;
    }
    
    public String getLandlord(String home){
        String lord = configFile.getString(home + ".landlord");
        if (lord == null)
            return "";
        return lord;
    }
    
    public void setLandlord(String home, Player p){
        configFile.set(home + ".landlord", p.getName());
        configFile.save();
    }
    
    private void setRenter(String home, Player p){
        configFile.set(home + ".renter", p.getName());
        configFile.set(home + ".lastpay", System.currentTimeMillis()/1000);
        configFile.save();
    }
    
    public void removeRenter(String home){
        configFile.set(home + ".renter", "");
        configFile.set(home + ".lastpay", 0L);
        configFile.save();
    }
    
    public boolean rent(Player p){
        Map<String, ProtectedRegion> regions = plugin.worldGuard.getRegionManager(p.getWorld()).getRegions();
        for (ProtectedRegion rg : regions.values()){
            String region = rg.getId();
            if (isAvailable(region)){
                double price = getPrice(region);
                EconomyResponse es = plugin.getEconomy().withdrawPlayer(p.getName(), price);
                if (es.transactionSuccess()){
                    setRenter(region, p);
                    p.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "Congratulations! You are now renting this home");
                    return true;
                } else 
                    p.sendMessage(plugin.getPluginName() + ChatColor.RED + "You do not have enough time to rent this home!");
                return false;
            }
        }
        p.sendMessage(plugin.getPluginName() + ChatColor.RED + "You must stand inside a home first");
        return false;
    }
    
    public boolean buy(Player p){
        Map<String, ProtectedRegion> regions = plugin.worldGuard.getRegionManager(p.getWorld()).getRegions();
        for (ProtectedRegion rg : regions.values()){
            String region = rg.getId();
            if (isHome(region) && !hasLandlord(region)){
                double price = getPrice(region) * 14;
                EconomyResponse es = plugin.getEconomy().withdrawPlayer(p.getName(), price);
                if (es.transactionSuccess()){
                    setLandlord(region, p);
                    p.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "Congratulations! You now own this home");
                    return true;
                } else 
                    p.sendMessage(plugin.getPluginName() + ChatColor.RED + "You do not have enough time to buy this home!");
                return false;
            } else if (hasLandlord(region)){
                p.sendMessage(plugin.getPluginName() + ChatColor.RED + "It appears this home already has a landlord");
                return false;
            }
                    
        }
        p.sendMessage(plugin.getPluginName() + ChatColor.RED + "You must stand inside a home first");
        return false;
    }
}
