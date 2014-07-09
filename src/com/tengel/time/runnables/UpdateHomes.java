/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.runnables;

import com.tengel.time.Time;
import com.tengel.time.Commands;
import com.tengel.time.structures.Home;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Tim
 */
public class UpdateHomes implements Runnable {
    private final Time plugin;
    
    public UpdateHomes(Time plugin){
        this.plugin = plugin;
    }
    
    private boolean chargePlayer(Player p, double amount){
        EconomyResponse es = plugin.getEconomy().withdrawPlayer(p.getName(), amount);
        if (!es.transactionSuccess()){
            if (p.isOnline())
                p.sendMessage(ChatColor.RED + "You just lost your home, failure to pay");
            return false;
        } else {
            if (p.isOnline()){
                p.sendMessage(ChatColor.GREEN + "You just paid your daily rent of " + ChatColor.GRAY + Commands.convertSecondsToTime(amount));
            }
        }
        return true;
    }
    
    public void run() {
        Map<String, Home> homes = plugin.getHomes();
        long time = System.currentTimeMillis()/1000;
        //h.updateHomeRentPricesWithLandlords(); //unsure what this does
        for (String home : homes.keySet()){
            Home h = homes.get(home);
            long lastpay = h.getLastPay();
            long diff = time - lastpay;
            if (diff > 24*60*60){
                String renter = h.getRenter();
                Player p = plugin.getServer().getPlayer(renter);
                if (renter.length() != 0){
                    double rent = h.getRentPrice();
                    if (!chargePlayer(p, rent)){
                        h.setRenter("");
                    } else {
                        String landlord = h.getLandlord();
                        Player p_lord = plugin.getServer().getPlayer(landlord);
                        if (p_lord != null){
                            plugin.getEconomy().depositPlayer(landlord, rent);
                            if (p_lord.isOnline()){
                                p_lord.sendMessage(plugin.getPluginName()+ renter + " has paid you his/her daily rent of " + ChatColor.GREEN + Commands.convertSecondsToTime(rent));
                            }
                        }
                    }
                }
            }
        }
    }
    
}
