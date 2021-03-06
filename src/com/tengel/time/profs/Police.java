/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.profs;

//import com.tengel.time.TimePlayer;
import com.tengel.time.Time;
import com.tengel.time.structures.TimePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
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
        TimePlayer tp = plugin.getPlayer(villian.getName());
        int zone = tp.getZone();
        int bounty = tp.getBounty();
        if (bounty > 0){
            if (!tp.isJailed()){
                Location loc_jail = plugin.getLocation(zone, "jail");
                if (loc_jail != null){
                    villian.teleport(loc_jail);
                    tp.setJailed(true);
                    villian.sendMessage(ChatColor.RED + "You've been jailed until your bounty expires!");
                    villian.sendMessage(ChatColor.RED + "You may pay off your bounty with " + ChatColor.GREEN + ChatColor.BOLD + "/life bail" + 
                                            ChatColor.RESET + ChatColor.RED + " if you wish to seek freedom faster!");
                    cop.sendMessage("You've arrested " + ChatColor.RED + villian.getName() + ChatColor.WHITE + 
                                            " and collected a bounty of " + ChatColor.GREEN + tp.getBountyString());
                    plugin.getEconomy().depositPlayer((OfflinePlayer) cop, bounty);
                } else
                    plugin.sendConsole("No jail found for " + tp.getName());
            } else
                cop.sendMessage("This villian is already jailed");
            
        }
    }
}
