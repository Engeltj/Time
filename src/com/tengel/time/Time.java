/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.tengel.time.mysql.TimeSQL;
import com.tengel.time.profs.Builder;
import com.tengel.time.profs.Gatherer;
import com.tengel.time.profs.Landlord;
import com.tengel.time.profs.TimeProfession;
import com.tengel.time.structures.TimePlayer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
/**
 *
 * @author Tim
 */
public final class Time extends JavaPlugin {
    private final TimePlayerListener playerListener;
    private final RegionControl worldGuardListener;
    private final UpdatePlayers timeUpdater;
    private HashMap<String, TimePlayer> players;
    private Economy economy = null;
    private String pluginName;
    //private final TimePlayers players;
    private File configSigns;
    public WorldGuardPlugin worldGuard;
    public WorldEditPlugin worldEdit;
    private TimeSQL sql;
    private MobControl mobcontrol;
    
    public Gatherer prof_miner;
    public Gatherer prof_farmer;
    public Builder prof_builder;
    public Landlord prof_landlord;
    
    public Time() {
        players = new HashMap<String, TimePlayer>();
        playerListener = new TimePlayerListener(this);
        worldGuardListener = new RegionControl(this);
        timeUpdater = new UpdatePlayers(this,1);
    }
    
    @Override
    public void onEnable(){
        setupSql();
        PluginManager pm = getServer().getPluginManager();
        worldGuard = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
        worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
        prof_miner = new Gatherer(this, TimeProfession.MINER);
        prof_farmer = new Gatherer(this, TimeProfession.FARMER);
        prof_builder = new Builder(this);
        prof_landlord = new Landlord(this);
        
        mobcontrol = new MobControl(this, getServer().getWorld("Time"));
        
        if (worldGuard == null || !(worldGuard instanceof WorldGuardPlugin)) {
            getLogger().info(String.format("[%s] - Disabled due to no instance of WorldGuard found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupEconomy() ) {
            getLogger().info(String.format("[%s] - Disabled due to no instance of Vault found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        populateTimePlayers();
        setupHealthBar();
        
        pluginName = "[" + pm.getPlugin("Time").getName() + "] ";
        
        pm.registerEvents(this.playerListener, this);
        pm.registerEvents(this.worldGuardListener, this);
        pm.registerEvents(mobcontrol, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, timeUpdater, 0, timeUpdater.getUpdateInterval() * 20);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new UpdateSigns(this), 60, 1800 * 20);
        
        
        getLogger().info("Time by Engeltj has been enabled");
        processSchematics();
        
    }
 
    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        getLogger().info("Time by Engeltj has been disabled");
        
    }
    
    public void processSchematics(){
        final Time plugin = this;
        Runnable usetJobLeave = new BukkitRunnable() {
            public void run() {
                Connection con = plugin.getSql().getConnection();
                Statement st;
                try {
                    st = con.createStatement();
                    ResultSet rs = st.executeQuery("SELECT filename FROM `schematics` WHERE processed=0 AND approved=1;");
                    int i = 0;
                    ArrayList<String> files = new ArrayList<String>();
                    while (rs.next()){
                        files.add(rs.getString("filename"));
                        InputStream in = new URL("http://depthsonline.com/minecraft/schematics/"+rs.getString("filename")).openStream();
                        Files.copy(in, Paths.get(plugin.getDataFolder().toString(), "schematics", rs.getString("filename")));
                        i++;
                    }
                    for (String file : files){
                        st.executeUpdate("UPDATE `schematics` SET processed=1 WHERE filename='"+file+"';");
                    }
                    if (i>0)
                        plugin.sendConsole("Processed " + i + " schematic(s).");
                } catch (IOException ex) {
                    plugin.sendConsole("Failed to processSchematics, IOException\n" + ex);
                } catch (SQLException ex) {
                    plugin.sendConsole("Failed to processSchematics, SQLException\n" + ex);
                }
            }
        };
        getServer().getScheduler().scheduleSyncRepeatingTask(this, usetJobLeave, 5, 20 * 20);
    }
    
    public void setupHealthBar(){
        unsetHealthBar();
        Objective below,list;
        Scoreboard sb = getServer().getScoreboardManager().getMainScoreboard();
        for (Objective obj: sb.getObjectives()){
            sendConsole(obj.getName());
        }
        below = sb.registerNewObjective("obj_below", "dummy");
        //below.setDisplayName("Lvl - "+ ChatColor.RED + "❤❤❤❤❤❤❤❤")
        below.setDisplayName("/ 100");
        below.setDisplaySlot(DisplaySlot.BELOW_NAME);
        
        list = sb.registerNewObjective("obj_list", "dummy");
        list.setDisplayName("2");
        list.setDisplaySlot(DisplaySlot.PLAYER_LIST);
    }
    
    private void unsetHealthBar(){
        Scoreboard sb = getServer().getScoreboardManager().getMainScoreboard();
        if (sb.getObjective(DisplaySlot.BELOW_NAME) != null)
            sb.getObjective(DisplaySlot.BELOW_NAME).unregister();
        if (sb.getObjective("obj_below") != null)
            sb.getObjective("obj_below").unregister();
        if (sb.getObjective("obj_list") != null)
            sb.getObjective("obj_list").unregister();
  }
    
    public void setupSql(){
        Config c = new Config(this, "config.yml");
        ConfigurationSection section = c.getConfigurationSection("sql");
        if (section == null)
            section = c.createSection("sql");
        String host = c.getString("sql.host");
        String db = c.getString("sql.db");
        String user = c.getString("sql.user");
        String pass = c.getString("sql.pass");
        
        if (host == null){
            host = "127.0.0.1";
            section.set("host", host);
        }
        if (db == null){
            db = "minecraft";
            section.set("db", db);
        }
        if (user == null){
            user = "root";
            section.set("user", user);
        }
        if (pass == null){
            pass = "";
            section.set("pass", pass);
        }
        c.save();
        sql = new TimeSQL(this, host, db, user, pass);
    }
    
    public TimeSQL getSql(){
        if (sql == null)
            setupSql();
        return this.sql;
    }
    
    public void sendConsole(String message){
        getLogger().info(message);
    }
    
    public void populateTimePlayers(){
        for (Player player: getServer().getOnlinePlayers())
            addPlayer(player.getName());
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
    
    public Economy getEconomy(){
        return this.economy;
    }
    
    
    public TimePlayer getPlayer(String name){
        return players.get(name);
    }
    
    public void addPlayer(String name){
        TimePlayer tp = new TimePlayer(this, name);
        tp.load();
        players.put(name, tp);
    }
    
    public void removePlayer(String name){
        players.remove(name);
    }
    
    public TimePlayerListener getPlayerListener(){
        return this.playerListener;
    }
    
    public RegionControl getRegionControl(){
        return this.worldGuardListener;
    }
    
    public String getPluginName(){
        return this.pluginName;
    }
    
    public MobControl getMobControl(){
        return mobcontrol;
    }
    
    public File getConfigSigns(){
        if (configSigns == null)
            configSigns = new File(this.getDataFolder() + "\\signs.yml");
        if (!this.configSigns.exists()){
            try {
                this.configSigns.createNewFile();
            } catch (Exception e){
                this.sendConsole("Error creating signs.yml");
            }
        }
        return this.configSigns;
    }
    
    public Location getLocation(int zone, String type){
        Location loc = null;
        World world = this.getServer().getWorld("Time");
        if (type.equalsIgnoreCase("jail")){
            
            if (zone == 0){
                Random r = new Random(4);
                int rand = r.nextInt();
                loc = new Location(world, 370, 66, 390+rand*4);
            }
        } else if (type.equalsIgnoreCase("freedom")){
            if (zone == 0)
                loc = new Location(world, 362, 66, 391);
        }
        return loc;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        TimeCommands tc = new TimeCommands(this, sender, cmd, label, args);
        return tc.executeCommand();
    }
    
    public Material getItemMaterial(String id_or_name){
        int blockId = -1;
        try {
            blockId = Integer.parseInt(id_or_name);
        } catch (Exception ignored){}
        for (Material mat : Material.values()){
            final int id = mat.getId();
            if ((blockId == id) || (id_or_name.equalsIgnoreCase(mat.name()))){
                return mat;
            }
        }
        return null;
    }
    
    public String intToString(int num, int digits) {
        assert digits > 0 : "Invalid number of digits";
        // create variable length array of zeros
        char[] zeros = new char[digits];
        Arrays.fill(zeros, '0');
        // format number as String
        DecimalFormat df = new DecimalFormat(String.valueOf(zeros));
        return df.format(num);
    }
}
