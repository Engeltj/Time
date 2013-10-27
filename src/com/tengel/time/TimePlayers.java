/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

/**
 *
 * @author Tim
 */
public class TimePlayers {
    private HashMap players;
    private Time plugin;
    
    public TimePlayers(Time plugin){
        players = new HashMap();
        this.plugin = plugin;
    }
    
    public void addPlayer(Player p){
        if (!isPlayerAdded(p.getName())){
            players.put(p.getName(), new PlayerConfig(p,plugin));
        }
    }
    
    public void removePlayer(String name){
        try {
            players.remove(name);
        }catch (Exception e){}
    }
    
    public void removePlayer(Player p){
        try {
            players.remove(p.getName());
        }catch (Exception e){}
    }
    
    public PlayerConfig getPlayerConfig(String name){
        PlayerConfig config = (PlayerConfig) players.get(name);
        return config;
    }
    
    private boolean isPlayerAdded(String name){
        try {
            players.get(name);
        } catch (Exception e){
            return false;
        }
        return true;
    }
}