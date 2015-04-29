/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.tengel.time.runnables.UpdatePlayers;
import com.tengel.time.runnables.UpdateSigns;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.tengel.time.mysql.TimeSQL;
import com.tengel.time.profs.Builder;
import com.tengel.time.profs.Gatherer;
import com.tengel.time.profs.Landlord;
import com.tengel.time.profs.TimeProfession;
import com.tengel.time.runnables.TimeMonsterSpawn;
import com.tengel.time.runnables.TimeSave;
import com.tengel.time.structures.Home;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
/**
 *
 * @author Tim
 */
public final class Time extends JavaPlugin {
    private TimePlayerListener playerListener;
    private RegionControl worldGuardListener;
    private UpdatePlayers timeUpdater;
    private HashMap<String, TimePlayer> players;
    private HashMap<String, Home> homes;
    private TimeBankHandler bank_handler;
    private Economy economy;
    private String pluginName;
    public WorldGuardPlugin worldGuard;
    public WorldEditPlugin worldEdit;
    private TimeSQL sql;
    private MobControl mobcontrol;
    private CreativePlots creative_plots;
    
    private TimeSigns shop_signs;
    private ConfigItemPrices shop_prices;
    private ConfigItemStock shop_stock;
    private ConfigReputation item_reputation;
    
    private double interestRate = 0;
    
    public Gatherer prof_miner;
    public Gatherer prof_farmer;
    public Builder prof_builder;
    public Landlord prof_landlord;
    private final Time plugin;
    
    public Time() {
        plugin = this;
    }
    
    @Override
    public void onEnable(){
        players = new HashMap<String, TimePlayer>();
        playerListener = new TimePlayerListener(this);
        worldGuardListener = new RegionControl(this);
        timeUpdater = new UpdatePlayers(this,1);
        bank_handler = new TimeBankHandler(this);
        setupSql();
        PluginManager pm = getServer().getPluginManager();
        creative_plots = new CreativePlots(this);
        
        shop_signs = new TimeSigns(this);
        shop_prices = new ConfigItemPrices(this);
        shop_stock = new ConfigItemStock(this);
        item_reputation = new ConfigReputation(this);
        
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
        
        
        pluginName = "[" + pm.getPlugin("Time").getName() + "] ";
        
        if (this.getServer().getWorld("Time") == null){
            this.sendConsole("World 'Time' does not exist, unloading ..");
            this.onDisable();
            return;
        }
        
        pm.registerEvents(playerListener, this);
        pm.registerEvents(creative_plots, this);
        pm.registerEvents(worldGuardListener, this);
        pm.registerEvents(bank_handler, this);
        //pm.registerEvents(mobcontrol, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, timeUpdater, 0, timeUpdater.getUpdateInterval() * 20);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new UpdateSigns(this), 60*20, 5*60*20);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new TimeSave(this), 10*60*20, 10*60*20);
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                shop_signs.load();
                plugin.loadHomes();
                plugin.loadTimePlayers();
                plugin.setupHealthBar();
                //plugin.loadMonsters();
            }
        });
        getLogger().info("Time by Engeltj has been enabled");
        //processSchematics();
        
        
    }
    
    public void save(){
        if (this.shop_signs != null)
            this.shop_signs.save();
        if (this.item_reputation != null)
            this.item_reputation.save();
        if (this.shop_prices != null)
            this.shop_prices.save();
        if (this.shop_stock != null)
            this.shop_stock.save();
        if (this.homes != null)
            for (String key : homes.keySet())
                homes.get(key).save();
        if (this.players != null)
            for (String key : players.keySet())
                players.get(key).save();
    }
 
    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        save();
        this.shop_prices = null;
        this.item_reputation = null;
        this.shop_stock = null;
        this.shop_signs = null;
        this.mobcontrol = null;
        this.creative_plots = null;
        this.economy = null;
        this.homes = null;
        this.playerListener = null;
        this.players = null;
        this.bank_handler = null;
        this.pluginName = null;
        this.prof_builder = null;
        this.prof_farmer = null;
        this.prof_landlord = null;
        this.prof_miner = null;
        this.sql = null;
        this.timeUpdater = null;
        this.worldEdit = null;
        this.worldGuard = null;
        this.worldGuardListener = null;
        getLogger().info("Time by Engeltj has been disabled");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Commands tc = new Commands(this, sender, cmd, label, args);
        return tc.executeCommand();
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
                } catch (Exception ex) {
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
    
    public void loadMonsters(){
        World w = getServer().getWorld("Time");
        List<Entity> entities = w.getEntities();
        for ( Entity entity : entities){
            if(entity instanceof Monster){
                entity.remove();
            }
        }
        Connection con = getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM `spawns`;");
            while (rs.next()){
                Location loc = new Location(w,rs.getInt("x"),rs.getInt("y"),rs.getInt("z"));
                //TimeMonster monster = new TimeMonster(this,rs.getInt("id"));
                getServer().getScheduler().runTask(this, new TimeMonsterSpawn(this, loc, rs.getString("type")));
                //monster.spawnWithCheck();
                //monsters.put(monster.getUniqueId(), monster);
            }
        } catch (Exception ex) {
            sendConsole("Failed to loadMonsters, " + ex);
        }
    }
    
    public void loadHomes(){
        homes = new HashMap<String, Home>();
        RegionManager mgr = worldGuard.getRegionManager(getServer().getWorld("Time"));
        Map<String, ProtectedRegion> regions = mgr.getRegions();
        Iterator it = regions.entrySet().iterator();
        while (it.hasNext()) {
            Entry pairs = (Entry)it.next();
            String home = pairs.getKey().toString();
            if (home.contains("home_")) 
                addHome(pairs.getKey().toString());
        }
        sendConsole("Homes loaded.");
    }
    
    public void loadTimePlayers(){
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
    
    public TimeBank getBank(String player){
        TimePlayer tp = getPlayer(player);
        if (tp == null){
            tp = new TimePlayer(this, player);
            tp.load();
        }
        return tp.getPlayerBank();
    }
    
    public void setBank(TimeBank bank, String player){
        TimePlayer tp = getPlayer(player);
        if (tp == null){
            tp = new TimePlayer(this, player);
            tp.load();
        }
        tp.setPlayerBank(bank);
        tp.save();
    }
    
    public Map<String, TimePlayer> getPlayers(){
        return players;
    }
    
    public TimePlayer addPlayer(String player){
        TimePlayer tp = new TimePlayer(this, player);
        tp.load();
        players.put(tp.getName(), tp);
        return tp;
    }
    
    public void removePlayer(String name){
        players.remove(name);
    }
    
    public Home getHome(String name){
        return homes.get(name);
    }
    
    public Home getHome(Location loc){
        RegionManager mgr = worldGuard.getRegionManager(loc.getWorld());
        for (ProtectedRegion rg : mgr.getApplicableRegions(loc)){
            Home home = getHome(rg.getId());
            if (home != null)
                return home;
        }
        return null;
    }
    
    public Map<String, Home> getHomesByOwner(String player){
        Map<String, Home> p_homes = new HashMap<String, Home>();
        for (String home : homes.keySet()){
            Home h = homes.get(home);
            if (h.getRenter().equalsIgnoreCase(player))
                p_homes.put(home, h);
        }
        return p_homes;
    }
    
    public Map<String, Home> getHomes(){
        return this.homes;
    }
    
    public void addHome(String name){
        Home home = homes.get(name);
        if (home == null)
            home = new Home(this, name);
        if (home.load())
            homes.put(name, home);
    }
    
    public void addHome(Home home){
        homes.put(home.getName(), home);
    }
    
    public void removeHome(String name){
        homes.remove(name);
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
    
    public CreativePlots getCreativePlots(){
        return creative_plots;
    }
    
    public TimeSigns getShopSigns(){
        return shop_signs;
    }
    
    public ConfigItemPrices getConfigItemPrices(){
        return this.shop_prices;
    }
    
    public ConfigItemStock getConfigItemStock(){
        return this.shop_stock;
    }
    
    public ConfigReputation getConfigReputation(){
        return this.item_reputation;
    }
    
    public Location getLocation(int zone, String type){
        Location loc = null;
        World world = this.getServer().getWorld("Time");
        if (type.equalsIgnoreCase("jail")){
            if (zone == 0){
                Random r = new Random();
                int rand = r.nextInt(5);
                loc = new Location(world, 370D, 66D, 390D+rand*4);
            }
        } else if (type.equalsIgnoreCase("freedom")){
            if (zone == 0)
                loc = new Location(world, 362, 66, 391);
        }
        return loc;
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
    
    public double getInterestRate(){
        if (interestRate == 0){
            Random rand = new Random();
            int d = 50 + rand.nextInt(150);
            interestRate = d / 10000D;
        }
        return interestRate;
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
    
    public String md5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
         } catch (Exception ignored) {}
         return null;
     }
}
