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
    }
    
}
