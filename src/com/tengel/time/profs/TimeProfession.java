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
public enum TimeProfession {
    UNEMPLOYED(0), 
    OFFICER(1), 
    BUILDER(2), 
    MINER(3), 
    STORE_OWNER(4), 
    BLACKSMITH(5), 
    ALCHEMIST(6), 
    LUMBERJACK(7), 
    FARMER(8), 
    LAND_LORD(9), 
    ROBBER(10), 
    DRUG_DEALER(11),
    DOCTOR(12), 
    DENTIST(13);
    
    private final int value;
    private TimeProfession(int value){
        this.value = value;
    }
    public int getValue(){
        return value;
    }
    
    public void give(Time plugin, Player p, String prof){
        if (get(plugin, p) != TimeProfession.UNEMPLOYED){
            p.sendMessage(plugin.getPluginName() + ChatColor.RED + "You already have a profession! Please '/life unemploy' first.");
            return;
        }
        
        ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(p.getName());
        cp.setProfession(prof);
    }
    
    public TimeProfession get(Time plugin, Player p){
        return plugin.getTimePlayers().getPlayerConfig(p.getName()).getProfession();
        //return TimeProfession.valueOf(prof);
    }
    
    public int getUnemployCost(int zone){
        switch(zone){
            case 0: return 7*24*60*60; // 7 days
            case 1: return 7*24*60*60 *4*3; //12 weeks
            case 2: return 7*24*60*60 *52*10; //10 years 
        }
        return 0;
    }
    
    public void take(Time plugin, Player p, int id){
        
    }
    
}
