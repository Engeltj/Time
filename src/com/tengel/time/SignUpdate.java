package com.tengel.time;

import org.bukkit.Location;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tim
 */
public class SignUpdate implements Runnable {
    private TimeSigns ts;
    public SignUpdate(TimeSigns ts){
        this.ts = ts;
    }
    
    public void updateSign(Location loc){
        
    }
    
    public void run() {
        ts.getConfig();
    }
    
}
