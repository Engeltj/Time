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
public class ConfigItemStock extends Config {

    public ConfigItemStock(Time plugin) {
        super(plugin);
        setConfigFile(new File(plugin.getDataFolder() + File.separator + "item_stock.yml").getAbsoluteFile());
        
        if (!getConfigFile().exists()){
            try {
                getConfigFile().createNewFile();
            } catch (Exception e){
                plugin.sendConsole("Error creating "+getConfigFile().toString());
            }
        } else
            try{
                load(getConfigFile());
            }catch (Exception ignored){}
    }
    
    public void setStock(String item, int stock){
        set(item, stock);
        save();
    }
    
    public int getStock(String item){
        return getInt(item);
    }
    
    public void addStock(String item, int amount){
        int stock = getInt(item) + amount;
        if (stock < 0)
            stock = 0;
        set(item, stock);
        save();
    }
    
    public void removeStock(String item, int amount){
        addStock(item, amount*-1);
    }
    
}
