/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import net.milkbowl.vault.item.Items;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
    private PlayerConfig configFile;
    
    public TimeCommands(Time plugin, PlayerConfig configFile) {
        this.plugin = plugin;
        this.configFile = configFile;
    }
    
    
    
}
