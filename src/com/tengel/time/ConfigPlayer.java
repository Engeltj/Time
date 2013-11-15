/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.tengel.time.mysql.Homes;
import com.tengel.time.profs.TimeProfession;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 *
 * @author Tim
 */
public class ConfigPlayer extends Config {
    private String playerName;
    private long life_left;
    public boolean flag_jobLeave = false;
    //private boolean
    
    public ConfigPlayer(Time plugin, Player p){
        super(plugin);
        playerName = p.getName();
        
        Connection con = plugin.getSql().getConnection();
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM `players` WHERE name='"+playerName+"';");
            String start = String.valueOf(System.currentTimeMillis()/1000);
            if (!rs.first())
                st.executeUpdate("INSERT INTO `players` (name, start, lastlogin) VALUES ('"+playerName+"', "+start+", "+start+");");
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to setup or create player: "+playerName);
        }
    }
    
    public String getPlayerName(){
        return this.playerName;
    }
    
    public void updateLastOnline(){
        set("lastonline", System.currentTimeMillis()/1000);
        save();
    }
    
    public void setPlayerStart(){
        set("start", System.currentTimeMillis()/1000);
        save();
    }
    
    public void setPlayerTimeZone(int id){
        set("zone", id);
        save();
    }
    
    public void addHome(String home){
        ConfigurationSection section = this.getConfigurationSection("homes");
        if (section == null)
            section = createSection("homes");
        section.set(home, true);
    }
    
    public void removeHome(String home){
        this.set("homes."+home, null);
        save();
    }
    
    public void setJailed(boolean inJail){
        set("jail", inJail);
        save();
    }
    
    public void setProfession(String profession){
        set("profession", profession);
        save();
    }
    
    public void setProfession(TimeProfession tp){
        set("profession", tp.toString());
        save();
    }
    
    public void addSkill(TimeProfession tp, int amount){
        ConfigurationSection section = this.getConfigurationSection("license");
        if (section == null)
            section = createSection("skills");
        int skill = section.getInt(tp.toString());
        this.set("skills."+tp.toString(), amount+skill);
        //DOES NOT SAVE :)
    }
    
    public int getSkill() {
        return this.getInt("skills." + this.getProfession().name());
    }
    
    public boolean getJailed(){
        return getBoolean("jail");
    }
    
    public boolean isJailed(){
        return getJailed();
    }
    
    public TimeProfession getProfession(){
        TimeProfession tp = TimeProfession.UNEMPLOYED;
        String prof = getString("profession");
        if (prof != null)
            tp = TimeProfession.valueOf(prof.toUpperCase());
        return tp;
    }
    
    public int getPlayerTimeZone(){
        return getInt("zone");
    }
    
    public int getBounty(){
        return getInt("bounty");
    }
    
    public String getBountyString(){
        TimeCommands tc = new TimeCommands();
        return tc.convertSecondsToTime(getDouble("bounty"));
    }
    
    public void addBounty(int amount){
        int bounty = getInt("bounty");
        set("bounty", bounty+amount);
        save();
    }
    
    public boolean addLicense(String name, int id){
        ConfigurationSection section = this.getConfigurationSection("license");
        if (section == null)
            section = createSection("license");
        if (section.get(name) == null){
            getPlugin().sendConsole("Adding license: " + name + " of id: " + String.valueOf(id));
            this.set("license."+name, id);
            save();
            return true;
        }
        else
            return false;
    }
    public boolean hasLicense(String name){
        return (this.getString("license."+name) != null);
    }
    
    public double getPlayerAge(){
        double start = 0;
        try {
            start = Double.valueOf(getString("start"));
        } catch (Exception e){
            return 100*24*60*60;
        }
        double age = System.currentTimeMillis()/1000 - start;
        return age;
    }
    
    public void removePlayer(){
        try {
            getConfigFile().delete();
        } catch (Exception e){}
    }
}
