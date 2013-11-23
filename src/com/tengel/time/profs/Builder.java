/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.profs;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.tengel.time.Time;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.World;

/**
 *
 * @author Tim
 */
public class Builder {
    private Time plugin;
    private TimeProfession prof;
    
    public Builder(Time plugin, TimeProfession prof){
        this.plugin = plugin;
        this.prof = prof;
    }
    
    public void createBuild(String player, String schematic){
        
    }
    
    public void checkBuild(String player){
        
    }
    
    public ProtectedRegion getCurrentBuild(String player){
        World w = plugin.getServer().getWorld("Build");
        if (w==null){
            plugin.sendConsole("Failed getting world called 'Build' in getCurrentBuild()");
            return null;
        }
        return plugin.worldGuard.getRegionManager(w).getRegion("buildplot_"+player.toLowerCase());
        
        /*Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT schematic_id FROM `job_builder` WHERE player='"+player+"';");
            int id = rs.getInt("schematic_id");
            rs = st.executeQuery("SELECT * FROM `schematics` WHERE id="+id+";");
            String title="";
            String filename="";
            if (rs.next()){
                title=rs.getString("title");
                filename=rs.getString("filename");
            }
        } catch (SQLException ex) {
           plugin.sendConsole("Failed get current build of player " + player + "\n" + ex);
        }*/
    }
}
