/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Tim
 */
public class TimeSigns {
    
    //public TimeSigns(){
        
    //}
    
    public void dropSign(Location location) {
        location.getBlock().setType(Material.AIR);
        location.getWorld().dropItemNaturally(location, new ItemStack(Material.SIGN, 1));
    }
}
