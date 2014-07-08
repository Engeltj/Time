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
public class ConfigItemPrices extends Config {    
    public ConfigItemPrices(Time plugin){
        super(plugin);
        setConfigFile(new File(plugin.getDataFolder() + File.separator + "item_prices.yml").getAbsoluteFile());
        
        if (!getConfigFile().exists()){
            try {
                getConfigFile().createNewFile();
            } catch (Exception e){
                plugin.sendConsole("Error creating item_prices.yml");
            }
        } else
            try{
                load(getConfigFile());
            }catch (Exception ignored){}
    }
    
    public void updateItem(String name, int cost){
        set(name, cost);
    }
    
    public int getItemPrice(String item){
        return getInt(item);
    }
}
