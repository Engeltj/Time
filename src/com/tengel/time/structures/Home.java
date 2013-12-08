/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.structures;

import com.tengel.time.Time;
import com.tengel.time.WorldGuardUtil;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.ChatColor;
import org.bukkit.util.Vector;

/**
 *
 * @author Tim
 */
public class Home implements IStructure{
    private Time plugin;
    private String name;
    private String display_name;
    private short zone;
    
    private int price;
    private int new_price;
    private long price_changed;
    private long price_warned;
    
    private String type;
    private String landlord;
    private String renter;
    private long lastpay;
    private Vector door;
    
    public Home(Time plugin, String region){
        this.plugin = plugin;
        this.name = region;
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
        } catch (SQLException ex) {
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
        } catch (SQLException ex) {
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
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to delete entry for home '"+name+"' in Home class, " + ex);
        }
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
    
    public int getPrice(){
        return price;
    }
    
    public void setPrice(int new_price){
        this.new_price = new_price;
        this.price_changed = System.currentTimeMillis()/1000;
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
