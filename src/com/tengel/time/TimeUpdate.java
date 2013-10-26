/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import java.io.File;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

/**
 *
 * @author Tim
 */
public class TimeUpdate implements Runnable {
    private Time plugin;
    private int updateInterval; //in seconds
    
    public TimeUpdate(Time plugin){
        this.plugin = plugin;
        this.updateInterval = 10;
    }
    
    public TimeUpdate(Time plugin, int updateInterval){
        this.plugin = plugin;
        this.updateInterval = updateInterval;
    }
    
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()){
            EconomyResponse es = plugin.getEconomy().withdrawPlayer(player.getName(), 1*updateInterval);
            if (!es.transactionSuccess()){
                //resetPlayer
                player.kickPlayer("You ran out of time! You're entire profile has been reset.");
                File f = new File("Essentials\\userdata\\" + player.getName().toLowerCase() + ".yml");
                f.delete();
                f = new File("..\\world\\players\\" + player.getName().toLowerCase() + ".yml");
                f.delete();
            }
        }
    }
    
    public int getUpdateInterval(){
        return this.updateInterval;
    }
}
