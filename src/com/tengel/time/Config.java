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
public class Config extends YamlConfiguration {
    private Time plugin;
    private File configFile;
    
    public Config(Time plugin){
        this.plugin = plugin;
    }
    
    public Config(Time plugin, String filename){
        this.plugin = plugin;
        configFile = new File(plugin.getDataFolder() + "\\"+filename).getAbsoluteFile();
        
        if (!configFile.exists()){
            try {
                configFile.createNewFile();
            } catch (Exception e){
                plugin.sendConsole("Error creating "+filename);
            }
        } else
            try{
                load(configFile);
            }catch (Exception e){}
    }
    
    public boolean setConfigFile(File configFile){
        if (configFile != null){
            this.configFile = configFile;
            return true;
        }
        return false;
    }
    
    public File getConfigFile(){
        return this.configFile;
    }
    
    public Time getPlugin(){
        return this.plugin;
    }
    
    public void save()
    {
        try{
            save(configFile);
        }
        catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    @Override
    public synchronized long getLong(String path)
    {
            return super.getLong(path);
    }
    
    @Override
    public synchronized void set(String path, Object value)
    {
            super.set(path, value);
    }
}
