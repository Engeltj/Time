/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.mysql;

import com.sk89q.worldedit.Vector;
import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.tengel.time.Config;
import com.tengel.time.ConfigPlayer;
import com.tengel.time.Time;
import com.tengel.time.WorldGuardUtil;
import com.tengel.time.profs.TimeProfession;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Tim
 */
public class Homes {
    private Time plugin;
    private Config configFile;
    
    public Homes(Time plugin){
        //configFile = new Config(plugin, "homes.yml");
        this.plugin = plugin;
    }
    
    public void create(Player p, String name, String type){
        if ((type==null) || (type.length() == 0) || type.equalsIgnoreCase("apt"))
            type = "apartment";
        else if (type.equalsIgnoreCase("home"))
            type = "house";
        if (!(type.equalsIgnoreCase("house")||type.equalsIgnoreCase("apartment")||type.equalsIgnoreCase("farm"))){
            p.sendMessage(plugin.getPluginName() + ChatColor.RED + "Invalid type, choices: house, apartment, farm");
            return;
        }
        if (isHome(name)){
            p.sendMessage(plugin.getPluginName() + ChatColor.RED + "The home name '"+name+"' is already taken, try again.");
            return;
        }
        WorldGuardUtil wgu = new WorldGuardUtil(plugin, p.getWorld());
        if (!wgu.saveSchematic(p, name)){
            p.sendMessage(ChatColor.RED + "Failed to save schematic for home '"+name+"', aborting home create.");
            return;
        }
        if (wgu.createRegionFromSelection(p, name) == null){
            p.sendMessage(ChatColor.RED + "Failed to create region for home '"+name+"', aborting home create.");
            return;
        }
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int updated = st.executeUpdate("INSERT INTO homes (name, type, x, y, z) VALUES ('"+name+"', '"+type+"', "+p.getLocation().getX()+", "+p.getLocation().getY()+", "+p.getLocation().getZ()+");");
            if (updated > 0)
                p.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "Successfully create home");
            else
                p.sendMessage(plugin.getPluginName() + ChatColor.RED + "Failed to create home, no rows were updated");
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to insert new home '"+name+"', " + ex);
        } 
    }
    
    public void commands(CommandSender sender, String[] args){
        ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(sender.getName());
        Player p = plugin.getServer().getPlayer(sender.getName());
        if (args[1].equalsIgnoreCase("rent")){
            Homes h = new Homes(plugin);
            h.rent(p);
        } else if (args[1].equalsIgnoreCase("buy")){
            Homes h = new Homes(plugin);
            if (cp.getProfession() == TimeProfession.LANDLORD)
                h.buy(p);
            else
                sender.sendMessage(plugin.getPluginName() + ChatColor.RED + "You need to be a landlord to purchase this home");
        } 
    }
    
    public void adminCommands(CommandSender sender, String[] args){
        if (args.length == 2){
            sender.sendMessage(ChatColor.GRAY + "create <name> [type]" + ChatColor.GREEN + "  > Create a new home, defaults to type apartment");
            sender.sendMessage(ChatColor.GRAY + "update <name>" + ChatColor.GREEN + "  > Update the doorway of a home");
            sender.sendMessage(ChatColor.GRAY + "reset <name>" + ChatColor.GREEN + "  > Reset home to factory state");
        } else {
            if (args[2].equalsIgnoreCase("create")){
                if (args.length < 4)
                    sender.sendMessage(ChatColor.RED + "Please specify the name of home");
                else {
                    Homes h = new Homes(plugin);
                    String type = "";
                    try { type = args[4]; } catch (Exception ex){}
                    h.create(plugin.getServer().getPlayer(sender.getName()), args[3], type);
                }

            } else if (args[2].equalsIgnoreCase("update")){
                sender.sendMessage(ChatColor.RED + "Not implemented yet.");
            } else if (args[2].equalsIgnoreCase("reset")){
                String home = "";
                if (args.length >=4)
                    home = args[3];
                if (!resetHome(plugin.getServer().getPlayer(sender.getName()), home)){
                    sender.sendMessage(ChatColor.RED + "Failed to reset home you are standing in or by name");
                }
            }
        }
    }
    
    public Vector getDoor(String home){
        Vector vec = null;
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT x,y,z FROM `homes` WHERE name='"+home+"';");
            if (rs.first()){
                vec = new Vector();
                vec.add(rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
            }
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to get home doorway for '"+home+"', " + ex);
        }
        return vec;
    }
    
    public void resetHome(String home){
        World w = plugin.getServer().getWorld("Time");
        ProtectedRegion pr = plugin.worldGuard.getRegionManager(w).getRegion(home);
        WorldGuardUtil wgu = new WorldGuardUtil(plugin, w);
        wgu.pasteSchematic(pr, home, "homes");
    }
    
    public boolean resetHome(Player p, String home){
        if (home == null)
            home = "";
        RegionManager mgr = plugin.worldGuard.getRegionManager(p.getWorld());
        ProtectedRegion pr = mgr.getRegion(home);
        if (pr == null){
            Vector v = toVector(p.getLocation());
            ApplicableRegionSet set = mgr.getApplicableRegions(v);
            for (ProtectedRegion each : set){
                if (isHome(each.getId())){
                    pr = each;
                }
            }
        }
        if (pr == null){
            plugin.sendConsole("Failed to get home for resetHome with player location or string '"+home+"'");
            return false;
        }
            
        World w = plugin.getServer().getWorld("Time");
        WorldGuardUtil wgu = new WorldGuardUtil(plugin, w);
        return wgu.pasteSchematic(pr, home, "homes");
    }
    
    public boolean isHome(String region){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM `homes` WHERE name='"+region+"';");
            return rs.first();
        } catch (SQLException ex) {
            Logger.getLogger(Homes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public boolean isAvailable(String region){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT renter FROM `homes` WHERE name='"+region+"';");
            if (rs.first()){
                String str = rs.getString("renter");
                if (str == null) return true;
                return (str.length()>0);
            }
        } catch (SQLException ex) {
            plugin.sendConsole("Fail to check availability on house: " + region);
        }
        return false;
    }
    
    private boolean hasLandlord(String region){
        return (getLandlord(region).length() > 0);
    }
    
    public double getPrice(String region){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT price FROM `homes` WHERE name='"+region+"';");
            if (rs.first()){
                double price = rs.getDouble("price");
                if (price > 0) return price;
            }
        } catch (SQLException ex) {
            plugin.sendConsole("Fail to check price on house: " + region);
        }
        return 999999999.0;
    }
    
    public Set getHomes(){
        Set<String> homes = new HashSet<String>();
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM `homes`;");
            while (rs.next()){
                String home = rs.getString("name");
                homes.add(home);
            }
        } catch (SQLException ex) {
            plugin.sendConsole("Fail to get list of homes: ");
        }
        return homes;
    }
    
    public long getLastPay(String region){
        return configFile.getLong(region + ".lastpay");
    }
    
    public String getRenter(String region){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT renter FROM `homes` WHERE name='"+region+"';");
            if (rs.first()){
                String renter = rs.getString("renter");
                if (renter == null)renter="";
                return renter;
            }
        } catch (SQLException ex) {
            plugin.sendConsole("Fail to get renter on home: " + region);
        }
        return "";
    }
    
    public String getLandlord(String region){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT owner FROM `homes` WHERE name='"+region+"';");
            if (rs.first()){
                String owner = rs.getString("owner");
                if (owner == null)owner="";
                return owner;
            }
        } catch (SQLException ex) {
            plugin.sendConsole("Fail to get landlord on home: " + region);
        }
        return "";
    }
    
    public boolean setLandlord(String region, String player){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int updated = st.executeUpdate("UPDATE homes SET owner='"+player+"' WHERE name='"+region+"';");
            return (updated > 0);
        } catch (SQLException ex) {
            plugin.sendConsole("Fail to set landlord on home: " + region);
        }
        return false;
    }
    
    public boolean setRenter(String region, String player){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int updated = st.executeUpdate("UPDATE homes SET renter='"+player+"' WHERE name='"+region+"';");
            st.executeUpdate("UPDATE homes SET lastpay="+String.valueOf(System.currentTimeMillis()/1000)+" WHERE name='"+region+"';");
            resetHome(region);
            return (updated > 0);
        } catch (SQLException ex) {
            plugin.sendConsole("Fail to set renter on home: " + region);
        }
        return false;
    }
    
    public boolean rent(Player p){
        RegionManager mgr = plugin.worldGuard.getRegionManager(p.getWorld());
        Vector v = toVector(p.getLocation());
        ApplicableRegionSet set = mgr.getApplicableRegions(v);
        for (ProtectedRegion each : set){
            String home = each.getId();
            if (isHome(home)){
                if (isAvailable(home)){
                    double price = getPrice(home);
                    EconomyResponse es = plugin.getEconomy().withdrawPlayer(p.getName(), price);
                    if (es.transactionSuccess()){
                        setRenter(home, p.getName());
                        p.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "Congratulations! You are now renting this home");
                        return true;
                    } else 
                        p.sendMessage(plugin.getPluginName() + ChatColor.RED + "You do not have enough time to rent this home!");
                    return false;
                }
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
                    setLandlord(region, p.getName());
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
