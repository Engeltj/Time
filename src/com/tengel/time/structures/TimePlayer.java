/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.structures;

import com.tengel.time.Time;
import com.tengel.time.Commands;
import com.tengel.time.profs.TimeProfession;
import com.tengel.time.TimePlayerInventory;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Tim
 */
public class TimePlayer implements IStructure {
    private Player player;
    private HashMap<TimeProfession, Integer> jobs;
    private short zone;
    private long start; //players first appearance
    private int bounty;
    private boolean jailed;
    private List<Short> blockLicenses;
    private Time plugin;
    private TimePlayerInventory spi;
    private boolean adminMode = false;
    private boolean died = false;
    
    public boolean flagConfirm;
    
    public TimePlayer(Time plugin, Player player){
        this.player = player;
        this.plugin = plugin;
    }
    
    public void load(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM `players` WHERE name='"+player.getName()+"';");
            if (rs.first()){
                this.setBounty(rs.getInt("bounty"));
                this.start = rs.getLong("start");
                this.setJailed(rs.getBoolean("jailed"));
                this.setZone(rs.getShort("zone"));
                this.setDied(rs.getBoolean("died"));
                
                this.jobs = new HashMap<TimeProfession, Integer>();
                String db_jobs = rs.getString("jobs");
                String [] jobs = db_jobs.split(",");
                for (String job : jobs){
                    if (job.length()>0){
                        int skill = 0;
                        ResultSet rs_skill = st.executeQuery("SELECT skill FROM `skills` WHERE player='"+player.getName()+"' AND job='"+job+"';");
                        if (rs_skill.first())
                            skill = rs_skill.getInt("skill");
                        this.jobs.put(TimeProfession.valueOf(job), skill);
                    }
                }
                ResultSet inventory = st.executeQuery("SELECT data FROM `inventories` WHERE player='"+player.getName()+"';");
                if (inventory.next()){                    
                    byte[] buf = inventory.getBytes(1);
                    ObjectInputStream objectIn = null;
                    if (buf != null){
                      objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));
                      this.spi = (TimePlayerInventory) objectIn.readObject();
                      this.spi.performDeserialization(player);
                    } else {
                        Bukkit.getServer().getConsoleSender().sendMessage("New");
                        this.spi = new TimePlayerInventory(player);
                    }
                        
                    
                } else {
                    Bukkit.getServer().getConsoleSender().sendMessage("New");
                    this.spi = new TimePlayerInventory(player);
                }
                
                blockLicenses = new ArrayList<Short>();
                ResultSet licenses = st.executeQuery("SELECT * FROM `licenses` WHERE player='"+player.getName()+"';");
                while (licenses.next())
                    blockLicenses.add(licenses.getShort("license"));
            } else
                create();
        } catch (Exception ex) {
            plugin.sendConsole("Failed to create TimePlayer for '"+player.getName()+"', " + ex);
        }
    }
    
    public void save(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            Iterator it = jobs.entrySet().iterator();
            String jobsString = "";
            while (it.hasNext()) {
                Entry pairs = (Entry)it.next();
                ResultSet rs = st.executeQuery("SELECT * FROM `skills` WHERE job='"+pairs.getKey()+"' AND player='"+player.getName()+"';");
                if (rs.first())
                    st.executeUpdate("UPDATE `skills` SET skill="+pairs.getValue()+" WHERE job='"+pairs.getKey()+"' AND player='"+player.getName()+"';");
                else
                    st.executeUpdate("INSERT INTO `skills` SET skill="+pairs.getValue()+",job='"+pairs.getKey()+"',player='"+player.getName()+"';");
                jobsString += pairs.getKey() + ",";
            }
            
            spi.updateInventoryData();
            spi.performSerialization();
            ResultSet rs = st.executeQuery("SELECT * FROM `inventories` WHERE player='"+player.getName()+"';");
            String statement = "";
            if (rs.first())
                statement = "UPDATE `inventories` SET data=? WHERE player='"+player.getName()+"';";
            else
                statement = "INSERT INTO `inventories` (player, data) VALUE ('"+player.getName()+"',?);";
            PreparedStatement pstmt = con.prepareStatement(statement);
            pstmt.setObject(1, spi);
            pstmt.executeUpdate();
            
            for (short license : blockLicenses)
                st.executeUpdate("REPLACE INTO `licenses` SET license="+license+" WHERE player='"+player.getName()+"' AND license="+license+";");
            st.executeUpdate("UPDATE `players` SET life="+plugin.getEconomy().getBalance(player.getName())+",bounty="+bounty+",zone="+zone+
                    ",lastseen="+System.currentTimeMillis()/1000+",jobs='"+jobsString+"',jailed="+jailed+" WHERE name='"+player.getName()+"';");
        } catch (Exception ex) {
            plugin.sendConsole("Failed to update db ford '"+player.getName()+"' in TimePlayer class, " + ex);
        }
    }
    
    private void create(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            st.executeUpdate("INSERT INTO `players` (name, start) VALUES ('"+player.getName()+"', "+System.currentTimeMillis()/1000+");");
        } catch (Exception ex) {
            plugin.sendConsole("Failed to create entry for player '"+player.getName()+"' in TimePlayer class, " + ex);
        }
        player.setLevel(1);
        
    }
    
    public void remove(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            st.executeUpdate("DELETE FROM `players` WHERE player='"+player.getName()+"';");
            st.executeUpdate("DELETE FROM `licenses` WHERE player='"+player.getName()+"';");
            st.executeUpdate("DELETE FROM `skills` WHERE player='"+player.getName()+"';");
            st.executeUpdate("DELETE FROM `job_builder` WHERE player='"+player.getName()+"';");
            st.executeUpdate("DELETE FROM `inventories` WHERE player='"+player.getName()+"';");
            st.executeUpdate("UPDATE `homes` SET renter='' WHERE renter='"+player.getName()+"';");
            st.executeUpdate("UPDATE `homes` SET owner='' WHERE owner='"+player.getName()+"';");
        } catch (Exception ex) {
            plugin.sendConsole("Failed remove profile of player "+player.getName()+", "+ ex);
        }
    }
    
    public void outOfTime(){
        player.sendMessage(ChatColor.RED + "You've run out of time!!!");
        player.setHealth(0D);
        player.setMaxHealth(2D);
        player.setLevel(1);
        died = true;
    }
    
    public boolean removeJob(TimeProfession job){
        return (jobs.remove(job)!= null);
    }
    
    public boolean addJob(TimeProfession job){
        if (!hasJob(job))
            return (jobs.put(job,0)!=null);
        return false;
    }
    
    public boolean hasJob(TimeProfession job){
        return (jobs.get(job)!=null);
    }
    
    public boolean hasBlockLicense(int block){
        for (short license : blockLicenses){
            if (license == block)
                return true;
        }
        return false;
    }
    
    public void setZone(short zone){
        this.zone = zone;
    }
    
    public void setAdminMode(boolean mode){
        this.adminMode = mode;
    }
    
    public void setJailed(boolean jailed){
        this.jailed = jailed;
    }
    
    public void setBounty(int amount){
        if (amount < 0) amount = 0;
        bounty = amount;
    }
    
    public void setDied(boolean value){
        died = false;
    }
    
    public void setBalance(int balance){
        plugin.getEconomy().getBalance(player.getName());
    }
    
    public void addBounty(int amount){
        if (amount < 0) amount = 0;
        bounty += amount;
    }
    
    public void addSkill(TimeProfession tp, int amount){
        int skill = this.getJobs().get(tp);
        this.getJobs().put(tp, skill+amount);
    }
    
    public boolean addBlockLicense(int block){
        if (!hasBlockLicense(block))
            return blockLicenses.add((short)block);
        return false;
    }
    
    public Player getPlayer(){
        return player;
    }
    
    public String getName(){
        return player.getName();
    }
    
    public HashMap<TimeProfession, Integer> getJobs(){
        return jobs;
    }
    
    public short getZone(){
        return zone;
    }
    
    public boolean getAdminMode(){
        return adminMode;
    }
    
    public boolean getJailed(){
        return jailed;
    }
    
    public int getBounty(){
        return bounty;
    }
    
    public String getBountyString(){
        return Commands.convertSecondsToTime(bounty);
    }
    
    public long getAge(){
        return System.currentTimeMillis()/1000 - start;
    }
    
    public TimePlayerInventory getPlayerInventory(){
        return spi;
    }
    
    public boolean hasDied(){
        return died;
    }
    
    public void sendMessage(String message){
        player.sendMessage(message);
    }
}
