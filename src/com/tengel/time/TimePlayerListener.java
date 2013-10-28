/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import static org.bukkit.Bukkit.getServer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
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
import org.bukkit.event.player.PlayerInteractEvent;
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
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        String msg = event.getMessage();
        if (msg.equalsIgnoreCase("I love Depths")){
            p.sendMessage("Depths loves you!");
        }
        else if (msg.equalsIgnoreCase("balance"))
            p.sendMessage(Double.toString(plugin.getEconomy().getBalance(p.getName())));
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onSignChange(SignChangeEvent event){
        Player player = event.getPlayer();

        if (event.getLine(0).equalsIgnoreCase("[License]")){
          LicenseSigns ls = new LicenseSigns(plugin, player);
          ls.licenseSignCreate(event);
        }
        else if (event.getLine(0).equalsIgnoreCase("[Time Shop]")){
            
        }
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event){
        Block b = null;
        if (!event.hasBlock()) {
            try {
                b = event.getPlayer().getTargetBlock(null, 5);
            } catch (Exception e) {
                return;
            }
        } else
            b = event.getClickedBlock();
        
        
        if (b.getType().equals(Material.SIGN_POST) || b.getType().equals(Material.WALL_SIGN)) {
            LicenseSigns ls = new LicenseSigns(plugin, event.getPlayer());
            Sign s = (Sign) b.getState();
            String itemName = s.getLine(1);
            double cost = 0;
            Pattern p = Pattern.compile("-?\\d+");
            Matcher m = p.matcher(s.getLine(2));
            if (m.find()){
                cost = Double.valueOf(m.group());
            } else{
                plugin.sendConsole("SignInteract event, error reading cost");
                return;
            }
            ls.licenseBuy(itemName, cost);
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
