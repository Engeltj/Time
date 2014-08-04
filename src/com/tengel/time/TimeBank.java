/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


/**
 *
 * @author Tim
 */
public class TimeBank implements java.io.Serializable {
    private transient ArrayList<ItemStack> depot_items;
    private ArrayList sdepot;
    private long balance;
    private int last_compound;
    private int depot_size = 1024;
    
    public TimeBank(){
        depot_items = new ArrayList<ItemStack>();
    }
    
    public TimeBank(int depot_size){
        depot_items = new ArrayList<ItemStack>();
        this.depot_size = depot_size;
    }
    
    public void performSerialization(){
        sdepot = new ArrayList();
        
        for (ItemStack is : depot_items){
            if (is != null)
                sdepot.add(is.serialize());
        }
    }
    
    public void performDeserialization(){
        depot_items = new ArrayList<ItemStack>();
        for (int i=0;i<sdepot.size();i++){
            Object obj = sdepot.get(i);
            if (obj != null)
                depot_items.add(ItemStack.deserialize((Map<String,Object>) obj));
        }
    }
    
    public Inventory createDepot(){
       int size = 9*6;
       Inventory depot = Bukkit.createInventory(null, size, "Depot");
       for (ItemStack is : depot_items){
           if (is != null)
            depot.addItem(is);
       }
       return depot;
    }
    
    public Inventory getDepot(){
        return createDepot();
    }
    
    public boolean addItem(ItemStack item){
        if (item != null){
            depot_items.add(item);
            return true;
        }
        return false;
    }
    
    public void updateDepotItems(Inventory inv){
        int i = 0;
        for (ItemStack is: inv.getContents()){
            if (i+1 > depot_items.size())
                depot_items.add(is);
            else
                depot_items.set(i, is);
            i++;
        }
        Iterator it = depot_items.iterator();
        while (it.hasNext()){
            ItemStack is = (ItemStack) it.next();
            if (is == null || is.getType().equals(Material.AIR))
                it.remove();
        }
    }
    
    public long getBalance(){
        return balance;
    }
    
    public int getDepotSize(){
        return depot_size;
    }
    
    public void setDepotSize(int size){
        depot_size = size;
    }
    
    public void setBalance(long balance){
        this.balance = balance;
    }
    
    /**
     * Will compound this banks balance with the interest rate given
     * under the condition that the day # (1-365) is not equal to the
     * current day this is executed
     * 
     * @param rate  The current interest rate in %
     * @return      The amount gained from the compound
     */
    public long compoundInterest(int rate){
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_YEAR);
        if (day != last_compound){
            long before = balance;
            balance += balance * (rate/100L);
            last_compound = day;
            return (balance - before);
        }
        return 0;
    }
}