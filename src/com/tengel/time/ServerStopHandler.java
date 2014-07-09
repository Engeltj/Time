/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

/**
 *
 * @author Tim
 */
public class ServerStopHandler extends Thread {
    final Time plugin;
    public ServerStopHandler(Time plugin){
        this.plugin = plugin;
    }
    
    public void run() {
        plugin.save();
    }
}
