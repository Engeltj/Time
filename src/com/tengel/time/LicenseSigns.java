/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Tim
 */
public class LicenseSigns {
    private Time plugin;
    private Player player;
    
    public LicenseSigns(Time plugin, Player player){
        this.plugin = plugin;
        this.player = player;
    }
    
    public void licenseAdd(String block, double cost){
        int b=0;
        Material m = null;
        try {
            b = Integer.parseInt(block);
        } catch (Exception e){
            return;
        }
        for (Material mat : Material.values()){
            final int id = mat.getId();
            if (b == id)
                m = mat;
        }
        if (plugin.getPlayerListener().checkPermissions(player, "license.add", false)) {
            if (plugin.getEconomy().getBalance(player.getName()) >= cost){
                PlayerConfig configFile = plugin.getTimePlayers().getPlayerConfig(player.getName());
                if (configFile.addLicense(m.name(), b)){
                    plugin.getEconomy().withdrawPlayer(configFile.getPlayerName(), cost);
                    player.sendMessage(plugin.getPluginName() + m.name() + " license aquired!");
                }
                else {
                    player.sendMessage(plugin.getPluginName() + "It appears you already own the license to mine " + m.name());
                }
            }
        }
    }
    
    public void licenseSignCreate(SignChangeEvent event){
        if (plugin.getPlayerListener().checkPermissions(player, "license.createsign", false)) {
            Material m = null;
            int blockId = -1;
            int cost = 10;
            try {
                blockId = Integer.parseInt(event.getLine(1));
            }catch (Exception e){}
            
            for (Material mat : Material.values()){
                if ((mat.getId() == blockId) || (mat.name().equalsIgnoreCase(event.getLine(1)))){
                    event.setLine(0, ChatColor.BLUE + "[License]");
                    event.setLine(1, mat.name());
                } 
            }
           
            try {
                cost = Integer.parseInt(event.getLine(1));
            }catch (Exception e){
                //plugin.sendConsole("Failed to parse cost (" + event.getLine(1) + ") in licenseSignCreate");
            }
            
            event.setLine(2, "Time: " + String.valueOf(cost) + "mins");
          }
          else {
            player.sendMessage(plugin.getPluginName() + "You do not have permissions to do that!");
            dropSign(event.getBlock().getLocation());
          }
    }
    
    private void dropSign(Location location) {
        location.getBlock().setType(Material.AIR);
        location.getWorld().dropItemNaturally(location, new ItemStack(Material.SIGN, 1));
    }
}
