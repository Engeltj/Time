/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.runnables;

import com.tengel.time.structures.TimeMonster;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Tim
 */
public class RunnableSpawn extends BukkitRunnable {
    private final TimeMonster monster;
    
    public RunnableSpawn(TimeMonster monster){
        this.monster = monster;
    }
    
    public void run() {
        monster.spawn();
    }
    
}
