/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import java.text.SimpleDateFormat;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
/**
 *
 * @author Tim
 */
public final class Time extends JavaPlugin {
    //public static Time time;

    public final TimePlayerListener playerListener;
    private TimeUpdate timeUpdater;
    private Economy economy = null;
    String pluginName;

    public Time() {
        playerListener = new TimePlayerListener(this);
        timeUpdater = new TimeUpdate(this,5);
    }
    
    @Override
    public void onEnable(){
        PluginManager pm = getServer().getPluginManager();

        if (!setupEconomy() ) {
            getLogger().info(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("ECONOMY: " + economy.getName());
        pluginName = "[" + pm.getPlugin("Time").getName() + "] ";
        
        pm.registerEvents(this.playerListener, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, timeUpdater, 0, timeUpdater.getUpdateInterval() * 20);
        //getServer().getServicesManager().register(Economy.class, new EconomyControl(), this, ServicePriority.Highest);
        
        
        getLogger().info("Time by Engeltj has been enabled");
    }
 
    @Override
    public void onDisable() {
        getLogger().info("Time by Engeltj has been disabled");
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
    
    public Economy getEconomy(){
        return this.economy;
    }    
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
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
        else if (command.equalsIgnoreCase("life") && args.length == 1){
            
        }
        else if (command.equalsIgnoreCase("life")){
            //SimpleDateFormat df = new SimpleDateFormat("'You have' HH 'hour(s)' mm 'minute(s)' ss 'second(s) left to live.'");
            int seconds = (int) this.getEconomy().getBalance(sender.getName());
            int minutes = 0;
            int hours = 0;
            int days = 0;
            int years = 0;
            
            if (seconds > 60){
                minutes = (int)Math.floor(seconds/60);
                seconds -= minutes*60;
            }
            if (minutes > 60){
                hours = (int)Math.floor(minutes/60);
                minutes -= hours*60;
            }
            if (hours > 24){
                days = (int)Math.floor(hours/60);
                hours -= days*60;
            }
            if (days > 365){
                years = (int)Math.floor(days/365);
                days -= years*60;
            }
            
            if (years > 1)
                sender.sendMessage(this.pluginName + "You have: " + Integer.toString(years) + " year(s) and " + Integer.toString(days) + " day(s) left to live.");
            else if (days > 1)
                sender.sendMessage(this.pluginName + "You have: " + Integer.toString(days) + " day(s) and " + Integer.toString(hours) + " hour(s) left to live.");
            else if (hours > 1)
                sender.sendMessage(this.pluginName + "You have: " + Integer.toString(hours) + " hour(s) and " + Integer.toString(minutes) + " minute(s) left to live.");
            else if (minutes > 1)
                sender.sendMessage(this.pluginName + "You have: " + Integer.toString(minutes) + " minute(s) and " + Integer.toString(seconds) + " second(s) left to live.");
            else
                sender.sendMessage(this.pluginName + "You have: " + Integer.toString(seconds) + " second(s) left to live.");
        }
        return false;
    }
}
