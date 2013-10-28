/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Tim
 */
public class TimeSigns extends YamlConfiguration {
    private Time plugin;
    private Player player;
    private File configFile;
    
    public TimeSigns(Time plugin, Player player){
        super();
        this.plugin = plugin;
        this.player = player;
        
        this.configFile = new File(plugin.getDataFolder() + "\\signs.yml").getAbsoluteFile();        
        if (!this.configFile.exists()){
            try {
                this.configFile.createNewFile();
            } catch (Exception e){
                plugin.sendConsole("Error creating signs.yml");
            }
        } else
            try{
                super.load(this.configFile);
            }catch (Exception e){}
    }
    
    public void create(SignChangeEvent event){
        Block b = event.getBlock();
        Location l = b.getLocation();
        String path = Integer.toString(l.getBlockX())+","+Integer.toString(l.getBlockY())+","+Integer.toString(l.getBlockZ());
        this.set(path, event.getLine(0));
        save();
    }
    
    public void dropSign(Location location) {
        location.getBlock().setType(Material.AIR);
        location.getWorld().dropItemNaturally(location, new ItemStack(Material.SIGN, 1));
    }
    
    public boolean checkValidItem(String itemName){
        for (Material mat : Material.values()){
            final int id = mat.getId();
            if (itemName.equalsIgnoreCase(mat.name())){
                return true;
            }
        }
        return false;
    }
    
    public Material getItemMaterial(String id_or_name){
        int blockId = -1;
        try {
            blockId = Integer.parseInt(id_or_name);
        } catch (Exception e){}
        for (Material mat : Material.values()){
            final int id = mat.getId();
            if ((blockId == id) || (id_or_name.equalsIgnoreCase(mat.name()))){
                return mat;
            }
        }
        return null;
    }
    
    public Time getPlugin(){
        return this.plugin;
    }
    
    public Player getPlayer(){
        return this.player;
    }
    
    public File getConfig(){
        return this.configFile;
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
}
