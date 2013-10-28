/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author Tim
 */
public class PlayerConfig extends YamlConfiguration {
    private File configFile;
    //private Player player;
    private Time plugin;
    private String playerName;
    
    public PlayerConfig(Player player, Time plugin){
        super();
        playerName = player.getName();
        
        File folder = new File(plugin.getDataFolder(), "players");
        if (!folder.exists())
                folder.mkdirs();
        
        this.configFile = new File(plugin.getDataFolder() + "\\players", player.getName() + ".yml").getAbsoluteFile();
        //this.player = player;
        this.plugin = plugin;
        if (!this.configFile.exists()){
            try {
                this.configFile.createNewFile();
                setPlayerStart();
            } catch (Exception e){
                plugin.sendConsole("Error creating profile for player: " + player.getName());
            }
        } else
            try{
                super.load(this.configFile);
            }catch (Exception e){}
    }
    
    public String getPlayerName(){
        return this.playerName;
    }
    
    public void updateLastOnline(){
        set("lastonline", System.currentTimeMillis()/1000);
        save();
    }
    
    public void setPlayerStart(){
        set("start", System.currentTimeMillis()/1000);
        save();
    }
    
    public boolean addLicense(String name, int id){
        ConfigurationSection section = this.getConfigurationSection("license");
        if (section == null)
            section = createSection("license");
        if (section.get(name) == null){
            plugin.sendConsole("Adding license: " + name + " of id: " + String.valueOf(id));
            this.set("license."+name, id);
            save();
            return true;
        }
        else
            return false;
    }
    
    public double getPlayerAge(){
        //plugin.sendConsole(Long.toString(getLong("start")));
        double start = 0;
        try {
            start = Double.valueOf(getString("start"));
        } catch (Exception e){
            return 100*24*60*60;
        }
        double age = System.currentTimeMillis()/1000 - start;
        return age;
    }
    
    public File getConfigFile(){
        return this.configFile;
    }
    
    public void removePlayer(){
        try {
            configFile.delete();
        } catch (Exception e){}
    }
    
    public void save()
    {
        try{
            save(configFile);
        }
        catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    @Override
    public synchronized long getLong(String path)
    {
            return super.getLong(path);
    }
    
    @Override
    public synchronized void set(String path, Object value)
    {
            super.set(path, value);
    }
}
