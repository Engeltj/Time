/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.runnables;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.tengel.time.Time;
import com.tengel.time.structures.TimeMonster;
import org.bukkit.Location;
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
    }
    
    public void run() {
        spawn();
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
    
}
