/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import java.io.File;
import org.bukkit.entity.Player;

/**
 *
 * @author Tim
 */
public class ConfigWanted extends Config{
    public ConfigWanted(Time plugin){
        super(plugin, "wanted.yml");
    }
    
    public void addPlayer(Player player, int bounty){
        this.set(player.getName(), bounty);
        save();
    }
    
    public void removePlayer(Player player){
        this.set(player.getName(), null);
        save();
    }
    
    public int getBounty(Player player){
        return this.getInt(player.getName());
    }
}
