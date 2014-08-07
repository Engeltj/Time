/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tengel.time;

import java.util.ArrayList;
import org.bukkit.GameMode;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 *
 * @author Tim
 */
public class TimePlayerInventory implements java.io.Serializable {
    private transient Player p;
    
    private GameMode registered_gm = GameMode.SURVIVAL;
    private String pi_creative = "";
    private String pa_creative = "";
    private String pi_survival = "";
    private String pa_survival = "";
    
    private transient ArrayList<ItemStack> i_unclaimed; //from house eviction
    private String si_unclaimed = "";
    
    
    public TimePlayerInventory(Player p){
        this.p = p;
        i_unclaimed = new ArrayList();
        if (p.getGameMode() == GameMode.SURVIVAL){
            pi_survival = ItemSerialization.saveItemStack(p.getInventory().getContents());
            pa_survival = ItemSerialization.saveItemStack(p.getInventory().getArmorContents());
        }
        else {
            pi_creative = ItemSerialization.saveItemStack(p.getInventory().getContents());
            pa_creative = ItemSerialization.saveItemStack(p.getInventory().getArmorContents());
        }
        registered_gm = p.getGameMode();
    }
    
    public void switchInventory(GameMode gm){
        PlayerInventory pi = p.getInventory();
        if ((gm == GameMode.CREATIVE) && (registered_gm != GameMode.CREATIVE)){
            pi_survival = ItemSerialization.saveItemStack(p.getInventory().getContents());
            pa_survival = ItemSerialization.saveItemStack(p.getInventory().getArmorContents());
            try {
                ItemStack [] iss_contents = ItemSerialization.loadItemStack(pi_creative);
                ItemStack [] iss_armor = ItemSerialization.loadItemStack(pa_creative);
                pi.setContents(iss_contents);
                pi.setArmorContents(iss_armor);
            } catch (Exception ex) {
                pi_creative = "";
                pa_creative = "";
                pi.clear();
                pi.setArmorContents(new ItemStack[4]); 
            }
            registered_gm = GameMode.CREATIVE;
        } else if ((gm == GameMode.SURVIVAL) && (registered_gm != GameMode.SURVIVAL)){
            pi_creative = ItemSerialization.saveItemStack(p.getInventory().getContents());
            pa_creative = ItemSerialization.saveItemStack(p.getInventory().getArmorContents());
            try {
                ItemStack [] iss_contents = ItemSerialization.loadItemStack(pi_survival);
                ItemStack [] iss_armor = ItemSerialization.loadItemStack(pa_survival);
                pi.setContents(iss_contents);
                pi.setArmorContents(iss_armor);
            } catch (Exception ex) {
                pi_survival = "";
                pa_survival = "";
                pi.clear();
                pi.setArmorContents(new ItemStack[4]); 
            }
            registered_gm = GameMode.SURVIVAL;
        }
    }
    
    public void reloadInventory(){
        PlayerInventory pi = p.getInventory();
        if (registered_gm == GameMode.SURVIVAL){
            try {
                ItemStack [] iss_contents = ItemSerialization.loadItemStack(pi_survival);
                ItemStack [] iss_armor = ItemSerialization.loadItemStack(pa_survival);
                pi.setContents(iss_contents);
                pi.setArmorContents(iss_armor);
            } catch (Exception ignored) {}
        } else {
            try {
                ItemStack [] iss_contents = ItemSerialization.loadItemStack(pi_creative);
                ItemStack [] iss_armor = ItemSerialization.loadItemStack(pa_creative);
                pi.setContents(iss_contents);
                pi.setArmorContents(iss_armor);
            } catch (Exception ignored) {}
        }
        
    }
    
    public void performSerialization(){
        ItemStack [] iss = new ItemStack[i_unclaimed.size()];
        for (int i=0;i<i_unclaimed.size();i++){
            iss[i] = i_unclaimed.get(i);
        }
        si_unclaimed = ItemSerialization.saveItemStack(iss);
        if (registered_gm == GameMode.SURVIVAL){
            pi_survival = ItemSerialization.saveItemStack(p.getInventory().getContents());
            pa_survival = ItemSerialization.saveItemStack(p.getInventory().getArmorContents());
        } else {
            pi_creative = ItemSerialization.saveItemStack(p.getInventory().getContents());
            pa_creative = ItemSerialization.saveItemStack(p.getInventory().getArmorContents());
        }
    }
    
    public void performDeserialization(Player p){
        this.p = p;
        this.i_unclaimed = new ArrayList<ItemStack>();
        try {
            ItemStack [] iss = ItemSerialization.loadItemStack(si_unclaimed);
            for (ItemStack is : iss)
                i_unclaimed.add(is);
        } catch (InvalidConfigurationException ignored) {}
    }
    
    public void addUnclaimed(ItemStack is){
        this.i_unclaimed.add(is);
    }
    
    public ItemStack popUnclaimed(){
        ItemStack is = i_unclaimed.get(0);
        i_unclaimed.remove(0);
        return is;
    }
}
