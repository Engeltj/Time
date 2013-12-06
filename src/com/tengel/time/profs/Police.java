/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.profs;

import com.tengel.time.ConfigPlayer;
import com.tengel.time.Time;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Tim
 */
public class Police{
    private final Time plugin;
    
    public Police(Time plugin){
        this.plugin = plugin;
    }
    
    public void arrestPlayer(Player cop, Player villian){
        ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(villian.getName());
        int zone = cp.getPlayerTimeZone();
        int bounty = cp.getBounty();
        if (bounty > 0){
            villian.teleport(plugin.getLocation(zone, "jail"));
            cp.setJailed(true);
            villian.sendMessage(ChatColor.RED + "You've been jailed until your bounty expires!");
            villian.sendMessage(ChatColor.RED + "You may pay off your bounty with " + ChatColor.GREEN + "/life bail" +
                                    ChatColor.RED + " if you wish to seek freedom faster!");
            cop.sendMessage("You've arrested " + ChatColor.RED + villian.getName() + ChatColor.WHITE + 
                                    " and collected a bounty of " + ChatColor.GREEN + cp.getBountyString());
            plugin.getEconomy().depositPlayer(cop.getName(), bounty);
        }
    }
}
