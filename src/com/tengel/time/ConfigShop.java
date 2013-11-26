/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import java.io.File;

/**
 *
 * @author Tim
 */
public class ConfigShop extends Config {    
    public ConfigShop(Time plugin){
        super(plugin);
        setConfigFile(new File(plugin.getDataFolder() + "\\item_prices.yml").getAbsoluteFile());
        
        if (!getConfigFile().exists()){
            try {
                getConfigFile().createNewFile();
            } catch (Exception e){
                plugin.sendConsole("Error creating item_prices.yml");
            }
        } else
            try{
                load(getConfigFile());
            }catch (Exception e){}
    }
    
    public void updateItem(int id, double cost){
        String item = Integer.toString(id);
        set(item, cost);
        save();
    }
    
    public double getItemPrice(int id){
        String item = Integer.toString(id);
        return getDouble(item);
    }
}
