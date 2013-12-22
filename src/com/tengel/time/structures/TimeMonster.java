/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.structures;

import com.tengel.time.Time;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

/**
 *
 * @author Tim
 */
public class TimeMonster {
    final private Time plugin;
    private Location location;
    private EntityType type;
    private LivingEntity ent;
    private UUID uuid;
    private int id;
    
    public TimeMonster(Time plugin, Location loc, String type){
        this.location = loc;
        this.type = EntityType.valueOf(type);        
        this.plugin = plugin;
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            st.executeUpdate("INSERT INTO `spawns` (type,x,y,z) VALUES ('"+type+"',"+location.getX()+","+location.getY()+","+location.getZ()+");");
            ResultSet rs = st.executeQuery("SELECT id FROM `spawns` WHERE x="+location.getX()+" AND y="+location.getY()+" AND z="+location.getZ()+";");
            if (rs.first())
                 id = rs.getInt("id");
        } catch (Exception ex) {
            plugin.sendConsole("Failed to add new spawn for '"+type+"' in TimeMonster class, " + ex);
        }
    }
    
    public TimeMonster(Time plugin, int id){
        this.plugin = plugin;
        this.id = id;
        
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM `spawns` WHERE id="+id+";");
            if (rs.first()){
                location = new Location(plugin.getServer().getWorld("Time"),rs.getDouble("x"),rs.getDouble("y"),rs.getDouble("z"));
                type = EntityType.valueOf(rs.getString("type"));
                int uid = rs.getInt("uid");
                for (LivingEntity lent : location.getWorld().getLivingEntities())
                    if (lent.getUniqueId().variant() == uid)
                        ent = lent;
                if (ent == null)
                    spawn();
            } else 
                plugin.sendConsole("Failed to get spawn matching id '"+id+"' in TimeMonster class");
        } catch (Exception ex) {
            plugin.sendConsole("Failed to load id '"+id+"' in TimeMonster class, " + ex);
        }
    }
    
    public void save(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            st.executeUpdate("REPLACE INTO `spawns` SET type='"+type.toString()+"',uid="+uuid.variant()+",x="+
                    location.getX()+",y="+location.getY()+",z="+location.getZ()+" WHERE id="+id+";");
        } catch (Exception ex) {
            plugin.sendConsole("Failed to save monster with ID '"+id+"' in TimeMonster class, " + ex);
        }
    }
    
    public void spawn(){
        ent = (LivingEntity) location.getWorld().spawnEntity(location, type);
        plugin.removeMonster(uuid);
        uuid = ent.getUniqueId();
        plugin.addMonster(this);
    }
    
    public void spawnWithCheck(){
        for (LivingEntity lent : location.getWorld().getLivingEntities())
            if (lent.getUniqueId() == uuid)
                        return;
        spawn();
    }
    
    public LivingEntity getEntity(){
        return ent;
    }
    
    public UUID getUniqueId(){
        return uuid;
    }
    
}
