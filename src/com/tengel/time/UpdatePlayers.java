/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicInteger;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author Tim
 */
public class UpdatePlayers implements Runnable {
    private Time plugin;
    private int updateInterval; //in seconds
    
    public UpdatePlayers(Time plugin){
        this.plugin = plugin;
        this.updateInterval = 10;
    }
    
    public UpdatePlayers(Time plugin, int updateInterval){
        this.plugin = plugin;
        this.updateInterval = updateInterval;
    }
    
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()){
            ConfigPlayer config = new ConfigPlayer(plugin, player);
            if (config.getPlayerAge() > 7*24*60*60*1000) { //7000 days
                EconomyResponse es = plugin.getEconomy().withdrawPlayer(player.getName(), 1*updateInterval);
                if (!es.transactionSuccess()){
                    plugin.getTimePlayers().resetPlayer(player);
                }
                //BukkitTask task = new ExampleTask(this.plugin).runTaskLater(this.plugin, 20);
            }
        }
    }
    
    public int getUpdateInterval(){
        return this.updateInterval;
    }
}
