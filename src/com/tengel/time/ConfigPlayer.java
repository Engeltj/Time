/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.tengel.time.profs.TimeProfession;
import java.io.File;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 *
 * @author Tim
 */
public class ConfigPlayer extends Config {
    private String playerName;
    public boolean flag_jobLeave = false;
    //private boolean
    
    public ConfigPlayer(Time plugin, Player player){
        super(plugin);
        playerName = player.getName();
        
        
        File folder = new File(plugin.getDataFolder(), "players");
        if (!folder.exists())
                folder.mkdirs();
        
        setConfigFile(new File(plugin.getDataFolder() + "\\players", player.getName() + ".yml").getAbsoluteFile());
        
        if (!getConfigFile().exists()){
            try {
                getConfigFile().createNewFile();
                setPlayerStart();
            } catch (Exception e){
                plugin.sendConsole("Error creating profile for player: " + player.getName());
            }
        } else
            try{
                load(getConfigFile());
            }catch (Exception e){}
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
        if (prof == null)
            tp = TimeProfession.valueOf(prof);
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
