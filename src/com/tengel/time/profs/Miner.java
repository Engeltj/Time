/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.profs;

import com.tengel.time.Config;
import com.tengel.time.Time;
import java.util.ArrayList;
import java.util.Map;
import org.bukkit.Material;

/**
 *
 * @author Tim
 */
public class Miner {
    private final Time plugin;
    private ArrayList blacklist;
    private Map<Material, Integer> earnings;
    
    public Miner(Time plugin){
        this.plugin = plugin;
        loadEarnings();
    }
    
    private void loadEarnings(){
        Config c = new Config(this.plugin,"block_worth.yml");
        for (Material m : Material.values()){
            if (!c.contains(m.name())){
                c.set(m.name(), 0);
                earnings.put(m, 0);
            } else {
                int worth = c.getInt(m.name());
                earnings.put(m, worth);
            }
        }
        
    }
    
    public ArrayList getMinerBlacklist(){
        Material.values();
        if (blacklist == null){
            blacklist = new ArrayList();
            Config c = new Config(this.plugin,"config.yml");
            String bl = c.getString("blocks.miner_blacklist");
            if (bl != null){
                String [] bl_list = bl.split(",");
                for (String index : bl_list){
                    Material m = Material.getMaterial(index);
                    blacklist.add(m);
                }
            }
        }
        return blacklist;
    }
    
    public int getSkillEarned(Material m){
        return earnings.get(m);
    }
}
