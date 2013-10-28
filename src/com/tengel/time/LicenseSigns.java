/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Tim
 */
public class LicenseSigns extends TimeSigns{
    
    
    public LicenseSigns(Time plugin, Player player){
        super(plugin, player);
    }
    
    public void buy(String block, double cost){
        Material m = getItemMaterial(block);
        if (m == null){
            getPlugin().sendConsole("Invalid item via license sign");
            return;
        }
        if (getPlugin().getPlayerListener().checkPermissions(getPlayer(), "license.buy", false)) {
            if (getPlugin().getEconomy().getBalance(getPlayer().getName()) >= cost){
                PlayerConfig configFile = getPlugin().getTimePlayers().getPlayerConfig(getPlayer().getName());
                
                if (configFile.addLicense(m.name(), Material.valueOf(m.name()).getId())){
                    getPlugin().getEconomy().withdrawPlayer(configFile.getPlayerName(), cost);
                    getPlayer().sendMessage(getPlugin().getPluginName() + m.name() + " license aquired!");
                }
                else {
                    getPlayer().sendMessage(getPlugin().getPluginName() + "It appears you already own the license to mine " + m.name());
                }
            }
        } else
            getPlayer().sendMessage(getPlugin().getPluginName() + "You do not have permissions to do that!");
    }
    
    public void create(SignChangeEvent event){
        if (getPlugin().getPlayerListener().checkPermissions(getPlayer(), "license.createsign", false)) {
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

            event.setLine(0, ChatColor.BLUE + "[License]");
            event.setLine(1, m.name());
            event.setLine(2, "Cost: " + String.valueOf(cost) + " mins");
            super.create(event);
        }
        else {
          getPlayer().sendMessage(getPlugin().getPluginName() + "You do not have permissions to do that!");
          dropSign(event.getBlock().getLocation());
        }
    }
}
