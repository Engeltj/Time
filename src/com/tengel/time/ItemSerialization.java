/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

 
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
 
public class ItemSerialization {
    public static String saveItemStack(ItemStack [] iss) {
        YamlConfiguration config = new YamlConfiguration();
        
        // Save every element in the list
        saveItemStack(iss, config);
        return config.saveToString();
    }
    
    private static void saveItemStack(ItemStack [] iss, ConfigurationSection destination) {
        // Save every element in the list
        int i=0;
        for (ItemStack is : iss){
            // Don't store NULL entries
            if (is != null) {
                destination.set(Integer.toString(i), is);
            }
            i++;
        }
    }
    
    public static ItemStack[] loadItemStack(String data) throws InvalidConfigurationException {
        YamlConfiguration config = new YamlConfiguration();
        
        // Load the string
        config.loadFromString(data);
        return loadItemStack(config);
    }
    
    private static ItemStack[] loadItemStack(ConfigurationSection source) throws InvalidConfigurationException {
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        
        try {
            // Try to parse this inventory
            for (String key : source.getKeys(false)) {
                int number = Integer.parseInt(key);
                
                // Size should always be bigger
                while (stacks.size() <= number) {
                    stacks.add(null);
                }
                
                stacks.set(number, (ItemStack) source.get(key));
            }
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationException("Expected a number.", e);
        }
        
        // Return result
        return stacks.toArray(new ItemStack[0]);
    }
}
