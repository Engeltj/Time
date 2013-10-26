/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

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
public final class Time extends JavaPlugin implements ITime{
    public static Time time;
    public final TimePlayerListener playerListener;
    private Economy economy = null;
    String pluginName;

    public Time() {
        this.playerListener = new TimePlayerListener(this);
    }
    
    @Override
    public void onEnable(){
        PluginManager pm = getServer().getPluginManager();
        pluginName = "[" + pm.getPlugin("Time").getName() + "] ";
        runTheTimerThingy(20*60*15,60);
        
        pm.registerEvents(this.playerListener, this);
        
        getServer().getServicesManager().register(Economy.class, new EconomyControl(), this, ServicePriority.Highest);

        getLogger().info("Time by Engeltj has been enabled");
    }
 
    @Override
    public void onDisable() {
        getLogger().info("Time by Engeltj has been disabled");
    }
    
    private void setupExternalEconomy() {
        Plugin vault = this.getServer().getPluginManager().getPlugin("Vault");
        if (vault != null & vault instanceof Vault) {
            RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null)
                economy = economyProvider.getProvider();
            else
                getLogger().info(this.pluginName + "Servers Economy plugin seems to be missing!");
        }
    }
    
    public Economy getEconomyManager(){
        if (this.economy == null)
            setupExternalEconomy();
        return this.economy;
    }
    
    
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("ghost") && args.length == 1) {
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
        return false;
    }
    
    
    public static long getTicksFromMinutes(long minutes){//short method to convert minutes to ticks
 
    return minutes * 60 * 20; //Minutes times 60 for seconds times 20 for ticks.
 
}
 
    //Delay is a delay before it will start broadcasting, period is how much time between running(in minutes, here)
    public void runTheTimerThingy(long delayTicks, long periodMinutes){

        //Schedule our task with the scheduler
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){

            public void run() {
                getServer().broadcastMessage("Depths in currently working on its own server plugin with a complete RPG experience!");
                //Bukkit.getServer().broadcast(ChatColor.DARK_RED + "Garris0n " + ChatColor.GOLD + "is awesome!");
            }

        }, delayTicks, getTicksFromMinutes(periodMinutes));

    }
}
