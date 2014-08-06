/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.profs;

//import com.tengel.time.TimePlayer;
import com.tengel.time.Time;
import com.tengel.time.structures.TimePlayer;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;


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
            p.sendMessage(ChatColor.RED + "You already have a profession! Please " + ChatColor.BOLD + "/life unemploy" + ChatColor.RESET + ChatColor.RED + " first.");
            return;
        } else {
            tp.addJob(this);
            this.jobOrientation(p);
            p.sendMessage(ChatColor.GREEN + "You have now become a " + this.toString().toLowerCase() + "!");
        }
    }
    
    public void jobOrientation(Player p){
        p.setGameMode(GameMode.SURVIVAL);
        if (this.equals(TimeProfession.OFFICER)){
            PlayerInventory pi = p.getInventory();
            if (!pi.getItem(0).getType().equals(Material.STICK) || !pi.getItem(0).getItemMeta().getDisplayName().equals("Baton")){
                ItemStack is = new ItemStack(Material.STICK, 1);
                ItemStack is_backup = pi.getItem(0).clone();
                ItemMeta im = is.getItemMeta();
                im.setDisplayName("Baton");
                ArrayList<String> lore = new ArrayList();
                lore.add("Arrests bountied players");
                im.setLore(lore); 
                is.setItemMeta(im);
                pi.setItem(0, is);
                pi.addItem(is_backup);
            }
        }
    }
    //public TimeProfession get(Time plugin, Player p){
    //    return plugin.getTimePlayers().getPlayerConfig(p.getName()).getProfession();
    //}
    
    public static int getUnemployCost(int zone){
        switch(zone){
            case 0: return 7*24*60*60; // 7 days
            case 1: return 7*24*60*60 *4*3; //12 weeks
            case 2: return 7*24*60*60 *52*10; //10 years 
        }
        return 0;
    }
    
}
