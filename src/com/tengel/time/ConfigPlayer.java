/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.tengel.time.profs.TimeProfession;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;

/**
 *
 * @author Tim
 */
public class ConfigPlayer {
    private final String playerName;
    private int player_id = 0;
    private int skill = 0;
    private int bounty = 0;
    private long start_time = 0;
    private final Time plugin;
    public boolean flag_jobLeave = false;
    private List<Integer> licenses;
    
    public ConfigPlayer(Time plugin, Player p){
        this.plugin = plugin;
        playerName = p.getName();
        licenses = new ArrayList<Integer>();
        Connection con = plugin.getSql().getConnection();
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM `players` WHERE name='"+playerName+"';");
            long start = System.currentTimeMillis()/1000;
            if (!rs.first())
                st.executeUpdate("INSERT INTO `players` (name, start, lastseen) VALUES ('"+playerName+"', "+start+", "+start+");");
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to setup or create player: " + playerName + "\n" + ex);
        }
    }
    
    public void loadPlayer(){
        player_id = getId();
        skill = getSkill(getProfession().name());
        bounty = updateBounty();
        licenses = getPlayerLicenses();
        start_time = getStartTime();
        plugin.getServer().getPlayer(playerName).setExp(skill);
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
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM `players` WHERE name='"+playerName+"';");
            if (rs.first())
                id = rs.getInt("id");
            //plugin.sendConsole(playerName + " id: " + id + " " + rs.next());
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to get ID of player " + playerName + "\n" + ex);
        }
        return id;
    }
    
    private List<Integer> getPlayerLicenses(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM licenses WHERE player_id="+player_id+";");
            while (rs.next())
                licenses.add(rs.getInt("license"));
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to get licenses of player " + playerName + "\n" + ex);
        }
        return licenses;
    }
    
    private boolean updateLife(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            double life = plugin.getEconomy().getBalance(playerName);
            int rs = st.executeUpdate("UPDATE `players` SET life="+life+",lastupdate="+System.currentTimeMillis()/1000+" WHERE name='"+playerName+"';");
            return (rs > 0);
        } catch (SQLException ex) {
            plugin.sendConsole("Failed update life of player " + playerName + "\n" + ex);
        }
        return false;
        
    }
    
    public boolean updateLastSeen(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int rs = st.executeUpdate("UPDATE `players` SET lastseen="+System.currentTimeMillis()/1000+" WHERE name='"+this.playerName+"';");
            return (rs > 0);
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to update lastseen of player " + playerName + "\n" + ex);
        }
        return false;
    }
    
    public boolean setPlayerStart(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int rs = st.executeUpdate("UPDATE `players` SET start="+System.currentTimeMillis()/1000+" WHERE name='"+playerName+"';");
            return (rs > 0);
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to set start time of player " + playerName + "\n" + ex);
        }
        return false;
    }
    
    public boolean setPlayerTimeZone(int id){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int rs = st.executeUpdate("UPDATE `players` SET zone="+id+" WHERE name='"+playerName+"';");
            return (rs > 0);
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to set time zone of player " + playerName + "\n" + ex);
        }
        return false;
    }
    
    public boolean setJailed(boolean inJail){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int rs = st.executeUpdate("UPDATE players SET jailed="+inJail+" WHERE name='"+this.playerName+"';");
            return (rs > 0);
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to set jailed state of player " + playerName + "\n" + ex);
        }
        return false;
    }
    
    public boolean setProfession(String profession){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int rs = st.executeUpdate("UPDATE players SET jobs='"+profession+"' WHERE name='"+this.playerName+"';");
            return (rs > 0);
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to set profession of player " + playerName + "\n" + ex);
        }
        return false;
    }
    
    public boolean setProfession(TimeProfession tp){
        return setProfession(tp.name());
    }
    
    private boolean setSkill(String profession, int skill){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int rs = st.executeUpdate("UPDATE `skills` SET value="+skill+" WHERE player_id="+player_id+" AND skill='"+profession+"';");
            return (rs>0);
       } catch (SQLException ex) {
            plugin.sendConsole("Failed to set skill of player " + playerName + "\n" + ex);
        }
        return false;
    }
    
    public boolean updateSkill(TimeProfession tp, int amount){
        Connection con = plugin.getSql().getConnection();
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
            plugin.sendConsole("Failed to update skill of player " + playerName + "\n" + ex);
        }
        return false;
    }
    
    public long getStartTime() {
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();                
            ResultSet rs = st.executeQuery("SELECT start FROM `players` WHERE id="+player_id+";");
            while (rs.next()){
                return rs.getInt("start");
            }
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to get skill of player " + playerName + "\n" + ex);
        }
        return System.currentTimeMillis()/1000;
    }
    
    public int getSkill(String profession) {
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();                
            ResultSet rs = st.executeQuery("SELECT * FROM `skills` WHERE player_id="+player_id+";");
            while (rs.next()){
                if (rs.getString("skill").equalsIgnoreCase(profession)){
                    return rs.getInt("value");
                }
            }
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to get skill of player " + playerName + "\n" + ex);
        }
        return 0;
    }
    
    public int getLastUpdate(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT lastupdate FROM players WHERE name='"+playerName+"';");
            if (rs.next())
                return rs.getInt("lastupdate");
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to get lasted updated for player " + playerName + "\n" + ex);
        }
        return 0;
    }
    
    public boolean getJailed(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT jailed FROM players WHERE name='"+playerName+"';");
            if (rs.next())
                return rs.getBoolean("jailed");
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to get jailed state of player " + playerName + "\n" + ex);
        }
        return false;
    }
    
    public boolean isJailed(){
        return getJailed();
    }
    
    public TimeProfession getProfession(){
        TimeProfession tp = TimeProfession.UNEMPLOYED;
        String prof = null;
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT jobs FROM `players` WHERE name='"+playerName+"';");
            if (rs.next())
                prof = rs.getString("jobs");
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to get profession of player " + playerName + "\n" + ex);
        }
        if (prof != null)
            tp = TimeProfession.valueOf(prof.toUpperCase());
        return tp;
    }
    
    public int getPlayerTimeZone(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT zone FROM `players` WHERE name='"+playerName+"';");
            if (rs.next())
                return rs.getInt("zone");
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to get zone of player " + playerName + "\n" + ex);
        }
        return 0;
    }
    
    private int updateBounty(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT bounty FROM `bounty` WHERE player_id='"+player_id+"';");
            if (rs.next())
                return rs.getInt("bounty");
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to get bounty of player " + playerName + "\n" + ex);
        }
        return 0;
    }
    
    public int getBounty(){
        return bounty;
    }
    
    public String getBountyString(){
        return TimeCommands.convertSecondsToTime(bounty);
    }
    
    public void addBounty(int amount){
        bounty = bounty + amount;
    }
    
    public boolean addLicense(int block_id){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            if (hasLicense(block_id))
                return false;
            int affected = st.executeUpdate("INSERT INTO `licenses` (player_id, license) VALUES ("+player_id+","+block_id+");");
            licenses.add(block_id);
            return (affected > 0);
        } catch (SQLException ex) {
           plugin.sendConsole("Failed to add license to player " + playerName + "\n" + ex);
        }
        return false;
    }
    
    public boolean hasLicense(int block_id){
        return licenses.contains(block_id);
    }
    
    public long getPlayerAge(){
        return System.currentTimeMillis()/1000 - start_time;
    }
    
    public void removePlayer(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            st.executeUpdate("DELETE FROM `players` WHERE player='"+playerName+"';");
            st.executeUpdate("DELETE FROM `licenses` WHERE player_id="+player_id+";");
            st.executeUpdate("DELETE FROM `skills` WHERE player_id="+player_id+";");
            st.executeUpdate("DELETE FROM `job_builder` WHERE player='"+playerName+"';");
            st.executeUpdate("UPDATE `homes` SET renter='' WHERE renter='"+playerName+"';");
            st.executeUpdate("UPDATE `homes` SET owner='' WHERE owner='"+playerName+"';");
        } catch (SQLException ex) {
            plugin.sendConsole("Failed remove profile of player " + playerName + "\n" + ex);
        }
    }
}
