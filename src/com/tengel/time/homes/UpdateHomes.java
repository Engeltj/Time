/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.homes;

import com.tengel.time.ConfigPlayer;
import com.tengel.time.Time;
import com.tengel.time.TimeCommands;
import java.util.Set;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Tim
 */
public class UpdateHomes implements Runnable {
    private Time plugin;
    
    public UpdateHomes(Time plugin){
        this.plugin = plugin;
    }
    
    private boolean chargePlayer(Player p, double amount){
        EconomyResponse es = plugin.getEconomy().withdrawPlayer(p.getName(), amount);
        if (!es.transactionSuccess()){
            if (p.isOnline())
                p.sendMessage(plugin.getPluginName() + ChatColor.RED + "You just lost your home, failure to pay");
            return false;
        } else {
            if (p.isOnline()){
                TimeCommands tc = new TimeCommands();
                p.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "You just paid your daily rent of " + ChatColor.GRAY + tc.convertSecondsToTime(amount));
            }
        }
        return true;
    }
    
    public void run() {
        Homes h = new Homes(plugin);
        Set<String> temp = h.getHomes();
        String [] homes = temp.toArray(new String[0]);
        long time = System.currentTimeMillis()/1000;
        for (String home : homes){
            long lastpay = h.getLastPay(home);
            long diff = time - lastpay;
            if (diff > 24*60*60){
                String renter = h.getRenter(home);
                Player p = plugin.getServer().getPlayer(renter);
                if (renter.length() != 0){
                    double rent = h.getPrice(home);
                    if (!chargePlayer(p, rent)){
                        ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(p.getName());
                        cp.removeHome(home);
                        h.removeRenter(home);
                    } else {
                        String landlord = h.getLandlord(home);
                        Player p_lord = plugin.getServer().getPlayer(landlord);
                        if (p_lord != null){
                            plugin.getEconomy().depositPlayer(landlord, rent);
                            if (p_lord.isOnline()){
                                TimeCommands tc = new TimeCommands();
                                p_lord.sendMessage(plugin.getPluginName()+ renter + " has paid you his/her daily rent of " + ChatColor.GREEN + tc.convertSecondsToTime(rent));
                            }
                        }
                    }
                }
            }
        }
        
        Bukkit.getServer().getOfflinePlayer(null);
    }
    
}
