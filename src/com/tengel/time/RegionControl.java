/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import org.bukkit.event.EventHandler;
import com.mewin.WGRegionEvents.events.RegionEnterEvent;
import org.bukkit.event.Listener;
/**
 *
 * @author Tim
 */
public class RegionControl implements Listener {
    private Time plugin;
    private TimePlayers players;
    
    public RegionControl(Time plugin, TimePlayers players) {
            this.plugin = plugin;
            this.players = players;
    }

    @EventHandler
    public void onRegionEnter(RegionEnterEvent e){
        e.getPlayer().sendMessage("You just entered " + e.getRegion().getId());
    }
}
