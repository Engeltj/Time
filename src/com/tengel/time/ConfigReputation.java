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
public class ConfigReputation extends Config {

    public ConfigReputation(Time plugin) {
        super(plugin);
        setConfigFile(new File(plugin.getDataFolder() + File.separator + "item_reputation.yml").getAbsoluteFile());
        
        if (!getConfigFile().exists()){
            try {
                getConfigFile().createNewFile();
            } catch (Exception e){
                plugin.sendConsole("Error creating item_reputation.yml");
            }
        } else
            try{
                load(getConfigFile());
            }catch (Exception ignored){}
    }
    
    public void verifyItem(String name){
        getItemRep(name);
    }
    
    public int getItemRep(String name){
        int rep = getInt(name);
        if (rep < 1){
            set(name, 1);
            save();
            return 1;
        }else return rep;
    }
    
}
