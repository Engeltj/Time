/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.mysql;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.tengel.time.Config;
import com.tengel.time.Time;
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
        //configFile = new Config(plugin, "homes.yml");
        this.plugin = plugin;
    }
    
    public boolean create(Player p, String region, double price, boolean farm){
        if (!plugin.worldGuard.getRegionManager(p.getWorld()).hasRegion(region)){
            p.sendMessage(plugin.getPluginName() + ChatColor.RED + "Cannot create home, region '" + region + "' does not exist on this world!");
            return false;
        }
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            int updated=0;
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM `homes` WHERE name='"+region+"';");
            if (rs.first())
                updated =st.executeUpdate("UPDATE homes SET (price, type) VALUES ("+price+", "+farm+") WHERE name='"+region+"';");
            else
                updated = st.executeUpdate("INSERT INTO homes (name, price, type) VALUES ('"+region+"', "+price+", "+farm+");");
            if (updated > 0)
                p.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "Successfully create home");
            else
                p.sendMessage(plugin.getPluginName() + ChatColor.RED + "Failed to create home, no rows updated");
            return (updated > 0);
        } catch (SQLException ex) {
            Logger.getLogger(Homes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;        
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
        Set<String> homes = new HashSet<String>();//configFile.getKeys(false);
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
            return (updated > 0);
        } catch (SQLException ex) {
            plugin.sendConsole("Fail to set renter on home: " + region);
        }
        return false;
    }
    
    public boolean rent(Player p){
        Map<String, ProtectedRegion> regions = plugin.worldGuard.getRegionManager(p.getWorld()).getRegions();
        for (ProtectedRegion rg : regions.values()){
            String region = rg.getId();
            if (isAvailable(region)){
                double price = getPrice(region);
                EconomyResponse es = plugin.getEconomy().withdrawPlayer(p.getName(), price);
                if (es.transactionSuccess()){
                    setRenter(region, p.getName());
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
