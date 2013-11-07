/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.tengel.time.profs.TimeProfession;
import net.milkbowl.vault.economy.EconomyResponse;
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
    
    public TimeCommands(){
        
    }
    
    public TimeCommands(Time plugin, CommandSender sender, Command cmd, String label, String[] args) {
        this.sender = sender;
        this.plugin = plugin;
        this.cmd = cmd;
        this.label = label;
        this.args = args;
    }
    
    
    public String convertSecondsToTime(double seconds){
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

        return String.format("%04.0f·%02.0f·%02.0f·%01.0f·%02.0f·%02.0f", years,weeks,days,hours,minutes,seconds);
        
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
        if (!command.equalsIgnoreCase("life"))
            return false;
        else if (args.length == 0){
            sender.sendMessage(ChatColor.YELLOW + "Time format: YYYY/WW/DD/HH/MM/SS");
            sender.sendMessage(ChatColor.BOLD + "" + ChatColor.GREEN + "Your options are: ");
            sender.sendMessage(ChatColor.GREEN + "/life age  -- How long you've played");
            sender.sendMessage(ChatColor.GREEN + "/life left  -- How long you have left to live");
            sender.sendMessage(ChatColor.GREEN + "/life bounty  -- The bounty on your head to be captured");
            sender.sendMessage(ChatColor.GREEN + "/life bail  -- Pays off the price on your head so you may leave jail");
        }
        
        else if (args[0].equalsIgnoreCase("age")){
            double seconds = plugin.getTimePlayers().getPlayerConfig(sender.getName()).getPlayerAge();
            String time = convertSecondsToTime(seconds);
            sender.sendMessage(plugin.getPluginName() + ChatColor.AQUA + time);
        }
        else if (args[0].equalsIgnoreCase("left")){
            double seconds = plugin.getEconomy().getBalance(sender.getName());
            String time = convertSecondsToTime(seconds);
            sender.sendMessage(plugin.getPluginName() + ChatColor.DARK_GREEN + time); 
        }
        else if (args[0].equalsIgnoreCase("bounty")){
            ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(sender.getName());
            double bounty = cp.getBounty();
            String time = convertSecondsToTime(bounty);
            if (bounty > 0)
                sender.sendMessage(plugin.getPluginName() + ChatColor.RED + time);
            else
                sender.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "You are not on the bounty list");
        }
        else if (args[0].equalsIgnoreCase("bail")){
            ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(sender.getName());
            if (cp.isJailed()){
                int bounty = cp.getBounty();
                EconomyResponse es = plugin.getEconomy().withdrawPlayer(sender.getName(), bounty);
                if (es.transactionSuccess()){
                    sender.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "You've been freed at the cost of " + ChatColor.RED + cp.getBountyString());
                    //free user
                } else
                    sender.sendMessage(plugin.getPluginName() + ChatColor.RED + "You cannot afford bail, you must wait this one out.");
            } else
                sender.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "You are not in jail");
        } else if (args[0].equalsIgnoreCase("unemploy")){
            ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(sender.getName());
            TimeProfession tp = TimeProfession.UNEMPLOYED;
            int cost = tp.getUnemployCost(cp.getPlayerTimeZone());
            EconomyResponse es = plugin.getEconomy().withdrawPlayer(sender.getName(), cost);
        }
        return true;
    }
    
    
    
}
