/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.profs;

import com.tengel.time.ConfigPlayer;
import com.tengel.time.Time;
import com.tengel.time.structures.TimePlayer;
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
    LANDLORD(9), 
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
    
    public void give(Time plugin, Player p){
        TimePlayer tp = plugin.getPlayer(p.getName());
        if (!tp.getJobs().isEmpty()){
            p.sendMessage(ChatColor.RED + "You already have a profession! Please '/life unemploy' first.");
            return;
        }
        else 
            p.sendMessage(ChatColor.GREEN + "You have now become a " + this.toString().toLowerCase() + "!");
        
        tp.addJob(this);
    }
    
    //public TimeProfession get(Time plugin, Player p){
    //    return plugin.getTimePlayers().getPlayerConfig(p.getName()).getProfession();
    //}
    
    public int getUnemployCost(int zone){
        switch(zone){
            case 0: return 7*24*60*60; // 7 days
            case 1: return 7*24*60*60 *4*3; //12 weeks
            case 2: return 7*24*60*60 *52*10; //10 years 
        }
        return 0;
    }
    
}
