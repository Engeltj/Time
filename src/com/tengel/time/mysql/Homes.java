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
import com.tengel.time.TimeCommands;
import com.tengel.time.WorldGuardUtil;
import com.tengel.time.profs.TimeProfession;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
        if (name == null || name.length()==0)
            name = "home_" + String.valueOf(getNextId());
        else
            name = "home_"+name;
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
    
    public int getNextId(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SHOW TABLE STATUS LIKE 'homes';");
            if (rs.first()){
                return rs.getInt("Auto_increment")+1;
            }
        } catch (SQLException ex) {}
        return 0;
    }
    
    public void commands(CommandSender sender, String[] args){
        ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(sender.getName());
        Player p = plugin.getServer().getPlayer(sender.getName());
        if (args[1].equalsIgnoreCase("rent")){
            rent(p);
        } else if (args[1].equalsIgnoreCase("buy")){
            if (cp.getProfession() == TimeProfession.LANDLORD)
                buy(p);
            else
                sender.sendMessage(plugin.getPluginName() + ChatColor.RED + "You need to be a landlord to purchase this home");
        } 
    }
    
    public void adminCommands(CommandSender sender, String[] args){
        if (args.length == 2){
            sender.sendMessage(ChatColor.GRAY + "create <name> [type]" + ChatColor.GREEN + "  > Create a new home, defaults to type apartment");
            sender.sendMessage(ChatColor.GRAY + "update <name>" + ChatColor.GREEN + "  > Update the region of a home");
            sender.sendMessage(ChatColor.GRAY + "reset <name>" + ChatColor.GREEN + "  > Reset home to factory state");
        } else {
            if (args[2].equalsIgnoreCase("create")){
                String name = "";
                String type = "";
                if (args.length >= 4)
                name = args[3];
                try { type = args[4]; } catch (Exception ex){}
                create(plugin.getServer().getPlayer(sender.getName()), name, type);

            } else if (args[2].equalsIgnoreCase("update")){
                if (args.length >= 4)
                    updateHomePrice(args[3]);
                else
                    updateHomePrices();
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
    
    public double getWorth(String home){
        WorldGuardUtil wgu = new WorldGuardUtil(plugin, plugin.getServer().getWorlds().get(0));
        Vector v = wgu.getSchematicDimensions(home, "homes");
        double area = v.getX()*v.getY()*v.getZ();
        return (area + area*5*getZone(home))*120;
    }
    
    public void updateHomePrice(String home){
        double worth = getWorth(home);
        setPrice(home, worth);
    }
    
    public void updateHomePrices(){
        ArrayList<String> s = getHomes();
        for (String home : s){
            updateHomePrice(home);
        }
    }
    
    public int getZone(String home){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT zone FROM `homes` WHERE name='"+home+"';");
            if (rs.first()){
                return rs.getInt("zone");
            }
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to get zone for home '"+home+"', " + ex);
        }
        return 0;
    }
    
    public String getName(String home){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT display_name FROM `homes` WHERE name='"+home+"';");
            if (rs.first()){
                String name = rs.getString("display_name");
                if (name.length()>0)
                    return name;
            }
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to get display_name for home '"+home+" in getName()', " + ex);
        }
        return home;
    }
    
    public void resetHome(String home){
        World w = plugin.getServer().getWorld("Time");
        ProtectedRegion pr = plugin.worldGuard.getRegionManager(w).getRegion(home);
        WorldGuardUtil wgu = new WorldGuardUtil(plugin, w);
        wgu.pasteSchematic(pr, home, "homes");
    }
    
    public String getHome(Location loc){
        RegionManager mgr = plugin.worldGuard.getRegionManager(loc.getWorld());
        Vector v = toVector(loc);
        ApplicableRegionSet set = mgr.getApplicableRegions(v);
        String home = "";
        for (ProtectedRegion each : set){
            if (isHome(each.getId())){
                home = each.getId();
            }
        }
        return home;
    }
    
    public boolean resetHome(Player p, String home){
        if (home == null)
            home = "";
        RegionManager mgr = plugin.worldGuard.getRegionManager(p.getWorld());
        ProtectedRegion pr = mgr.getRegion(home);
        if (pr == null)
            getHome(p.getLocation());
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
                return (str.length()==0);
            }
        } catch (SQLException ex) {
            plugin.sendConsole("Fail to check availability on house: " + region);
        }
        return false;
    }
    
    private boolean hasLandlord(String region){
        return (getLandlord(region).length() > 0);
    }
    
    public double getPrice(String home){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT price FROM `homes` WHERE name='"+home+"';");
            if (rs.first()){
                double price = rs.getDouble("price");
                if (price > 0) return price;
            }
        } catch (SQLException ex) {
            plugin.sendConsole("Fail to check price on home: " + home);
        }
        return 999999999.0;
    }
    
    private boolean setPrice(String home, double price){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int updated = st.executeUpdate("UPDATE `homes` SET price="+price+" WHERE name='"+home+"';");
            return (updated > 0);
        } catch (SQLException ex) {
            plugin.sendConsole("Fail to set price on home: " + home);
        }
        return false;
    }
    
    public void setPrice(Player p, double price){
        String home = getHome(p.getLocation());
        if (home == null || home.length() == 0){
            p.sendMessage(plugin.getPluginName() + ChatColor.RED + "You must stand inside a home first");
            return;
        }
        if (getLandlord(home).equalsIgnoreCase(p.getName()))
            setPrice(home, price);
        else
            p.sendMessage(ChatColor.RED + "You do not own this home");
    }
    
    private boolean setDisplayName(String home, String name){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int updated = st.executeUpdate("UPDATE `homes` SET display_name='"+name+"' WHERE name='"+home+"';");
            return (updated > 0);
        } catch (SQLException ex) {
            plugin.sendConsole("Fail to set display_name on home: " + home);
        }
        return false;
    }
    
    public void setDisplayName(Player p, String name){
        String home = getHome(p.getLocation());
        if (home == null || home.length() == 0){
            p.sendMessage(plugin.getPluginName() + ChatColor.RED + "You must stand inside a home first");
            return;
        }
        if (getLandlord(home).equalsIgnoreCase(p.getName()))
            setDisplayName(home, name);
        else
            p.sendMessage(ChatColor.RED + "You do not own this home");
    }
    
    public ArrayList<String> getHomes(){
        ArrayList<String> homes = new ArrayList<String>();
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
            if (player.length()==0){
                resetHome(region);
            }
            return (updated > 0);
        } catch (SQLException ex) {
            plugin.sendConsole("Fail to set renter on home: " + region);
        }
        return false;
    }
    
    public void rent(Player p){
        String home = getHome(p.getLocation());
        if (isHome(home)){
            if (isAvailable(home)){
                double price = getPrice(home);
                EconomyResponse es = plugin.getEconomy().withdrawPlayer(p.getName(), price);
                if (es.transactionSuccess()){
                    setRenter(home, p.getName());
                    p.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "Congratulations! You are now renting this home");
                } else 
                    p.sendMessage(plugin.getPluginName() + ChatColor.RED + "You do not have enough time to rent this home!");
            }
        }
        p.sendMessage(plugin.getPluginName() + ChatColor.RED + "You must stand inside a home first");
    }
    
    public void buy(Player p){
        String home = getHome(p.getLocation());
        boolean hasOwner = hasLandlord(home);
        if (isHome(home) && !hasOwner){
            double price = getPrice(home) * 14;
            EconomyResponse es = plugin.getEconomy().withdrawPlayer(p.getName(), price);
            if (es.transactionSuccess()){
                setLandlord(home, p.getName());
                p.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "Congratulations! You now own this home");
            } else 
                p.sendMessage(plugin.getPluginName() + ChatColor.RED + "You do not have enough time to buy this home!");
        } else if (hasOwner){
            String landlord = getLandlord(home);
            if (landlord.equalsIgnoreCase(p.getName()))
                p.sendMessage(plugin.getPluginName() + ChatColor.RED + "You already own this home");
            else
                p.sendMessage(plugin.getPluginName() + ChatColor.RED + "It appears this home already owned by "+ChatColor.GRAY + landlord);
        } else 
            p.sendMessage(plugin.getPluginName() + ChatColor.RED + "You must stand inside a home first"); 
    }
    
    public boolean sell(Player p, Player newOwner, double price){
        String home = getHome(p.getLocation());
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int updated = st.executeUpdate("INSERT INTO `homes_offer` (name, seller, buyer, price, expiry) VALUES ("+
                    "'"+home+"','"+p.getName()+"','"+newOwner.getName()+"',"+price+","+(System.currentTimeMillis()/1000 + 2*60*60)+");");
            if (newOwner.isOnline()){
                newOwner.sendMessage(p.getName() + ChatColor.GREEN + " has offered you the home " + ChatColor.GRAY + getName(home) + ChatColor.GREEN + " for " + 
                        ChatColor.RED + TimeCommands.convertSecondsToTime(price) + ChatColor.GREEN + ". Type "+ ChatColor.GRAY+"/life job accept"+ ChatColor.GREEN+" to purchase");
            }
            return (updated > 0);
        } catch (SQLException ex) {
            plugin.sendConsole("Fail to offer home '"+home+"' from '"+p.getName()+"' to '"+newOwner.getName()+"'");
        }
        return false;
    }
    
    public void disown(Player p){
        String home = getHome(p.getLocation());
        String landlord = getLandlord(home);
        if (landlord.equalsIgnoreCase(p.getName())){
            if (setLandlord(home, ""))
                p.sendMessage(ChatColor.GREEN + "You now no longer own this home!");
        } else
            p.sendMessage(ChatColor.RED + "You do not own this home");
    }
}
