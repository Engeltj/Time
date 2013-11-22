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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 *
 * @author Tim
 */
public class ConfigPlayer extends Config {
    private String playerName;
    //private double life;
    private int player_id = 0;
    private int skill = 0;
    public boolean flag_jobLeave = false;
    private List<Integer> licenses;
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
    
    
    public void loadPlayer(){
        skill = this.getSkill(this.getProfession().name());
        player_id = getId();
        licenses = getPlayerLicenses();
    }
    
    public void savePlayer(){
        updateLastSeen();
        updateLife();
    }
    
    public String getName(){
        return this.playerName;
    }
    
    public int getId(){
        int id=0;
        Connection con = getPlugin().getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT id FROM `players` WHERE name='"+this.playerName+"';");
            if (rs.next())
                id = rs.getInt("id");
        } catch (SQLException ex) {
            Logger.getLogger(Homes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }
    
    private List<Integer> getPlayerLicenses(){
        List<Integer> licenses = null;
        Connection con = getPlugin().getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM licenses WHERE player_id="+player_id+";");
            while (rs.next())
                licenses.add(rs.getInt("license"));
        } catch (SQLException ex) {
            Logger.getLogger(Homes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return licenses;
    }
    
    /*public int getLife(){
        updateLife();
        Connection con = getPlugin().getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT life FROM `players` WHERE name='"+this.playerName+"';");
            if (rs.next())
                return rs.getInt("life");
        } catch (SQLException ex) {
            Logger.getLogger(Homes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }*/
    
    private boolean updateLife(){
        Connection con = getPlugin().getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            //int ltr = (int)System.currentTimeMillis()/1000 - getLastUpdate(); //life to be removed
            double life = getPlugin().getEconomy().getBalance(playerName);// - ltr;
            int rs = st.executeUpdate("UPDATE `players` (life, lastupdate) VALUES ("+(int)life+","+(int)System.currentTimeMillis()/1000+") WHERE name='"+this.playerName+"';");
            return (rs > 0);
        } catch (SQLException ex) {
            Logger.getLogger(Homes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
        
    }
    
    public boolean updateLastSeen(){
        Connection con = getPlugin().getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int rs = st.executeUpdate("UPDATE `players` SET lastseen="+(int)System.currentTimeMillis()/1000+" WHERE name='"+this.playerName+"';");
            return (rs > 0);
        } catch (SQLException ex) {
            Logger.getLogger(Homes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public boolean setPlayerStart(){
        Connection con = getPlugin().getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int rs = st.executeUpdate("UPDATE `players` SET start="+(int)System.currentTimeMillis()/1000+" WHERE name='"+this.playerName+"';");
            return (rs > 0);
        } catch (SQLException ex) {
            Logger.getLogger(Homes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public boolean setPlayerTimeZone(int id){
        Connection con = getPlugin().getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int rs = st.executeUpdate("UPDATE `players` SET zone="+id+" WHERE name='"+this.playerName+"';");
            return (rs > 0);
        } catch (SQLException ex) {
            Logger.getLogger(Homes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public boolean setJailed(boolean inJail){
        Connection con = getPlugin().getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int rs = st.executeUpdate("UPDATE players SET jailed="+inJail+" WHERE name='"+this.playerName+"';");
            return (rs > 0);
        } catch (SQLException ex) {
            Logger.getLogger(Homes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public boolean setProfession(String profession){
        Connection con = getPlugin().getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int rs = st.executeUpdate("UPDATE players SET jobs='"+profession+"' WHERE name='"+this.playerName+"';");
            return (rs > 0);
        } catch (SQLException ex) {
            Logger.getLogger(Homes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public boolean setProfession(TimeProfession tp){
        return setProfession(tp.name());
    }
    
    private boolean setSkill(String profession, int skill){
        Connection con = getPlugin().getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int rs = st.executeUpdate("UPDATE `skills` SET value="+skill+" WHERE player_id="+player_id+" AND skill='"+profession+"';");
            return (rs>0);
       } catch (SQLException ex) {
            Logger.getLogger(Homes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public boolean updateSkill(TimeProfession tp, int amount){
        Connection con = getPlugin().getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int skill = getSkill(tp.name());
            if (setSkill(tp.name(), skill))
                return true;
            
            //if alive at this point, the row pertaining players skill is not yet added
            int updated = st.executeUpdate("INSERT INTO `skills` (player_id, skill, value) VALUES ("+player_id+",'"+tp.name()+"',"+amount+");");
            return (updated > 0);
        } catch (SQLException ex) {
            Logger.getLogger(Homes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public int getSkill(String profession) {
        Connection con = getPlugin().getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();                
            ResultSet rs = st.executeQuery("SELECT * FROM `skills` WHERE player_id="+this.playerName+";");
            while (rs.next()){
                if (rs.getString("skill").equalsIgnoreCase(profession)){
                    return rs.getInt("value");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Homes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
    
    public int getLastUpdate(){
        Connection con = getPlugin().getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT lastupdate FROM players WHERE name='"+this.playerName+"';");
            if (rs.next())
                return rs.getInt("lastupdate");
        } catch (SQLException ex) {
            Logger.getLogger(Homes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
    
    public boolean getJailed(){
        Connection con = getPlugin().getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT jailed FROM players WHERE name='"+this.playerName+"';");
            if (rs.next())
                return rs.getBoolean("jailed");
        } catch (SQLException ex) {
            Logger.getLogger(Homes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
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
    
    public boolean addLicense(int block_id){
        Connection con = getPlugin().getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            if (hasLicense(block_id))
                return false;
            int affected = st.executeUpdate("INSERT INTO `licenses` (player_id, license) VALUES ("+player_id+","+block_id+");");
            licenses.add(block_id);
            return (affected > 0);
        } catch (SQLException ex) {
            Logger.getLogger(Homes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public boolean hasLicense(int block_id){
        for (int license : licenses){
            if (license == block_id)
                return true;
        }
        return false;
    }
    
    public int getPlayerAge(){
        int start=0;
        Connection con = getPlugin().getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT start FROM `players` WHERE id="+player_id+";");
            if (rs.next())
                start = rs.getInt("start");
        } catch (SQLException ex) {
            Logger.getLogger(Homes.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (start == 0) return 0;
        int age = (int) System.currentTimeMillis()/1000 - start;
        return age;
    }
    
    public void removePlayer(){
        Connection con = getPlugin().getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            st.executeUpdate("DELETE FROM `players` WHERE player='"+playerName+"';");
            st.executeUpdate("DELETE FROM `licenses` WHERE player_id="+player_id+";");
            st.executeUpdate("DELETE FROM `skills` WHERE player_id="+player_id+";");
            st.executeUpdate("DELETE FROM `prof_builder` WHERE player='"+playerName+"';");
            st.executeUpdate("UPDATE `homes` SET renter='' WHERE renter='"+playerName+"';");
            st.executeUpdate("UPDATE `homes` SET owner='' WHERE owner='"+playerName+"';");
        } catch (SQLException ex) {
            Logger.getLogger(Homes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
