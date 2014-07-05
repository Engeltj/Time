/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.mysql;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.tengel.time.*;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

/**
 *
 * @author Tim
 */
public class Homes {
    private final Time plugin;
    
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
            p.sendMessage(ChatColor.RED + "Invalid type, choices: house, apartment, farm");
            return;
        }
        if (isHome(name)){
            p.sendMessage(ChatColor.RED + "The home name '"+name+"' is already taken, try again.");
            return;
        }
        WorldGuardUtil wgu = new WorldGuardUtil(plugin, p.getWorld());
        if (!wgu.saveSchematic(p, "homes", name)){
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
                p.sendMessage(ChatColor.GREEN + "Successfully create home");
            else
                p.sendMessage(ChatColor.RED + "Failed to create home, no rows were updated");
        } catch (Exception ex) {
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
                return rs.getInt("Auto_increment");
            }
        } catch (SQLException ignored) {}
        return 0;
    }
    
    
    
    /*
        Returns new price set for a home
    */
    public double getNewRentPrice(String home){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT new_price FROM `homes` WHERE name='"+home+"';");
            if (rs.first())
                return rs.getDouble("new_price");
        } catch (Exception ex) {
            plugin.sendConsole("Failed to get new_price for home '"+home+"', " + ex);
        }
        return 0D;
    }
    
    /*
        Returns epoch date when the home price was changed
    */
    public double getRentPriceChanged(String home){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT price_changed FROM `homes` WHERE name='"+home+"';");
            if (rs.first())
                return rs.getDouble("price_changed");
        } catch (Exception ex) {
            plugin.sendConsole("Failed to get new_price for home '"+home+"', " + ex);
        }
        return 0D;
    }
    
    public Vector getDoor(String home){
        Vector vec = null;
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT x,y,z FROM `homes` WHERE name='"+home+"';");
            if (rs.first()){
                vec = new Vector(rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"));
            }
        } catch (Exception ex) {
            plugin.sendConsole("Failed to get home doorway for '"+home+"', " + ex);
        }
        return vec;
    }
    
    public boolean setDoor(String home, double x, double y, double z){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int updated = st.executeUpdate("UPDATE `homes` SET x="+x+",y="+y+",z="+z+" WHERE name='"+home+"';");
            return (updated>0);
        } catch (Exception ex) {
            plugin.sendConsole("Failed to set home doorway for '"+home+"', " + ex);
        }
        return false;
    }
    
    public double getBuyWorth(String home){
        return getRentWorth(home)*30;
    }
    
    public double getRentWorth(String home){
        WorldGuardUtil wgu = new WorldGuardUtil(plugin, plugin.getServer().getWorlds().get(0));
        Vector v = wgu.getSchematicDimensions(home, "homes");
        double area = v.getX()*v.getY()*v.getZ();
        return (area + area*5*getZone(home))*120;
    }
    
    /* 
        Updates rent price of home based on landlords new_price or size (if no landlord or renter)
    */
    public void updateHomeRentPrice(String home){
        String lord = getLandlord(home);
        if (!(lord.length()>0 || getRenter(home).length()>0)){
            double worth = getRentWorth(home);
            setRentPrice(home, worth);
        } else if (lord.length() > 0){
            double new_price = getNewRentPrice(home);
            String renter = this.getRenter(home);
            if (new_price > 0){
                String home_name = this.getName(home);
                double time = System.currentTimeMillis()/1000 - getRentPriceChanged(home);
                Player p = plugin.getServer().getPlayer(renter);
                if (time > 14*24*60*60 || p == null){
                    setRentPrice(home, new_price);
                    if (p != null && p.isOnline()){
                        
                        p.sendMessage(ChatColor.GREEN + "Your home '"+ChatColor.GRAY+home_name+ChatColor.GREEN+"' rent price has changed to "+ChatColor.GRAY+TimeCommands.convertSecondsToTime(new_price)+
                                ChatColor.GREEN+" per day.");
                    }
                } else if (p.isOnline()){
                    long last_warned = System.currentTimeMillis()/1000 - getLastWarnedNewRentPrice(home);
                    if (last_warned > 24*60*60){
                        p.sendMessage(ChatColor.GREEN + "Your home '"+ChatColor.GRAY+home_name+ChatColor.GREEN+"' rent price will change to "+ChatColor.GRAY+TimeCommands.convertSecondsToTime(new_price)+
                                ChatColor.GREEN+" per day in " + ChatColor.GRAY+TimeCommands.convertSecondsToTime(time));
                    }
                }
            }
        }
    }
    
    private long getLastWarnedNewRentPrice(String home){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT price_warned FROM `homes` WHERE name='"+home+"';");
            if (rs.first()){
                return rs.getLong("price_warned");
            }
        } catch (Exception ex) {
            plugin.sendConsole("Failed to get price_warned for home '"+home+"', " + ex);
        }
        return 0;
    }
    
    private boolean updateWarnedUserNewRentPrice(String home){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int updated = st.executeUpdate("UPDATE `homes` SET price_warned="+System.currentTimeMillis()/1000+" WHERE name='"+home+"';");
            return (updated >0);
        } catch (Exception ex) {
            plugin.sendConsole("Failed to update 'price_warned' for home '"+home+"', " + ex);
        }
        return false;
    }
    
    public void updateHomeRentPricesWithLandlords(){
        ArrayList<String> s = getHomesWithLords();
        for (String home : s){
            updateHomeRentPrice(home);
        }
    }
    
    /*
        Updates all home rent prices, pretty intensive tasks when there are alot of homes. 
        ADMIN USE ONLY!!!! Should not be run unless entirely nessessary
    */
    public void updateHomeRentPrices(){
        ArrayList<String> s = getHomes();
        for (String home : s){
            updateHomeRentPrice(home);
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
        } catch (Exception ex) {
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
        } catch (Exception ex) {
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
        } catch (Exception ex) {
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
                return str == null || (str.length() == 0);
            }
        } catch (Exception ex) {
            plugin.sendConsole("Fail to check availability on house: " + region);
        }
        return false;
    }
    
    private boolean hasLandlord(String region){
        return (getLandlord(region).length() > 0);
    }
    
    public double getRentPrice(String home){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT price FROM `homes` WHERE name='"+home+"';");
            if (rs.first()){
                double price = rs.getDouble("price");
                if (price > 0) return price;
            }
        } catch (Exception ex) {
            plugin.sendConsole("Fail to check price on home: " + home);
        }
        return 999999999.0;
    }
    
    private boolean setRentPrice(String home, double price){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int updated = st.executeUpdate("UPDATE `homes` SET price="+price+", new_price=0, price_changed=0, price_warned=0 WHERE name='"+home+"';");
            return (updated > 0);
        } catch (Exception ex) {
            plugin.sendConsole("Fail to set price on home: " + home);
        }
        return false;
    }
    
    public void setPrice(Player p, double price){
        String home = getHome(p.getLocation());
        if (home == null || home.length() == 0){
            p.sendMessage(ChatColor.RED + "You must stand inside a home first");
            return;
        }
        if (getLandlord(home).equalsIgnoreCase(p.getName()))
            setRentPrice(home, price);
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
        } catch (Exception ex) {
            plugin.sendConsole("Fail to set display_name on home: " + home);
        }
        return false;
    }
    
    public boolean hasDisplayName(String name){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM `homes` WHERE display_name='"+name+"';");
            return (rs.first());
        } catch (Exception ex) {
            plugin.sendConsole("Fail to get display_name matching: " + name);
        }
        return false;
    }
    
    public void setDisplayName(Player p, String name){
        String home = getHome(p.getLocation());
        if (home == null || home.length() == 0){
            p.sendMessage(ChatColor.RED + "You must stand inside a home first");
            return;
        }
        if (getLandlord(home).equalsIgnoreCase(p.getName())){
            setDisplayName(home, name);
            p.sendMessage(ChatColor.GREEN + "Home's name was changed to "+ ChatColor.GRAY + name);
        }
        else
            p.sendMessage(ChatColor.RED + "You do not own this home");
    }
    
    public ArrayList<String> getRentedHomes(String player){
        ArrayList<String> homes = new ArrayList<String>();
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM `homes` WHERE renter='"+player+"';");
            if (rs.first()){
                String name = rs.getString("name");
                homes.add(name);
            }
        } catch (Exception ex) {
            plugin.sendConsole("Fail to get list of homes for "+player+", " + ex);
        }
        return homes;
    }
    
    public ArrayList<String> getHomesWithLords(){
        ArrayList<String> homes = new ArrayList<String>();
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM `homes` WHERE owner<>'';");
            while (rs.next()){
                String home = rs.getString("name");
                homes.add(home);
            }
        } catch (Exception ex) {
            plugin.sendConsole("Fail to get list of homes: ");
        }
        return homes;
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
        } catch (Exception ex) {
            plugin.sendConsole("Fail to get list of homes: ");
        }
        return homes;
    }
    
    public long getLastPay(String home){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT lastpay FROM `homes` WHERE name='"+home+"';");
            if (rs.first())
                return rs.getLong("lastpay");
        } catch (Exception ex) {
            plugin.sendConsole("Fail to get lastpay on home: " + home);
        }
        return 0L;
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
        } catch (Exception ex) {
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
        } catch (Exception ex) {
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
        } catch (Exception ex) {
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
        } catch (Exception ex) {
            plugin.sendConsole("Fail to set renter on home: " + region);
        }
        return false;
    }
    
    public void rent(Player p){
        String home = getHome(p.getLocation());
        if (isHome(home)){
            if (isAvailable(home)){
                double price = getRentPrice(home);
                EconomyResponse es = plugin.getEconomy().withdrawPlayer(p.getName(), price);
                if (es.transactionSuccess()){
                    setRenter(home, p.getName());
                    p.sendMessage(ChatColor.GREEN + "Congratulations! You are now renting this home");
                } else 
                    p.sendMessage(ChatColor.RED + "You do not have enough time to rent this home!");
            }
        } else
            p.sendMessage(ChatColor.RED + "You must stand inside a home first");
    }
    
    public void buy(Player p){
        String home = getHome(p.getLocation());
        boolean hasOwner = hasLandlord(home);
        if (isHome(home) && !hasOwner){
            double price = getBuyWorth(home);
            EconomyResponse es = plugin.getEconomy().withdrawPlayer(p.getName(), price);
            if (es.transactionSuccess()){
                setLandlord(home, p.getName());
                p.sendMessage(ChatColor.GREEN + "Congratulations! You now own this home");
            } else 
                p.sendMessage(ChatColor.RED + "You do not have enough time to buy this home!");
        } else if (hasOwner){
            String landlord = getLandlord(home);
            if (landlord.equalsIgnoreCase(p.getName()))
                p.sendMessage(ChatColor.RED + "You already own this home");
            else
                p.sendMessage(ChatColor.RED + "It appears this home already owned by " + ChatColor.GRAY + landlord);
        } else 
            p.sendMessage(ChatColor.RED + "You must stand inside a home first"); 
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
        } catch (Exception ex) {
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
