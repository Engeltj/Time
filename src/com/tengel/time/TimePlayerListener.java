/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import org.bukkit.Bukkit;
import static org.bukkit.Bukkit.getServer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Tim
 */
public class TimePlayerListener implements Listener {
    private Time plugin;
    private TimePlayers players;
    
    public TimePlayerListener(Time plugin, TimePlayers players) {
            this.plugin = plugin;
            this.players = players;
    }
    
    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        p.remove();
        Location loc = p.getLocation();
        String msg = event.getMessage();
        if (msg.equalsIgnoreCase("I love Depths")){
            p.sendMessage("Depths loves you!");
            loc.setX(loc.getX()+5);
            //p.getWorld().
            p.setDisplayName("Depths Lover");
            p.setPlayerListName("Depths Lover");
        }
        else if (msg.equalsIgnoreCase("balance"))
            p.sendMessage(Double.toString(plugin.getEconomy().getBalance(p.getName())));
        PlayerConfig pc = new PlayerConfig(p, plugin);
        pc.updateLastOnline();
        pc.save();
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onSignChange(SignChangeEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (event.getLine(0).equalsIgnoreCase("[License]")){
          LicenseSigns ls = new LicenseSigns(plugin, player);
          ls.licenseSignCreate(event);
        }
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        players.addPlayer(player);
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        players.removePlayer(player);
    }
    
    public boolean playerExists(String playername){
        for (OfflinePlayer player : plugin.getServer().getOfflinePlayers()) {
            if (player.getName().equalsIgnoreCase(playername)) {
              return true;
            }
        }
        return false;
    }
    
    public boolean checkPermissions(CommandSender sender, String permission, boolean sendMessage){
        if (!(sender instanceof Player))
            return true;

        if (sender.hasPermission("time." + permission)) {
            return true;
        }
        if (sendMessage) {
            sender.sendMessage(plugin.getPluginName() + "You do not have the permissions to do this.");
        }
        return false;
    }

}
