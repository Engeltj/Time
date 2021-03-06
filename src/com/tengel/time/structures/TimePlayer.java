/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.structures;

import com.google.common.base.Joiner;
import com.sk89q.worldedit.blocks.BlockType;
import com.tengel.time.Commands;
import com.tengel.time.Time;
import com.tengel.time.TimeBank;
import com.tengel.time.TimePlayerInventory;
import com.tengel.time.profs.TimeProfession;
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
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Tim
 */
public class TimePlayer implements IStructure {
    private Player player;
    private String name;
    private List<TimeProfession> jobs = new ArrayList<TimeProfession>();
    private Map<TimeProfession, Integer> job_skill = new HashMap<TimeProfession, Integer>();
    private short zone;
    private long start; //players first appearance
    private int bounty;
    private int reputation;
    private int reputation_gain = 0;
    private boolean jailed;
    private List<String> blockLicenses = new ArrayList<String>();
    private Time plugin;
    private TimePlayerInventory inventory;
    private TimeBank bank;
    private boolean adminMode = false;
    private boolean died;
    
    private boolean loaded = false;
    
    public boolean flagConfirm;
    private ItemStack confirm_enchant;
    
    public TimePlayer(Time plugin, String name){
        this.player = plugin.getServer().getPlayer(name);
        this.plugin = plugin;
        this.name = name;
    }
    
    private void loadInventory(byte[] buf){
        try {
            ObjectInputStream objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));
            this.inventory = (TimePlayerInventory) objectIn.readObject();
            this.inventory.performDeserialization(player);
        } catch (Exception ex){
            plugin.sendConsole("New inventory");
            this.inventory = new TimePlayerInventory(player);
        }
    }
     
    private void loadBank(byte[] buf){
        try {
           ObjectInputStream objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));
            this.bank = (TimeBank) objectIn.readObject();
            this.bank.performDeserialization();
        } catch (Exception ex){
            plugin.sendConsole("New bank");
            this.bank = new TimeBank();
        }
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
                
                loadInventory(rs.getBytes("inventory"));
                loadBank(rs.getBytes("bank"));
                this.updateLife(rs.getInt("lastseen"));
                
                String jobs = rs.getString("jobs");
                for (String job : jobs.split(",")){
                    try {
                        TimeProfession prof = TimeProfession.valueOf(job);
                        this.jobs.add(prof);
                    } catch (Exception ex){}
                }
                
                rs = st.executeQuery("SELECT skill,job FROM `skills` WHERE player='"+name+"';");
                while (rs.next()){
                    try {
                        TimeProfession prof = TimeProfession.valueOf(rs.getString("job"));
                        int skill = rs.getInt("skill");
                        this.job_skill.put(prof, skill);
                    } catch (IllegalArgumentException ex){}
               }
                
                //blockLicenses = new ArrayList<Short>();
                ResultSet licenses = st.executeQuery("SELECT * FROM `licenses` WHERE player='"+name+"';");
                while (licenses.next())
                    blockLicenses.add(licenses.getString("license"));
            } else
                create();
            loaded = true;
        } catch (Exception ex) {
            plugin.sendConsole("Failed to load TimePlayer for '"+name+"', " + ex);
            ex.printStackTrace();
            loaded = false;
            player.kickPlayer("Failed to load your data, email engeltj@gmail.com for assistance.");
        }
        return loaded;
    }
    
    public boolean save(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            Iterator it = job_skill.entrySet().iterator();
            while (it.hasNext()){
                Entry pairs = (Entry)it.next();
                ResultSet rs = st.executeQuery("SELECT * FROM `skills` WHERE job='"+pairs.getKey()+"' AND player='"+name+"';");
                if (rs.first())
                    st.executeUpdate("UPDATE `skills` SET skill="+pairs.getValue()+" WHERE job='"+pairs.getKey()+"' AND player='"+name+"';");
                else
                    st.executeUpdate("INSERT INTO `skills` SET skill="+pairs.getValue()+",job='"+pairs.getKey()+"',player='"+name+"';");
            }
            
            for (String license : blockLicenses)
                st.executeUpdate("REPLACE INTO `licenses` SET license="+license+" WHERE player='"+name+"' AND license="+license+";");
            if (!this.player.isDead())
                inventory.performSerialization();
            bank.performSerialization();
            PreparedStatement pstmt = con.prepareStatement("UPDATE `players` SET " +
                    "life=?,bounty=?,zone=?,lastseen=?,jobs=?,jailed=?,reputation=?,died=?,inventory=?,bank=?"+
                    " WHERE name='"+name+"';");
            pstmt.setLong(1,(long) plugin.getEconomy().getBalance(name));
            pstmt.setInt(2,bounty);
            pstmt.setInt(3,zone);
            pstmt.setLong(4,System.currentTimeMillis()/1000);
            pstmt.setString(5,Joiner.on(",").join(jobs));
            pstmt.setBoolean(6,jailed);
            pstmt.setInt(7,reputation);
            pstmt.setBoolean(8,died);
            pstmt.setObject(9,inventory);
            pstmt.setObject(10,bank);
            pstmt.executeUpdate();
            return true;
        } catch (Exception ex) {
            plugin.sendConsole("Failed to update db for '"+name+"' in TimePlayer class, " + ex);
            ex.printStackTrace();
        }
        return false;
    }
    
    private void create(){
        player.setGameMode(GameMode.SURVIVAL);
        loadInventory(null);
        loadBank(null);
        
        Connection con = plugin.getSql().getConnection();
        try {
            start = System.currentTimeMillis()/1000;
            PreparedStatement pstmt = con.prepareStatement("INSERT INTO `players` (name, start, inventory, bank) VALUES (?,?,?,?);");
            pstmt.setString(1, name);
            pstmt.setLong(2, start);
            pstmt.setObject(3, inventory);
            pstmt.setObject(4, bank);
            pstmt.executeUpdate();
        } catch (Exception ex) {
            plugin.sendConsole("Failed to create entry for player '"+name+"' in TimePlayer class, " + ex);
        }
        player.setExp(0f);
        player.setLevel(1);
        Location start_loc = new Location(plugin.getServer().getWorld("Time"), 342.5D, 59D, 377D, 180F, 1F);
        player.teleport(start_loc);
        player.getWorld().playSound(start_loc, Sound.BURP, 1, 1);
        plugin.getEconomy().withdrawPlayer(name, plugin.getEconomy().getBalance(name));
        
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                try{
                    player.sendMessage(ChatColor.GREEN+name+", here is 24 hours to begin your journey, your clock will start ticking in 7 days");
                    plugin.getEconomy().depositPlayer(name, 24*60*60);
                }catch(Exception ignored){};
            }
        }, 20*7); //5 seconds (20 ticks/s)
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                try{
                    player.sendMessage(ChatColor.GREEN+"Type " + ChatColor.BOLD+ "/life left" + ChatColor.RESET + ChatColor.GREEN + " to see your remaining life/balance");
                }catch(Exception ignored){};
            }
        }, 20*10); //5 seconds (20 ticks/s)
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
        player.sendMessage(ChatColor.RED + "You've run out of time!!!");
        player.setMaxHealth(2D);
        player.setHealth(0D);
        //player.setLevel(1);
        died = true;
        
    }
    
    public void outOfTimeRestore(){
        player.sendMessage(ChatColor.GREEN + "It looks like you have accumulated 24 hours of time once again! You are no longer crippled");
        player.setMaxHealth(20D);
        died = false;
    }
    
    private void updateLife(int lastseen){
        long interest = bank.compoundInterest(plugin.getInterestRate());
        if (interest > 0)
            sendMessage(ChatColor.YELLOW + "You've earned " + ChatColor.GREEN + Commands.convertSecondsToTime(interest) + ChatColor.YELLOW +
                    " in interest today due to your bank balance!");
        long balance = (long) plugin.getEconomy().getBalance(player);
        long owing = System.currentTimeMillis()/1000 - lastseen;
        if (owing > balance){
            owing -= balance;
            setBalance(0L);
            long bank_balance = bank.getBalance();
            if (owing > bank_balance)
                owing = bank_balance;
            bank.setBalance(bank_balance - owing);
        }
        else
            setBalance(balance - owing);
    }
    
    public boolean removeJob(TimeProfession job){
        return jobs.remove(job);
    }
    
    public boolean addJob(TimeProfession job){
        if (!hasJob(job))
            return (jobs.add(job));
        return false;
    }
    
    public boolean hasJob(TimeProfession job){
        return jobs.contains(job);
    }

    private boolean hasBlockLicense(String blockName){
        for (String license : blockLicenses){
            if (license.equals(blockName))
                return true;
        }
        return false;
    }
    
    public boolean hasBlockLicense(Block block){
        String blockName = block.getType().getData().getName();
        return hasBlockLicense(blockName);
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
    
    public void setBalance(long balance){
        plugin.getEconomy().withdrawPlayer(player, plugin.getEconomy().getBalance(player));
        plugin.getEconomy().depositPlayer(player, balance);
    }
    
    public void setRep(int rep){
        this.reputation = rep;
    }
    
    public void setPlayerBank(TimeBank bank){
        this.bank = bank;
    }
    
    public void addBounty(float amount){
        if (bounty == 0 && amount > 0)
            sendMessage(ChatColor.RED + "You've been added to the wanted list! See " + ChatColor.BOLD + "/life bounty");
        bounty += amount;
        if (bounty < 0)
            bounty = 0;
    }
    
    public void addSkill(TimeProfession tp, int amount){
        int skill = this.job_skill.get(tp);
        this.job_skill.put(tp, skill+amount);
    }
    
    public boolean addBlockLicense(Block block){
        return addBlockLicense(block.getType().getData().getName());
    }

    public boolean addBlockLicense(String blockName){
        if (!hasBlockLicense(blockName))
            return blockLicenses.add(blockName);
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
    
    public List<TimeProfession> getJobs(){
        return jobs;
    }
    
    public int getLevel(){
        return this.player.getLevel();
    }
    
    public short getZone(){
        return zone;
    }
    
    public boolean getAdminMode(){
        return adminMode;
    }
    
    
    
    public int getRep(){
        return this.reputation;
    }
    
    public int getSkill(TimeProfession prof){
        return this.job_skill.get(prof);
    }
    
    public int getBounty(){
        return bounty;
    }
    
    public String getBountyString(){
        return Commands.convertSecondsToTime(bounty);
    }
    
    public long getAge(){
        if (isLoaded())
            return System.currentTimeMillis()/1000 - start;
        else
            return 0;
    }
    
    public float getBalance(){
        return (float) plugin.getEconomy().getBalance(name);
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
    
    public boolean isJailed(){
        return jailed;
    }
    
    public boolean confirmEnchantment(ItemStack is){
        try{
            if (this.confirm_enchant != null && this.confirm_enchant.equals(is))
                return true;
            else {
                this.confirm_enchant = is;
                plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                    public void run() {
                        confirm_enchant = null;
                    }
                }, 20*5); //5 seconds (20 ticks/s)
                return false;
            }
        } catch(Exception ex){
            return false;
        }
    }
    
    public void sendMessage(String message){
        player.sendMessage(message);
    }
}
