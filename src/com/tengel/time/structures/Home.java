/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.structures;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.tengel.time.Time;
import com.tengel.time.TimeCommands;
import com.tengel.time.WorldGuardUtil;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 *
 * @author Tim
 */
public class Home implements IStructure{
    private final Time plugin;
    private String name="";
    private String display_name="";
    private short zone=0;
    
    private int price=0;
    private int new_price=0;
    private long price_changed=0;
    private long price_warned=0;
    
    private String type="";
    private String landlord="";
    private String renter="";
    private long lastpay=0;
    private Vector door;
    
    public Home(Time plugin, String name){
        this.plugin = plugin;
        this.name = name;
    }
    
    public Home(Time plugin, Player p, String name, String type){
        if (name == null || name.length()==0)
            name = "home_" + System.currentTimeMillis()/1000;
        else if (!name.contains("home_"))
            name = "home_"+name;
        
        if ((type==null) || (type.length() == 0) || type.equalsIgnoreCase("apt"))
            type = "apartment";
        else if (type.equalsIgnoreCase("home"))
            type = "house";
        WorldGuardUtil wgu = new WorldGuardUtil(plugin, p.getWorld());
        if (!wgu.saveSchematic(p, "homes", name))
            p.sendMessage(ChatColor.RED + "Failed to save schematic for home '"+name+"', aborting home create.");
        if (wgu.createRegionFromSelection(p, name) == null)
            p.sendMessage(ChatColor.RED + "Failed to create region for home '"+name+"', aborting home create.");
        this.door = p.getLocation().toVector();
        this.plugin = plugin;
        this.name = name;
        this.type = type;
    }
    
    public void load() {
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM `homes` WHERE name='"+name+"';");
            display_name = rs.getString("display_name");
            zone = rs.getShort("zone");
            price = rs.getInt("price");
            new_price = rs.getInt("new_price");
            price_changed = rs.getLong("price_changed");
            price_warned = rs.getLong("price_warned");
            type = rs.getString("type");
            landlord = rs.getString("landlord");
            renter = rs.getString("renter");
            lastpay = rs.getLong("lastpay");
            door = new Vector(rs.getDouble("x"),rs.getDouble("y"),rs.getDouble("z"));
        } catch (Exception ex) {
            plugin.sendConsole("Failed to create entry for home '"+name+"' in Home class, " + ex);
        }
    }

    public void save() {
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM `homes` WHERE name='"+name+"';");
            String query = String.format("zone=%d,price=%d,new_price=%d,price_changed=%.0f," +
                        "price_warned=%.0f,type='%s',landlord='%s',renter='%s',lastpay=%.0f,x=%f,y=%f,z=%f", zone,price,new_price,price_changed,price_warned,
                        type,landlord,renter,lastpay,door.getX(),door.getY(),door.getZ());
            if (rs.first())
                st.executeUpdate("UPDATE `homes` SET "+query+" WHERE name='"+name+"';");
            else
                st.executeUpdate("INSERT INTO `homes` SET "+query+";");
        } catch (Exception ex) {
            plugin.sendConsole("Failed to create/update entry for home '"+name+"' in Home class, " + ex);
        }
    }

    public void remove() {
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            st.executeQuery("DELETE FROM `homes` WHERE name='"+name+"';");
            WorldGuardUtil wgu = new WorldGuardUtil(plugin, plugin.getServer().getWorld("Time"));
        } catch (Exception ex) {
            plugin.sendConsole("Failed to delete entry for home '"+name+"' in Home class, " + ex);
        }
    }
    
    public void reset(){
        World w = plugin.getServer().getWorld("Time");
        RegionManager mgr = plugin.worldGuard.getRegionManager(w);
        ProtectedRegion pr = mgr.getRegion(getName());
        if (pr != null){
            WorldGuardUtil wgu = new WorldGuardUtil(plugin, w);
            wgu.pasteSchematic(pr, getName(), "homes");
            
        } else
            plugin.sendConsole("Failed to reset home '"+getName()+"'");
    }
    
    public void buy(Player p){
        if (landlord.length()==0){
            double price = getBuyWorth();
            EconomyResponse es = plugin.getEconomy().withdrawPlayer(p.getName(), price);
            if (es.transactionSuccess()){
                setLandlord(p.getName());
                p.sendMessage(ChatColor.GREEN + "Congratulations! You now own this home");
            } else 
                p.sendMessage(ChatColor.RED + "You do not have enough time to buy this home!");
        } else if (getLandlord().length()>0){
            if (landlord.equalsIgnoreCase(p.getName()))
                p.sendMessage(ChatColor.RED + "You already own this home");
            else
                p.sendMessage(ChatColor.RED + "It appears this home already owned by " + ChatColor.GRAY + getLandlord());
        }
    }
    
    public void rent(Player p){
        if (!hasRenter()){
            double price = getRentPrice();
            EconomyResponse es = plugin.getEconomy().withdrawPlayer(p.getName(), price);
             if (es.transactionSuccess()){
                setRenter(p.getName());
                p.sendMessage(ChatColor.GREEN + "Congratulations! You are now renting this home");
            } else 
                p.sendMessage(ChatColor.RED + "You do not have enough time to rent this home!");
        } else {
            if (getRenter().equalsIgnoreCase(p.getName()))
                p.sendMessage(ChatColor.RED + "You are already renting this home");
            else
                p.sendMessage(ChatColor.RED + "It appears this home already being rented by " + ChatColor.GRAY + getRenter());
        }
    }
    
    public boolean sell(Player p, Player newOwner, double price){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int updated = st.executeUpdate("INSERT INTO `homes_offer` (name, seller, buyer, price, expiry) VALUES ("+
                    "'"+getName()+"','"+p.getName()+"','"+newOwner.getName()+"',"+price+","+(System.currentTimeMillis()/1000 + 2*60*60)+");");
            if (newOwner.isOnline()){
                newOwner.sendMessage(p.getName() + ChatColor.GREEN + " has offered you the home " + ChatColor.GRAY + getName() + ChatColor.GREEN + " for " + 
                        ChatColor.RED + TimeCommands.convertSecondsToTime(price) + ChatColor.GREEN + ". Type "+ ChatColor.GRAY+"/life job accept"+ ChatColor.GREEN+" to purchase");
            }
            return (updated > 0);
        } catch (Exception ex) {
            plugin.sendConsole("Fail to offer home '"+getName()+"' from '"+p.getName()+"' to '"+newOwner.getName()+"'");
        }
        return false;
    }
    
    public void disown(Player p){
        if (landlord.equalsIgnoreCase(p.getName())){
            setLandlord("");
            p.sendMessage(ChatColor.GREEN + "You now no longer own this home!");
        } else
            p.sendMessage(ChatColor.RED + "You do not own this home");
    }
    
    public double getBuyWorth(){
        return getRentWorth()*30;
    }
    
    public double getRentWorth(){
        WorldGuardUtil wgu = new WorldGuardUtil(plugin, plugin.getServer().getWorlds().get(0));
        com.sk89q.worldedit.Vector v = wgu.getSchematicDimensions(getName(), "homes");
        double area = v.getX()*v.getY()*v.getZ();
        return (area + area*5*getZone())*120;
    }
    
    public String getName(){
        return name;
    }
    
    public String getDisplayName(){
        if (display_name.length()==0)
            return name;
        return display_name;
    }
    
    public short getZone(){
        return zone;
    }
    
    public String getRenter(){
        return renter;
    }
    
    public String getLandlord(){
        return landlord;
    }
    
    public int getRentPrice(){
        return price;
    }
    
    public Vector getDoor(){
        return door;
    }
    
    public long getLastPay(){
        return lastpay;
    }
    
    public boolean hasRenter(){
        return (renter == null || renter.length()==0);
    }
    
    public void setPrice(int new_price){
        if (renter.length()==0){
            this.new_price = 0;
            this.price_changed = 0;
            this.price = new_price;
        } else {
            this.new_price = new_price;
            this.price_changed = System.currentTimeMillis()/1000;
        }
    }
    
    public void setDoor(Vector door){
        this.door = door;
    }
    
    public void setRenter(String player){
        renter = player;
    }
    
    public void setLandlord(String player){
        landlord = player;
    }
    
    public boolean setType(String type){
        if ((type==null) || (type.length() == 0) || type.equalsIgnoreCase("apt"))
            type = "apartment";
        else if (type.equalsIgnoreCase("home"))
            type = "house";
        if (!(type.equalsIgnoreCase("house")||type.equalsIgnoreCase("apartment")||type.equalsIgnoreCase("farm")))
            return false;
        this.type = type;
        return true;
    }
}
