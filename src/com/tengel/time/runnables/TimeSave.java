/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.runnables;

import com.tengel.time.Time;

/**
 *
 * @author Tim
 */
public class TimeSave implements Runnable {
    final Time plugin;
    
    public TimeSave(Time plugin){
        this.plugin = plugin;
    }
    public void run() {
        plugin.save();
        plugin.sendConsole("Time plugin saved.");
    }
    
}
