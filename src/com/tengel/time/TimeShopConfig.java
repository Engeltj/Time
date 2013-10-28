/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Tim
 */
public class TimeShopConfig extends YamlConfiguration {
    private File configFile;
    private Time plugin;
    
    public TimeShopConfig(Time plugin){
        super();
        this.configFile = new File(plugin.getDataFolder() + "\\item_prices.yml").getAbsoluteFile();
        this.plugin = plugin;
        
        if (!this.configFile.exists()){
            try {
                this.configFile.createNewFile();
            } catch (Exception e){
                plugin.sendConsole("Error creating item_prices.yml");
            }
        } else
            try{
                super.load(this.configFile);
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
    
    public void save(){
        try{
            save(configFile);
        }
        catch (IOException e) {
            plugin.sendConsole("Failed to correctly save item prices config file");
        }
    }
}
