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
import org.bukkit.ChatColor;
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
            RegionControl rc = plugin.getRegionControl();
            boolean wrongZone = rc.isWrongTimeZone(player);
            ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(player.getName());
            if (wrongZone){
                EconomyResponse es = plugin.getEconomy().withdrawPlayer(player.getName(), 1*updateInterval);
                //ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(player.getName());
                if (cp.getBounty() <= 0)
                    player.sendMessage(plugin.getPluginName() + ChatColor.WHITE + "You've been added to the wanted list!");
                if (es.transactionSuccess())
                    cp.addBounty(1*updateInterval);
            } else if (!wrongZone && cp.getBounty() > 0){
                cp.addBounty(updateInterval*-1);
                if (cp.getBounty() == 0){
                    player.sendMessage(plugin.getPluginName() + ChatColor.WHITE + "You've are free and cleared of all charges");
                }
            }
              
            
            
            if ((cp.getPlayerAge() > 7*24*60*60*1000)){ //7000 days
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
