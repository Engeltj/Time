/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.structures;

import com.tengel.time.Time;
import com.tengel.time.profs.TimeProfession;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

/**
 *
 * @author Tim
 */
public class TimePlayer implements IStructure{
    private String name;
    private HashMap<TimeProfession, Integer> jobs;
    private short zone;
    private long start; //players first appearance
    private int bounty;
    private boolean jailed;
    private List<Short> blockLicenses;
    private Time plugin;
    
    public boolean flagConfirm;
    
    public TimePlayer(Time plugin, String player){
        name = player;
        this.plugin = plugin;
    }
    
    public void load(){
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
                
                blockLicenses = new ArrayList<Short>();
                ResultSet licenses = st.executeQuery("SELECT * FROM `licenses` WHERE player='"+name+"';");
                while (licenses.next())
                    blockLicenses.add(licenses.getShort("license"));
            } else
                create();
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to create TimePlayer for '"+name+"', " + ex);
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
                ResultSet rs = st.executeQuery("SELECT * FROM `skills` WHERE job='"+pairs.getKey()+"' AND player='"+name+"';");
                if (rs.first())
                    st.executeUpdate("UPDATE `skills` SET skill="+pairs.getValue()+" WHERE job='"+pairs.getKey()+"' AND player='"+name+"';");
                else
                    st.executeUpdate("INSERT INTO `skills` SET skill="+pairs.getValue()+",job='"+pairs.getKey()+"',player='"+name+"';");
                jobsString += pairs.getKey() + ",";
            }
            for (short license : blockLicenses)
                st.executeUpdate("REPLACE INTO `licenses` SET license="+license+" WHERE player='"+name+"' AND license="+license+";");
            st.executeUpdate("UPDATE `players` SET life="+plugin.getEconomy().getBalance(name)+",bounty="+bounty+",zone="+zone+
                    ",lastseen="+System.currentTimeMillis()/1000+",jobs='"+jobsString+"',jailed="+jailed+" WHERE name='"+name+"';");
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to update db for '"+name+"' in TimePlayer class, " + ex);
        }
    }
    
    private void create(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            st.executeUpdate("INSERT INTO `players` (name, start) VALUES ('"+name+"', "+System.currentTimeMillis()/1000+");");
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to create entry for player '"+name+"' in TimePlayer class, " + ex);
        }        
    }
    
    public void remove(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            st.executeUpdate("DELETE FROM `players` WHERE player='"+name+"';");
            st.executeUpdate("DELETE FROM `licenses` WHERE player='"+name+"';");
            st.executeUpdate("DELETE FROM `skills` WHERE player='"+name+"';");
            st.executeUpdate("DELETE FROM `job_builder` WHERE player='"+name+"';");
            st.executeUpdate("UPDATE `homes` SET renter='' WHERE renter='"+name+"';");
            st.executeUpdate("UPDATE `homes` SET owner='' WHERE owner='"+name+"';");
        } catch (SQLException ex) {
            plugin.sendConsole("Failed remove profile of player " + name +", "+ ex);
        }
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
    
    public void setJailed(boolean jailed){
        this.jailed = jailed;
    }
    
    public void setBounty(int amount){
        if (amount < 0) amount = 0;
        bounty = amount;
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
    
    public HashMap<TimeProfession, Integer> getJobs(){
        return jobs;
    }
    
    public short getZone(){
        return zone;
    }
    
    public boolean getJailed(){
        return jailed;
    }
    
    public int getBounty(){
        return bounty;
    }
    
    public long getAge(){
        return System.currentTimeMillis()/1000 - start;
    }
}
