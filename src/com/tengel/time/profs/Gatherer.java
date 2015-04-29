/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.profs;

import com.tengel.time.Config;
import com.tengel.time.Time;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Tim
 */
public class Gatherer {
    private final Time plugin;
    private ArrayList blacklist;
    private Map<Material, Integer> earnings;
    private Config blocks_worth;
    private final TimeProfession prof;
    
    public Gatherer(Time plugin, TimeProfession prof){
        this.plugin = plugin;
        this.prof = prof;
        loadEarnings();
    }
    
    public int getBlockWorth(Block block){
        Material m = block.getType();
        return earnings.get(m);
    }
    
    private void loadEarnings(){
        earnings = new HashMap<Material, Integer>();
        if (prof == TimeProfession.MINER)
            blocks_worth = new Config(this.plugin,"exp_miner.yml");
        else if (prof == TimeProfession.FARMER)
            blocks_worth = new Config(this.plugin,"exp_farmer.yml");
        for (Material m : Material.values()){
            if (!blocks_worth.contains(m.name())){
                blocks_worth.set(m.name(), 0);
                blocks_worth.save();
                earnings.put(m, 0);
            } else {
                int worth = blocks_worth.getInt(m.name());
                earnings.put(m, worth);
            }
        }
    }
    
    public int getSkillEarned(Material m){
        return earnings.get(m);
    }
}
