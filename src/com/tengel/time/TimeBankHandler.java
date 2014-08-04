/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.tengel.time.structures.TimePlayer;
import java.util.Set;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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
    final private IconMenu bank;
    
    public TimeBankHandler(Time plug){
        this.plugin = plug;
        
        bank = new IconMenu("Time Capsule", 9, new IconMenu.OptionClickEventHandler() {
            @Override
            public void onOptionClick(IconMenu.OptionClickEvent event) {
                String name = event.getPlayer().getName();
                TimePlayer tp = plugin.getPlayer(name);
                String type = event.getName();
                long balance = (long) Math.floor(plugin.getEconomy().getBalance(name));
                TimeBank tb = tp.getPlayerBank();
                int pos = event.getPosition();
                if (pos == 4) {
                    tp.sendMessage(ChatColor.YELLOW + "Capsule:       " + ChatColor.GREEN + Commands.convertSecondsToTime(tb.getBalance()));
                    tp.sendMessage(ChatColor.YELLOW + "Current life:  " + ChatColor.GREEN + Commands.convertSecondsToTime(plugin.getEconomy().getBalance(name)));
                    tp.sendMessage(ChatColor.GRAY   + "Combined:      " + Commands.convertSecondsToTime(tb.getBalance() + plugin.getEconomy().getBalance(name)));
                    return;
                }
                long amount = 0;
                if (type.equals("1 day"))
                    amount = 24*60*60;
                else if (type.equals("1 week"))
                    amount = 7*24*60*60;
                else if (type.equals("1 year"))
                    amount = 365*24*60*60;
                
                if (pos < 4){ //deposit
                    if (amount == 0)
                        amount = balance - 24*60*60;
                    EconomyResponse er = plugin.getEconomy().withdrawPlayer(name, amount);
                    if (er.transactionSuccess()){
                        tb.setBalance(tb.getBalance()+amount);
                        tp.sendMessage(ChatColor.YELLOW + "You've just deposited " + ChatColor.GREEN + event.getName() + ", remaining " + ChatColor.DARK_GREEN +
                                Commands.convertSecondsToTime(plugin.getEconomy().getBalance(name)));
                    } else
                        tp.sendMessage(ChatColor.RED + "Not enough time!");
                } else {
                    if (amount == 0)
                        amount = tb.getBalance();
                    if (tb.getBalance() >= amount){
                        plugin.getEconomy().depositPlayer(name, amount);
                        tb.setBalance(tb.getBalance() - amount);
                        tp.sendMessage(ChatColor.YELLOW + "You've just withdrew " + ChatColor.GREEN + event.getName() + ", your life is now " + ChatColor.DARK_GREEN +
                                Commands.convertSecondsToTime(plugin.getEconomy().getBalance(name)));
                    } else
                        tp.sendMessage(ChatColor.RED + "Not enough time in capsule!");
                }
            }
        }, plugin)
        .setOption(0, new ItemStack(Material.EYE_OF_ENDER, 1), "1 day", "Deposit")
        .setOption(1, new ItemStack(Material.EYE_OF_ENDER, 1), "1 week", "Deposit")
        .setOption(2, new ItemStack(Material.EYE_OF_ENDER, 1), "1 year", "Deposit")
        .setOption(3, new ItemStack(Material.EYE_OF_ENDER, 1), "All (except 1 day)", "Deposit")
        .setOption(4, new ItemStack(Material.SIGN, 1), "Get Balances", "Capsule & Holdings")
        .setOption(8, new ItemStack(Material.EYE_OF_ENDER, 1), "1 day", "Withdraw")
        .setOption(7, new ItemStack(Material.EYE_OF_ENDER, 1), "1 week", "Withdraw")
        .setOption(6, new ItemStack(Material.EYE_OF_ENDER, 1), "1 year", "Withdraw")
        .setOption(5, new ItemStack(Material.EYE_OF_ENDER, 1), "All (empty bank)", "Withdraw");
        
    }
    
    /**
     * Scans regions based on location to see if provided location
     * is part of a bank
     * 
     * @param loc
     * @return 
     */
    public String getBank(Location loc){
        Set<String> regions = plugin.getRegionControl().getRegions(loc).keySet();
        for (String region : regions){
            if (region.contains("bank_"))
                return region;
        }
        return null;
    }
    
    @EventHandler
    public void onBankInteract(PlayerInteractEvent event){
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            Block b = event.getClickedBlock();
            if (getBank(b.getLocation()) != null){
                Player p = event.getPlayer();
                TimePlayer tp = plugin.getPlayer(p.getName());
                if (b.getType().equals(Material.CHEST)){
                    Inventory depot = tp.getPlayerBank().getDepot();
                    p.openInventory(depot);
                    event.setCancelled(true);
                } else if(b.getType().equals(Material.ENDER_CHEST)){
//                    System.out.println("TIME CAPSULE!");
                    bank.open(p);
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
        
    }
}
