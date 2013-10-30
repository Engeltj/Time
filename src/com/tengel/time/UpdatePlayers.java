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
            PlayerConfig config = new PlayerConfig(plugin, player);
            //plugin.sendConsole(Double.toString(config.getPlayerAge()));
            if (config.getPlayerAge() > 7*24*60*60*1000) { //7000 days
                EconomyResponse es = plugin.getEconomy().withdrawPlayer(player.getName(), 1*updateInterval);
                if (!es.transactionSuccess()){
                    String name = player.getName();
                    player.kickPlayer("You ran out of time! You're entire profile has been reset.");
                    for (World world : plugin.getServer().getWorlds()){
                        try {
                            File f = new File(System.getProperty("user.dir") + "\\" + world.getName() + "\\players\\" + name + ".dat");
                            f.delete();
                        }catch(Exception e){}
                    }
                    
                    try{
                        FileOutputStream writer = new FileOutputStream(System.getProperty("user.dir") + "\\plugins\\Essentials\\userdata\\" + name.toLowerCase() + ".yml");
                        writer.write(0);
                        writer.close();
                    }
                    catch (Exception e){
                        plugin.sendConsole(plugin.getPluginName() + "Failed to delete " + "Essentials\\userdata\\" + name.toLowerCase() + ".yml");
                    }
                    synchronized(config.getConfigFile()){
                         config.removePlayer();
                    }
                  
                }
            }
        }
    }
    
    public int getUpdateInterval(){
        return this.updateInterval;
    }
}
