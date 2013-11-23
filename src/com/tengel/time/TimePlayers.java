/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author Tim
 */
public class TimePlayers {
    private Map<String, ConfigPlayer> players;
    private Time plugin;
    
    public TimePlayers(Time plugin){
        players = new HashMap<String, ConfigPlayer>();
        this.plugin = plugin;
    }
    
    public ConfigPlayer addPlayer(Player p){
        if (!isPlayerAdded(p.getName())){
            players.put(p.getName(), new ConfigPlayer(plugin,p));
            return players.get(p.getName());
        }
        return null;
    }
    
    public void removePlayer(String name){
        try {
            players.remove(name);
        }catch (Exception e){}
    }
    
    public void removePlayer(Player p){
        removePlayer(p.getName());
    }
    
    public ConfigPlayer getPlayerConfig(String name){
        ConfigPlayer config = (ConfigPlayer) players.get(name);
        if (config == null){
            Player p = plugin.getServer().getPlayer(name);
            if (p == null){
                plugin.sendConsole("Player not found ..");
            } else{
                config = new ConfigPlayer(plugin, p);
                config.loadPlayer();
            }
        }
        return config;
    }
    
    private boolean isPlayerAdded(String name){
        if (players.get(name) == null)
            return false;
        return true;
    }
    
    public void resetPlayer(Player p){
        String name = p.getName();
        ConfigPlayer config = plugin.getTimePlayers().getPlayerConfig(name);
        p.kickPlayer("You ran out of time! You're entire profile has been reset.");
        for (World world : plugin.getServer().getWorlds()){
            try {
                File f = new File(System.getProperty("user.dir") + "\\" + world.getName() + "\\players\\" + name + ".dat");
                f.delete();
            }catch(Exception e){}
        }

        try{
            FileOutputStream writer = new FileOutputStream(System.getProperty("user.dir") + "\\plugins\\Essentials\\userdata\\" + name.toLowerCase() + ".yml");
            writer.write(0);
            writer.close();
        }
        catch (Exception e){
            plugin.sendConsole(plugin.getPluginName() + "Failed to delete " + "Essentials\\userdata\\" + name.toLowerCase() + ".yml");
        }
        config.removePlayer();
    }
}
