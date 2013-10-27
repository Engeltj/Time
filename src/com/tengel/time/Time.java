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

    private TimePlayerListener playerListener;
    private TimeUpdate timeUpdater;
    private Economy economy = null;
    private String pluginName;
    private TimePlayers players;

    public Time() {
        players = new TimePlayers(this);
        playerListener = new TimePlayerListener(this,players);
        timeUpdater = new TimeUpdate(this,1);
    }
    
    @Override
    public void onEnable(){
        PluginManager pm = getServer().getPluginManager();
        populateTimePlayers();
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
    
    public void sendConsole(String message){
        getLogger().info(message);
    }
    
    public void populateTimePlayers(){
        for (Player player: getServer().getOnlinePlayers()){
            players.addPlayer(player);
        }
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
    
    public TimePlayers getTimePlayers(){
        return this.players;
    }
    
    public TimePlayerListener getPlayerListener(){
        return this.playerListener;
    }
    
    public String getPluginName(){
        return this.pluginName;
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
            double seconds = this.getEconomy().getBalance(sender.getName());
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
        }
        return false;
    }
}
