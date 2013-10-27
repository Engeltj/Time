/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import net.milkbowl.vault.item.Items;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

/**
 *
 * @author Tim
 */
public class TimeCommands implements Listener{
    private Time plugin;
    private CommandSender sender;
    private Command cmd;
    private String label;
    private String[] args;
    
    public TimeCommands(Time plugin, CommandSender sender, Command cmd, String label, String[] args) {
        this.sender = sender;
        this.plugin = plugin;
        this.cmd = cmd;
        this.label = label;
        this.args = args;
    }
    
    public boolean executeCommand(){
        String command = cmd.getName();
        if(command.equalsIgnoreCase("ghost") && args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return true;
            }
            // After checking to make sure that the sender is a Player, we can safely case it to one.
            Player s = (Player) sender;

            // Gets the player who shouldn't see the sender.
            Player target = Bukkit.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("Player " + args[0] + " is not online.");
                return true;
            }
            // Hides a given Player (s) from someone (target).
            target.hidePlayer(s);
            return true;
        }
        else if (command.equalsIgnoreCase("age")){//&& args.length == 1){
            double seconds = plugin.getTimePlayers().getPlayerConfig(sender.getName()).getPlayerAge();
            double minutes = 0;
            double hours = 0;
            double days = 0;
            double weeks = 0;
            double years = 0;
            
            if (seconds > 60){
                minutes = Math.floor(seconds/60);
                seconds -= minutes*60;
            }
            if (minutes > 60){
                hours = Math.floor(minutes/60);
                minutes -= hours*60;
            }
            if (hours > 24){
                days = Math.floor(hours/24);
                hours -= days*24;
            }
            if (days > 30){
                weeks = Math.floor(days/30);
                days -= weeks*30;
            }
            if (weeks > 52){
                years = Math.floor(weeks/52);
                weeks -= years*52;
            }
            
            String result = String.format("%04.0f·%02.0f·%02.0f·%01.0f·%02.0f·%02.0f", years,weeks,days,hours,minutes,seconds);
            
            sender.sendMessage(ChatColor.AQUA + result);
            return true;
        }
        else if (command.equalsIgnoreCase("life")){
            //SimpleDateFormat df = new SimpleDateFormat("'You have' HH 'hour(s)' mm 'minute(s)' ss 'second(s) left to live.'");
            double seconds = plugin.getEconomy().getBalance(sender.getName());
            double minutes = 0;
            double hours = 0;
            double days = 0;
            double weeks = 0;
            double years = 0;
            
            if (seconds > 60){
                minutes = Math.floor(seconds/60);
                seconds -= minutes*60;
            }
            if (minutes > 60){
                hours = Math.floor(minutes/60);
                minutes -= hours*60;
            }
            if (hours > 24){
                days = Math.floor(hours/24);
                hours -= days*24;
            }
            if (days > 30){
                weeks = Math.floor(days/30);
                days -= weeks*30;
            }
            if (weeks > 52){
                years = Math.floor(weeks/52);
                weeks -= years*52;
            }
            
            String result = String.format("%04.0f·%02.0f·%02.0f·%01.0f·%02.0f·%02.0f", years,weeks,days,hours,minutes,seconds);
            
            sender.sendMessage(ChatColor.DARK_GREEN + result);
            return true;
        }
        return false;
    }
    
    
    
}
