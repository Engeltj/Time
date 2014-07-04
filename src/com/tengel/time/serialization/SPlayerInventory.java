/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tengel.time.serialization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 *
 * @author Tim
 */
public class SPlayerInventory implements java.io.Serializable {
    private transient Player p;
    private transient ItemStack [] pi_creative = new ItemStack[36];
    private transient ItemStack [] pa_creative = new ItemStack[4]; //player armour
    private transient ItemStack [] pi_survival = new ItemStack[36];
    private transient ItemStack [] pa_survival = new ItemStack[4];
    private GameMode registered_gm = GameMode.SURVIVAL;
    private ArrayList spi_creative;
    private ArrayList spa_creative;
    private ArrayList spi_survival;
    private ArrayList spa_survival;
    
    public SPlayerInventory(Player p){
        System.out.println("We made it here 1..");
        this.p = p;
        if (p.getGameMode() == GameMode.SURVIVAL){
            pi_survival = p.getInventory().getContents().clone();
            pa_survival = p.getInventory().getArmorContents().clone();
        }
        else {
            pi_creative = p.getInventory().getContents().clone();
            pa_creative = p.getInventory().getArmorContents().clone();
        }
    }
    
    public void switchInventory(GameMode gm){
        System.out.println("reg_old: " + registered_gm);
        PlayerInventory pi = p.getInventory();
        if ((gm == GameMode.CREATIVE) && (registered_gm != GameMode.CREATIVE)){
            System.out.println("We made it here 2..");
            pi_survival = pi.getContents();
            pa_survival = pi.getArmorContents();
            pi.setContents(pi_creative);
            pi.setArmorContents(pa_creative);
            registered_gm = GameMode.CREATIVE;
        } else if ((gm == GameMode.SURVIVAL) && (registered_gm != GameMode.SURVIVAL)){
            pi_creative = pi.getContents();
            pa_creative = pi.getArmorContents();
            pi.setContents(pi_survival);
            pi.setArmorContents(pa_survival);
            registered_gm = GameMode.SURVIVAL;
        }  
         System.out.println("reg_new: " + registered_gm);
    }
    
    public void updateInventoryData(){
        if (registered_gm == GameMode.SURVIVAL){
            pi_survival = p.getInventory().getContents().clone();
            pa_survival = p.getInventory().getArmorContents().clone();
        } else {
            pi_creative = p.getInventory().getContents().clone();
            pa_creative = p.getInventory().getArmorContents().clone();
        }
    }
    
    public void performSerialization(){
        spi_creative = new ArrayList();
        spa_creative = new ArrayList();
        spi_survival = new ArrayList();
        spa_survival = new ArrayList();
        for (ItemStack is : pi_creative){
            if (is != null)
                spi_creative.add(is.serialize());
            else
                spi_creative.add(null);
        }
        for (ItemStack is : pa_creative){
            if (is != null)
                spa_creative.add(is.serialize());
            else
                spa_creative.add(null);
        }
        for (ItemStack is : pi_survival){
            if (is != null)
                spi_survival.add(is.serialize());
            else
                spi_survival.add(null);
        }
        for (ItemStack is : pa_survival){
            if (is != null)
                spa_survival.add(is.serialize());
            else
                spa_survival.add(null);
        }
    }
    
    public void performDeserialization(Player p){
        this.p = p;
        pi_creative = new ItemStack[36];
        pa_creative = new ItemStack[4]; //player armour
        pi_survival = new ItemStack[36];
        pa_survival = new ItemStack[4];
        for (int i=0;i<spi_creative.size();i++){
            Object obj = spi_creative.get(i);
            if (obj != null)
                pi_creative[i] = ItemStack.deserialize((Map<String,Object>) obj);
            else
                pi_creative[i] = null;
        }
        for (int i=0;i<spa_creative.size();i++){
            Object obj = spa_creative.get(i);
            if (obj != null)
                pa_creative[i] = ItemStack.deserialize((Map<String,Object>) obj);
            else
                pa_creative[i] = null;
        }
        for (int i=0;i<spi_survival.size();i++){
            Object obj = spi_survival.get(i);
            if (obj != null)
                pi_survival[i] = ItemStack.deserialize((Map<String,Object>) obj);
            else
                pi_survival[i] = null;
        }
        for (int i=0;i<spa_survival.size();i++){
            Object obj = spa_survival.get(i);
            if (obj != null)
                pa_survival[i] = ItemStack.deserialize((Map<String,Object>) obj);
            else
                pa_survival[i] = null;
        }
    }
}
