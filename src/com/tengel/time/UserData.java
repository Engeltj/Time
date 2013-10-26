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
public class UserData {
        private Time time;
        protected Player base;
        private final PlayerConfig config;
        private final File folder;
        
    	protected UserData(Player player, Time time)
	{
		this.base = base;
		this.time = time;
		folder = new File(time.getDataFolder(), "players");
		if (!folder.exists())
			folder.mkdirs();
                
		config = new PlayerConfig(player);
                
	}
}
