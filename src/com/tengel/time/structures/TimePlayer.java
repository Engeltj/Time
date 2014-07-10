/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.structures;

import com.tengel.time.Time;
import com.tengel.time.Commands;
import com.tengel.time.TimeBank;
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
    private String name;
    private HashMap<TimeProfession, Integer> jobs;
    private short zone;
    private long start; //players first appearance
    private int bounty;
    private int reputation;
    private int reputation_gain = 0;
    private boolean jailed;
    private List<Short> blockLicenses;
    private Time plugin;
    private TimePlayerInventory inventory;
    private TimeBank bank;
    private boolean adminMode = false;
    private boolean died;
    
    private boolean loaded = false;
    
    public boolean flagConfirm;
    
    public TimePlayer(Time plugin, String name){
        this.player = plugin.getServer().getPlayer(name);
        this.plugin = plugin;
    }
    
    public boolean load(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM `players` WHERE name='"+name+"';");
            if (rs.first()){
                this.setBounty(rs.getInt("bounty"));
                this.start = rs.getLong("start");
                this.setJailed(rs.getBoolean("jailed"));
                this.setZone(rs.getShort("zone"));
                this.setDied(rs.getBoolean("died"));
                this.setRep(rs.getInt("reputation"));
                this.reputation_gain = rs.getInt("reputation_gain");
                
                byte[] buf = rs.getBytes("inventory");
                if (buf != null){
                  ObjectInputStream objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));
                  this.inventory = (TimePlayerInventory) objectIn.readObject();
                  this.inventory.performDeserialization(player);
                } else {
                    plugin.sendConsole("New inventory");
                    this.inventory = new TimePlayerInventory(player);
                }
                
                buf = rs.getBytes("bank");
                if (buf != null){
                  ObjectInputStream objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));
                  this.bank = (TimeBank) objectIn.readObject();
                  this.bank.performDeserialization();
                } else {
                    plugin.sendConsole("New bank");
                    this.bank = new TimeBank();
                }
                
                this.jobs = new HashMap<TimeProfession, Integer>();
                String db_jobs = rs.getString("jobs");
                String [] jobs = db_jobs.split(",");
                for (String job : jobs){
                    if (job.length()>0){
                        int skill = 0;
                        ResultSet rs_skill = st.executeQuery("SELECT skill FROM `skills` WHERE player='"+name+"' AND job='"+job+"';");
                        if (rs_skill.first())
                            skill = rs_skill.getInt("skill");
                        this.jobs.put(TimeProfession.valueOf(job), skill);
                    }
                }
//                ResultSet inventory = st.executeQuery("SELECT data FROM `inventories` WHERE player='"+name+"';");
//                if (inventory.next()){                    
//                    byte[] buf = inventory.getBytes(1);
//                    ObjectInputStream objectIn = null;
//                    if (buf != null){
//                      objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));
//                      this.spi = (TimePlayerInventory) objectIn.readObject();
//                      this.spi.performDeserialization(player);
//                    } else {
//                        Bukkit.getServer().getConsoleSender().sendMessage("New");
//                        this.spi = new TimePlayerInventory(player);
//                    }
//                        
//                    
//                } else {
//                    Bukkit.getServer().getConsoleSender().sendMessage("New");
//                    this.spi = new TimePlayerInventory(player);
//                }
                
                blockLicenses = new ArrayList<Short>();
                ResultSet licenses = st.executeQuery("SELECT * FROM `licenses` WHERE player='"+name+"';");
                while (licenses.next())
                    blockLicenses.add(licenses.getShort("license"));
            } else
                create();
            loaded = true;
        } catch (Exception ex) {
            plugin.sendConsole("Failed to create TimePlayer for '"+name+"', " + ex);
            loaded = false;
        }
        return loaded;
    }
    
    public boolean save(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            Iterator it = jobs.entrySet().iterator();
            String jobsString = "";
            while (it.hasNext()) {
                Entry pairs = (Entry)it.next();
                ResultSet rs = st.executeQuery("SELECT * FROM `skills` WHERE job='"+pairs.getKey()+"' AND player='"+name+"';");
                if (rs.first())
                    st.executeUpdate("UPDATE `skills` SET skill="+pairs.getValue()+" WHERE job='"+pairs.getKey()+"' AND player='"+name+"';");
                else
                    st.executeUpdate("INSERT INTO `skills` SET skill="+pairs.getValue()+",job='"+pairs.getKey()+"',player='"+name+"';");
                jobsString += pairs.getKey() + ",";
            }
            
//            inventory.updateInventoryData();
//            inventory.performSerialization();
//            ResultSet rs = st.executeQuery("SELECT * FROM `inventories` WHERE player='"+name+"';");
//            String statement = "";
//            if (rs.first())
//                statement = "UPDATE `inventories` SET data=? WHERE player='"+name+"';";
//            else
//                statement = "INSERT INTO `inventories` (player, data) VALUE ('"+name+"',?);";
//            PreparedStatement pstmt = con.prepareStatement(statement);
//            pstmt.setObject(1, inventory);
//            pstmt.executeUpdate();
            
            for (short license : blockLicenses)
                st.executeUpdate("REPLACE INTO `licenses` SET license="+license+" WHERE player='"+name+"' AND license="+license+";");
            
            inventory.updateInventoryData();
            inventory.performSerialization();
            bank.performSerialization();
            PreparedStatement pstmt = con.prepareStatement("UPDATE `players` SET " +
                    "life=?,bounty=?,zone=?,lastseen=?,jobs=?,jailed=?,reputation=?,died=?,inventory=?,bank=?"+
                    " WHERE name='"+name+"';");
            pstmt.setLong(1,(long) plugin.getEconomy().getBalance(name));
            pstmt.setInt(2,bounty);
            pstmt.setInt(3,zone);
            pstmt.setLong(4,System.currentTimeMillis()/1000);
            pstmt.setString(5,jobsString);
            pstmt.setBoolean(6,jailed);
            pstmt.setInt(7,reputation);
            pstmt.setBoolean(8,died);
            pstmt.setObject(9,inventory);
            pstmt.setObject(10,bank);
            pstmt.executeUpdate();
//            st.executeUpdate("UPDATE `players` SET " +
//                    "life="++
//                    ",bounty="+bounty+
//                    ",zone="+zone+
//                    ",lastseen="+System.currentTimeMillis()/1000+
//                    ",jobs='"+jobsString+"'"+
//                    ",jailed="+jailed+
//                    ",reputation="+reputation+
//                    ",died="+died+
//                    " WHERE name='"+name+"';");
            return true;
        } catch (Exception ex) {
            plugin.sendConsole("Failed to update db for '"+name+"' in TimePlayer class, " + ex);
        }
        return false;
    }
    
    private void create(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            st.executeUpdate("INSERT INTO `players` (name, start) VALUES ('"+name+"', "+System.currentTimeMillis()/1000+");");
        } catch (Exception ex) {
            plugin.sendConsole("Failed to create entry for player '"+name+"' in TimePlayer class, " + ex);
        }
        player.setLevel(1);
        
    }
    
    public boolean remove(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            st.executeUpdate("DELETE FROM `players` WHERE player='"+name+"';");
            st.executeUpdate("DELETE FROM `licenses` WHERE player='"+name+"';");
            st.executeUpdate("DELETE FROM `skills` WHERE player='"+name+"';");
            st.executeUpdate("DELETE FROM `job_builder` WHERE player='"+name+"';");
            st.executeUpdate("DELETE FROM `inventories` WHERE player='"+name+"';");
            st.executeUpdate("UPDATE `homes` SET renter='' WHERE renter='"+name+"';");
            st.executeUpdate("UPDATE `homes` SET owner='' WHERE owner='"+name+"';");
            return true;
        } catch (Exception ex) {
            plugin.sendConsole("Failed remove profile of player "+name+", "+ ex);
        }
        return false;
    }
    
    public void outOfTime(){
        died = true;
        player.sendMessage(ChatColor.RED + "You've run out of time!!!");
        player.setHealth(0D);
        player.setMaxHealth(2D);
        player.setLevel(1);
        
    }
    
    public void outOfTimeRestore(){
        player.sendMessage(ChatColor.GREEN+"It looks like you have accumulated 24 hours of time once again! You are no longer crippled");
        player.setMaxHealth(2D);
        died = false;
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
    
    public void setPassword(String password){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            st.executeUpdate("UPDATE `players` SET password='"+plugin.md5(password)+"' WHERE name='"+getName()+"';");
        } catch (Exception ex){
            plugin.sendConsole("Failed to set password for " + getName());
        }
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
        died = value;
    }
    
//    public void setBalance(int balance){
//        plugin.getEconomy().getBalance(name);
//    }
    
    public void setRep(int rep){
        this.reputation = rep;
    }
    
    public void setPlayerBank(TimeBank bank){
        this.bank = bank;
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
    
    public boolean addRep(int rep){
        if (reputation_gain < 500){
            if (rep+reputation_gain > 500)
                rep = 500-reputation_gain;
            reputation += rep;
            reputation_gain += rep;
            return true;
        } else
            return false;
        
    }
    
    public void updatePlayer(Player p){
        this.player = p;
    }
    
    public Player getPlayer(){
        return player;
    }
    
    public String getName(){
        return name;
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
        return inventory;
    }
    
    public TimeBank getPlayerBank(){
        return bank;
    }
    
    public boolean hasDied(){
        return died;
    }
    
    public boolean isLoaded(){
        return loaded;
    }
    
    public void sendMessage(String message){
        player.sendMessage(message);
    }
}
