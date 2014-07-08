package com.tengel.time.runnables;

import com.tengel.time.ConfigItemPrices;
import com.tengel.time.ConfigItemStock;
import com.tengel.time.Time;
import com.tengel.time.TimeSigns;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private final Time plugin;
    public UpdateSigns(Time plugin){
        this.plugin = plugin;
    }
    
    public void run() {
        TimeSigns ts = plugin.getShopSigns();
        Map<String, Sign> signs = ts.getSigns();
        ConfigItemStock cis = plugin.getConfigItemStock();
        ConfigItemPrices cip = plugin.getConfigItemPrices();
        for (String key : signs.keySet()){
            Sign s = signs.get(key);
            if (s.getLine(0).contains("Buy")){
                String item = s.getLine(1);
                ts.setSignPrice(s, cip.getItemPrice(item));
                ts.setSignStock(s, cis.getStock(item));
            }
        }
//        File f = plugin.getConfigSigns();
//        List valid = new ArrayList();
//        try {
//            String line;
//            BufferedReader br = new BufferedReader(new FileReader(f));
//            while ((line = br.readLine()) != null) {
//                String [] data = line.split(":");
//                String [] coords = data[0].split(",");
//                String worldName = data[1].trim();
//                Location loc = null;
//                for (World world : plugin.getServer().getWorlds()){
//                    if (world.getName().equalsIgnoreCase(worldName)){
//                        loc = new Location(world, Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
//                        Block block = loc.getBlock();
//                        if (block == null || ((block.getType() != Material.SIGN_POST) && (block.getType() != Material.SIGN)))
//                            continue;
//                        Sign s = (Sign) block.getState();
//                        if (s.getLine(0).contains("Buy")){
//                            String itemName = s.getLine(1);
//                            for (Material mat : Material.values()){
//                                if (itemName.equalsIgnoreCase(mat.name())){
//                                    ConfigShop tsc = new ConfigShop(plugin);
//                                    double cost = tsc.getDouble(String.valueOf(mat.getId()));
//                                    s.setLine(2, ChatColor.GREEN + Integer.toString((int)cost)+ " mins");
//                                    s.update();
//                                }
//                            }
//                        }
//                    }
//                }
//                if (loc != null){
//                    valid.add(coords[0]+','+coords[1]+','+coords[2] + ": "+ worldName);
//                }
//            }
//            br.close();
//        } catch (Exception ex) {
//            plugin.sendConsole("UpdateSigns threw exception: " + ex);
//            return;
//        }
//        if (valid.size() > 0){
//            try {
//                FileWriter fstream = new FileWriter(f, false);
//                BufferedWriter out = new BufferedWriter(fstream);
//                for (Object aValid : valid) {
//                    out.write(aValid.toString() + "\n");
//                }
//                out.close();
//            } catch (Exception ex) {
//                plugin.sendConsole("Writing signs caused exception: " + ex);
//            }
//            
//        }
        
    }
    
}