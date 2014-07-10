/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import org.bukkit.inventory.ItemStack;


/**
 *
 * @author Tim
 */
public class TimeBank implements java.io.Serializable {
    private transient ItemStack [] depot;
    private ArrayList sdepot;
    private long balance;
    private int last_compound;
    private int depot_size = 1024;
    
    public TimeBank(){
        depot = new ItemStack[50];
    }
    
    public TimeBank(int depot_size){
        depot = new ItemStack[50];
        this.depot_size = depot_size;
    }
    
    public void performSerialization(){
        sdepot = new ArrayList();
        
        for (ItemStack is : depot){
            if (is != null)
                sdepot.add(is.serialize());
            else
                sdepot.add(null);
        }
    }
    
    public void performDeserialization(){
        depot = new ItemStack[depot_size];
        for (int i=0;i<sdepot.size();i++){
            Object obj = sdepot.get(i);
            if (obj != null)
                depot[i] = ItemStack.deserialize((Map<String,Object>) obj);
            else
                depot[i] = null;
        }
    }
    
    public boolean setItem(ItemStack is, int slot){
        if (slot > depot_size || slot < 1)
            return false;
        depot[slot-1] = is.clone();
        return true;
    }    
    
    public boolean removeItem(int slot){
        if (slot > depot_size || slot < 1)
            return false;
        depot[slot-1] = null;
        return true;
    }
    
    public ItemStack getItem(int slot){
        if (slot > depot_size || slot < 1)
            return null;
        return depot[slot-1];
    }
    
    public boolean addItem(ItemStack item){
        for (int i=0;i<depot.length;i++){
            if (depot[i] == null){
                depot[i] = item.clone();
                return true;
            }  
        }
        return false;
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
