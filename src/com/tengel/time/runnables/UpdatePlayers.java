/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.runnables;

import com.tengel.time.RegionControl;
import com.tengel.time.Time;
import com.tengel.time.structures.TimePlayer;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Tim
 */
public class UpdatePlayers implements Runnable {
    private final Time plugin;
    private final int updateInterval; //in seconds
    
    public UpdatePlayers(Time plugin, int updateInterval){
        this.plugin = plugin;
        this.updateInterval = updateInterval;
    }
    
    public void run() {
        RegionControl rc = plugin.getRegionControl();
        for (Player player : plugin.getServer().getOnlinePlayers()){
            boolean wrongZone = rc.isWrongZone(player);
            TimePlayer tp = plugin.getPlayer(player.getName());
            if (wrongZone){
                plugin.getEconomy().withdrawPlayer(player.getName(), updateInterval);
                //ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(player.getName());
                if (tp.getBounty() <= 0)
                    player.sendMessage(ChatColor.RED + "You've been added to the wanted list!");
                //if (es.transactionSuccess())
                    tp.addBounty(updateInterval);
            } else if (tp.getBounty() > 0){
                tp.addBounty(updateInterval*-1);
                if (tp.getBounty() == 0)
                    player.sendMessage(ChatColor.GREEN + "You are free and cleared of all charges");
            }
            if ((tp.getAge() > 60) && !tp.hasDied()){ //7 days
                EconomyResponse es = plugin.getEconomy().withdrawPlayer(player.getName(), updateInterval);
                if (!es.transactionSuccess()){
                    if (!tp.getJailed())
                        tp.outOfTime();
                }
            } else if(tp.hasDied()){
                if (plugin.getEconomy().getBalance(tp.getName()) > 24*60*60){
                    tp.outOfTimeRestore();
                }
            }
        }
    }
    
    public int getUpdateInterval(){
        return this.updateInterval;
    }
}