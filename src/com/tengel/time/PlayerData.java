/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import java.io.File;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

/**
 *
 * @author Tim
 */
public class PlayerData {
        private Time plugin;
        protected Player base;
        private final PlayerConfig config;
        private final File folder;
        
    	public PlayerData(Player player, Time plugin)
	{
		this.base = base;
		this.plugin = plugin;
		folder = new File(plugin.getDataFolder(), "players");
		if (!folder.exists())
			folder.mkdirs();
                
		config = new PlayerConfig(player, plugin);
                
	}
}
