/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

/**
 *
 * @author Tim
 */
public class TimeShop extends TimeSigns {
    
    public TimeShop(Time plugin, Player player){
        super(plugin, player);
    }
    
    public void buy(String itemName, int cost){
        Material m = getItemMaterial(itemName);
        if (getPlugin().getEconomy().getBalance(getPlayer().getName()) >= cost){
            EconomyResponse es = getPlugin().getEconomy().withdrawPlayer(getPlayer().getName(), cost*60);
            if (es.transactionSuccess()){
                //getPlayer().
            }
        }
    }
    
    public void create(SignChangeEvent event){
        if (getPlugin().getPlayerListener().checkPermissions(getPlayer(), "shopsign.create", false)){
            Material m = getItemMaterial(event.getLine(1));
            int cost = 0;
            if (m == null){
                getPlayer().sendMessage("Invalid item name or ID on line 2");
                dropSign(event.getBlock().getLocation());
                return;
            }
            try {
                cost = Integer.parseInt(event.getLine(2));
            }catch (Exception e){
                getPlayer().sendMessage(getPlugin().getPluginName() + "Invalid cost on line 3.");
                dropSign(event.getBlock().getLocation());
                return;
            }
            
            event.setLine(0, ChatColor.GREEN + "[Time Shop]");
            event.setLine(1, m.name());
            event.setLine(2, "Cost: " + String.valueOf(cost) + " mins");
            
            TimeShopConfig tsc = new TimeShopConfig(getPlugin());
            tsc.updateItem(m.getId(), cost);
            super.create(event);
        } else{
            getPlayer().sendMessage(getPlugin().getPluginName() + "You do not have permissions to do that!");
            dropSign(event.getBlock().getLocation());
        }
        
    }
    
}
