/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.tengel.time.structures.TimePlayer;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Tim
 */
public class TimeBankHandler implements Listener {
    final private Time plugin;
    
    public TimeBankHandler(Time plugin){
        this.plugin = plugin;
    }
    
    /**
     * Scans regions based on location to see if provided location
     * is part of a bank
     * 
     * @param loc
     * @return 
     */
    public boolean isBank(Location loc){
        System.out.println("CHECKING IF BANK");
        Set<String> regions = plugin.getRegionControl().getRegions(loc).keySet();
        for (String region : regions){
            if (region.contains("bank_"))
                return true;
        }
        return false;
            
    }
    
    @EventHandler
    public void onBankInteract(PlayerInteractEvent event){
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            Block b = event.getClickedBlock();
            if (isBank(b.getLocation())){
                System.out.println("LOOKS LIKE ITS A BANK");
                Player p = event.getPlayer();
                TimePlayer tp = plugin.getPlayer(p.getName());
                if (b.getType().equals(Material.CHEST)){
                    Inventory depot = tp.getPlayerBank().getDepot();
                    p.openInventory(depot);
                    event.setCancelled(true);
                }
            }
        }
    }
    
//    @EventHandler
//    public void onInventoryClick(InventoryClickEvent event) {
//        //Inventory e_inv = event.getInventory();
//        if (event.getClickedInventory().getName().equalsIgnoreCase("Depot")){
//            Player p = (Player) event.getWhoClicked();
//            TimeBank tb = plugin.getBank(p.getName());
//            ItemStack is_hand = event.getCurrentItem();
//            if (!is_hand.getType().equals(Material.AIR)){
//                //tb.removeItem(is);
//            } else {
//                //event.getCurrentItem();
//            }
//            System.out.println(event.getCurrentItem());
//            event.setCancelled(true);
//        }
//    }
    
    @EventHandler
    public void onDepotClose(InventoryCloseEvent event){
        if (event.getInventory().getName().equalsIgnoreCase("Depot")){
            Player p = (Player) event.getPlayer();
            String pname = p.getName();
            TimeBank tb = plugin.getBank(pname);
            tb.updateDepotItems(event.getInventory());
        }
    }
    
    public void icon_menu(){
        IconMenu menu = new IconMenu("My Fancy Menu", 9, new IconMenu.OptionClickEventHandler() {
            @Override
            public void onOptionClick(IconMenu.OptionClickEvent event) {
                event.getPlayer().sendMessage("You have chosen " + event.getName());
                event.setWillClose(true);
            }
        }, plugin)
        .setOption(3, new ItemStack(Material.APPLE, 1), "Food", "The food is delicious")
        .setOption(4, new ItemStack(Material.IRON_SWORD, 1), "Weapon", "Weapons are for awesome people")
        .setOption(5, new ItemStack(Material.EMERALD, 1), "Money", "Money brings happiness");
    }
}
