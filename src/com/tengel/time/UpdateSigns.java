package com.tengel.time;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tim
 */
public class UpdateSigns implements Runnable {
    private Time plugin;
    public UpdateSigns(Time plugin){
        this.plugin = plugin;
    }
    
    public void run() {
        File f = plugin.getConfigSigns();
        List valid = new ArrayList();
        try {
            String line;
            BufferedReader br = new BufferedReader(new FileReader(f));
            while ((line = br.readLine()) != null) {
                String [] data = line.split(":");
                String [] coords = data[0].split(",");
                String worldName = data[1].trim();
                Location loc = null;
                for (World world : plugin.getServer().getWorlds()){
                    if (world.getName().equalsIgnoreCase(worldName)){
                        loc = new Location(world, Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
                        Block block = loc.getBlock();
                        if (block == null || ((block.getType() != Material.SIGN_POST) && (block.getType() != Material.SIGN)))
                            continue;
                        Sign s = (Sign) block.getState();
                        if (s.getLine(0).contains("Shop")){
                            String itemName = s.getLine(1);
                            for (Material mat : Material.values()){
                                if (itemName.equalsIgnoreCase(mat.name())){
                                    ConfigShop tsc = new ConfigShop(plugin);
                                    double cost = tsc.getDouble(String.valueOf(mat.getId()));
                                    s.setLine(2, ChatColor.GREEN + Integer.toString((int)cost)+ " mins");
                                    s.update();
                                }
                            }
                        }
                    }
                }
                if (loc != null){
                    valid.add(coords[0]+','+coords[1]+','+coords[2] + ": "+ worldName);
                }
            }
            br.close();
        } catch (Exception ex) {
            plugin.sendConsole("UpdateSigns threw exception: " + ex);
            return;
        }
        if (valid.size() > 0){
            try {
                FileWriter fstream = new FileWriter(f, false);
                BufferedWriter out = new BufferedWriter(fstream);
                for (int i=0;i<valid.size();i++){
                    out.write(valid.get(i).toString() + "\n");
                }
                out.close();
            } catch (Exception ex) {
                plugin.sendConsole("Writing signs caused exception: " + ex);
            }
            
        }
        
    }
    
}
