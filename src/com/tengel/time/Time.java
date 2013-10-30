/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import java.io.File;
import java.text.SimpleDateFormat;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
    private TimePlayerListener playerListener;
    private UpdatePlayers timeUpdater;
    private Economy economy = null;
    private String pluginName;
    private TimePlayers players;
    private File configSigns;

    public Time() {
        players = new TimePlayers(this);
        playerListener = new TimePlayerListener(this,players);
        timeUpdater = new UpdatePlayers(this,1);
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
        
        pluginName = "[" + pm.getPlugin("Time").getName() + "] ";
        
        pm.registerEvents(this.playerListener, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, timeUpdater, 0, timeUpdater.getUpdateInterval() * 20);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new UpdateSigns(this), 0, 1 * 60 * 20);
        
        
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
    
    public File getConfigSigns(){
        if (configSigns == null)
            configSigns = new File(this.getDataFolder() + "\\signs.yml");
        if (!this.configSigns.exists()){
            try {
                this.configSigns.createNewFile();
            } catch (Exception e){
                this.sendConsole("Error creating signs.yml");
            }
        }
        return this.configSigns;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        TimeCommands tc = new TimeCommands(this, sender, cmd, label, args);
        return tc.executeCommand();
    }
    
    public Material getItemMaterial(String id_or_name){
        int blockId = -1;
        try {
            blockId = Integer.parseInt(id_or_name);
        } catch (Exception e){}
        for (Material mat : Material.values()){
            final int id = mat.getId();
            if ((blockId == id) || (id_or_name.equalsIgnoreCase(mat.name()))){
                return mat;
            }
        }
        return null;
    }
}
