/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.runnables;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.tengel.time.Time;
import com.tengel.time.structures.TimeMonster;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Tim
 */
public class RunnableSpawn extends BukkitRunnable {
    private final Time plugin;
    private final Location location;
    private final EntityType type;
    
    public RunnableSpawn(Time plugin, Location location, String type){
        this.plugin = plugin;
        this.location = location;
        this.type = EntityType.valueOf(type);
        updateLocation();
    }
    
    public void run() {
        //spawn();
    }
    
    private void spawn(){
        Monster monster = (Monster) location.getWorld().spawnEntity(location, type);
        monster.setRemoveWhenFarAway(false);
        TimeMonster m = new TimeMonster(monster);
        //plugin.sendConsole("Spawning " + ent.toString());
        //plugin.getMobControl().removeMonster(uuid);
        //uuid = ent.getUniqueId();
        plugin.getMobControl().addTimeMonster(m);
    }
    
    private void updateLocation(){
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        boolean flag = false;
        while (location.getBlock().getType() != Material.AIR){
            location.add(0,1,0);
            flag = true;
        }
        if (flag){
            Connection con = plugin.getSql().getConnection();
            try {
                Statement st = con.createStatement();
                int updated = st.executeUpdate("UPDATE `spawns` SET x="+location.getBlockX()+",y="+location.getBlockY()+",z="+location.getBlockZ()+" WHERE " +
                        "x="+x+" AND y="+y+" AND z="+z+";");
                
                if (updated > 0)
                    plugin.sendConsole("Location for monster '"+type.name()+"' changed to x="+location.getBlockX()+",y="+location.getBlockY()+",z="+location.getBlockZ()+
                            " from x="+x+",y="+y+",z="+z);
            } catch (Exception ex) {
                plugin.sendConsole("Failed to fix mob location, " + ex);
            }
        }
    }
    
}
