/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Tim
 */
public class RunLater extends BukkitRunnable {
    private Player player;
    private String message;
    private String task;
    
    public RunLater(Player player, String task, String message) {
        this.player = player;
        this.message = message;
        this.task = task;
    }
 
    public void run() {
        player.sendMessage(message);
    }
}
