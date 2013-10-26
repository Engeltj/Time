/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
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
    private Economy economy;
    String pluginName;

    public Time() {
        this.playerListener = new TimePlayerListener(this);
    }
    
    @Override
    public void onEnable(){
        PluginManager pm = getServer().getPluginManager();
        pluginName = "[" + pm.getPlugin("Time").getName() + "] ";
        
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
}
