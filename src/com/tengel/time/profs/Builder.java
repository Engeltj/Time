/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.profs;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.tengel.time.Time;
import com.tengel.time.TimeCommands;
import com.tengel.time.WorldGuardUtil;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Tim
 */
public class Builder {
    private final Time plugin;
    private final TimeProfession prof;
    private static final String world_build = "Build";
    private World world;
    
    public Builder(Time plugin, TimeProfession prof){
        this.plugin = plugin;
        this.prof = prof;
        this.world = plugin.getServer().getWorld(world_build);
        if (world == null)
            plugin.sendConsole("Problematic: World '"+world_build+"' appears to not exist! Builder object will encounter to problems");
        
    }
    
    public void commands(String command, CommandSender sender, String [] args){
        DecimalFormat df = new DecimalFormat("#.##");
        if (args.length == 1){
            command = "/"+ command + " " + args[0] + " ";
            sender.sendMessage("\nYour current job is a " + ChatColor.GREEN + prof.toString().toLowerCase());
            sender.sendMessage(ChatColor.GRAY + command + "teleport" + ChatColor.GREEN + "  > Teleports you to your construct");
            sender.sendMessage(ChatColor.GRAY + command + "check" + ChatColor.GREEN + "  > Checks the % completion/correctness of your construct");
            sender.sendMessage(ChatColor.GRAY + command + "done" + ChatColor.GREEN + "  > If you're finished your construct, run this");
        } else if (args[1].equalsIgnoreCase("teleport")){
            ProtectedRegion pr = getPlayerRegion(sender.getName());
            if (pr == null)
                sender.sendMessage(plugin.getPluginName() + ChatColor.RED + "Visit "+ChatColor.GRAY+"http://depthsonline.com/minecraft/"+ChatColor.RED+" to sign up for a build");
            else {
                BlockVector bv_s = pr.getMinimumPoint();
                BlockVector bv_e = pr.getMaximumPoint();
                Location loc = new Location(world, (bv_s.getX()+bv_e.getX())/2, bv_e.getY()+4, (bv_s.getZ()+bv_e.getZ())/2);
                plugin.getServer().getPlayer(sender.getName()).teleport(loc);
            }
                
        } else if (args[1].equalsIgnoreCase("check")){
            ProtectedRegion pr = getPlayerRegion(sender.getName());
            if (pr == null)
                sender.sendMessage(plugin.getPluginName() + ChatColor.RED + "Visit "+ChatColor.GRAY+"http://depthsonline.com/minecraft/"+ChatColor.RED+" to sign up for a build");
            else {
                double progress = checkBuildProgress(sender);
                double pay = getBuildWorth(sender) * progress/100*progress/100;
                sender.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "Progress: "+ChatColor.GRAY+df.format(progress)+ChatColor.RED+"%");
                sender.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "If you "+ChatColor.GRAY+"/life job done"+ChatColor.GREEN+" now, you will be awarded "+
                        ChatColor.GRAY+TimeCommands.convertSecondsToTime(pay)+ChatColor.GREEN+" ("+df.format(progress*progress/100)+"% of maximum earnings)");
            }
        } else if (args[1].equalsIgnoreCase("done")){
            ProtectedRegion pr = getPlayerRegion(sender.getName());
            if (pr == null)
                sender.sendMessage(plugin.getPluginName() + ChatColor.RED + "Visit "+ChatColor.GRAY+"http://depthsonline.com/minecraft/"+ChatColor.RED+" to sign up for a build");
            else {
                double progress = checkBuildProgress(sender);
                double pay = getBuildWorth(sender) * progress/100*progress/100;
                setPlayerBuildComplete(sender.getName(), pr);
                EconomyResponse er = plugin.getEconomy().depositPlayer(sender.getName(),pay);
                if (er.transactionSuccess()){
                    sender.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "Congratulations! You have been given " + ChatColor.GRAY + TimeCommands.convertSecondsToTime(pay) +
                            ChatColor.GREEN + " life!");
                    sender.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "You managed to complete " + ChatColor.GRAY + df.format(progress) + ChatColor.GREEN + "% of this construct");
                } else {
                    sender.sendMessage(plugin.getPluginName() + ChatColor.RED + "A problem occured while trying to reward you with life. Speak to an admin!");
                    plugin.sendConsole("Failed rewarding '"+sender.getName()+"' with "+df.format(pay)+" life!");
                }
            }
        }
    }
    
    public boolean setPlayerBuildComplete(String player, ProtectedRegion pr){
        WorldGuardUtil wgu = new WorldGuardUtil(plugin, world);
        wgu.deleteRegion(pr.getId());
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int updated = st.executeUpdate("UPDATE `job_builder` SET completed=1 WHERE player='"+player+"';");
            return (updated > 0);
        } catch (SQLException ex) {
           plugin.sendConsole("Failed to get schematics in getSchematics()\n" + ex);
        }
        return false;
    }
    
    public ArrayList<String> getSchematics(){
        ArrayList<String> arr = new ArrayList<String>();
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT filename FROM `schematics`  WHERE approved=1 AND processed=1;");
            while (rs.next())
                arr.add(rs.getString("filename"));
        } catch (SQLException ex) {
           plugin.sendConsole("Failed to get schematics in getSchematics()\n" + ex);
        }
        return arr;
    }
    
    public World getWorld(){
        return this.world;
    }
    
    public ProtectedRegion getPlayerRegion(String player){
        return plugin.worldGuard.getRegionManager(world).getRegion("buildplot_"+player);
    }
    
    public boolean createBuild(CommandSender sender, String schematic){
        WorldGuardUtil wgu = new WorldGuardUtil(plugin, world);
        try {
            Vector vec = wgu.getSchematicDimensions(sender, schematic);
            //plugin.sendConsole(String.valueOf(vec));
            Location start = getNextStart();
            plugin.sendConsole(start.toString() + "createBuild");
            Location end = new Location(world, start.getX()+vec.getX(), start.getBlockY()+vec.getY(), start.getZ()+vec.getZ());
            ProtectedRegion pr = wgu.updateProtectedRegion(sender.getName(), start, end);
            wgu.pasteFirstLayer(sender, pr, schematic);
            addBuild(sender.getName(),schematic, start, end);
        } catch (Exception ex) {
            Logger.getLogger(TimeCommands.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    private int getSchematicId(String schematic){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        int id = 0;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT id FROM `schematics` WHERE filename='"+schematic+"';");
            if (rs.first())
                id = rs.getInt("id");
        } catch (SQLException ex) {
           plugin.sendConsole("Failed to obtain schematic id for '"+schematic+"' in getSchematicId\n" + ex);
        }
        return id;
    }
    
    private boolean addBuild(String name, String schematic, Location start, Location end){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        int x = 0;
        try {
            st = con.createStatement();
            int id = getSchematicId(schematic);
            String values = String.format("'%s',%d,%.0f,%.0f,%.0f,%.0f,%.0f,%.0f", name, id, start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ());
            int updated = st.executeUpdate("INSERT INTO `job_builder` (player,schematic_id,x1,y1,z1,x2,y2,z2) VALUES ("+values+");");
            return (updated > 0);
        } catch (SQLException ex) {
           plugin.sendConsole("Failed to insert new build project for '"+name+"' in `job_builder`\n" + ex);
        }
        return false;
    }
    
    private Location getNextStart(){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        int x = 0;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT x2 FROM `job_builder` ORDER BY x2 DESC");
            if (rs.first())
                x = rs.getInt("x2");
        } catch (SQLException ex) {
           plugin.sendConsole("Failed to get largest x2 coordinate from `job_builder`\n" + ex);
        }
        return new Location(world, x+1, 65, 0);
    }
    
    public String getBuildSchematicName(String player){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT schematic_id FROM `job_builder` WHERE player='"+player+"' AND completed=0;");
            if (rs.first()){
                int id = rs.getInt("schematic_id");
                rs = st.executeQuery("SELECT filename FROM `schematics` WHERE id="+id+";");
                if (rs.first())
                    return rs.getString("filename");
            }
        } catch (SQLException ex) {
           plugin.sendConsole("Failed to get current player build schematic name for '"+player+"'\n" + ex);
        }
        return null;
    }
    
    public double checkBuildProgress(CommandSender sender){
        ProtectedRegion pr = getCurrentBuild(sender.getName());
        WorldGuardUtil wgu = new WorldGuardUtil(plugin, world);
        String schem = getBuildSchematicName(sender.getName());
        if (schem == null){
            plugin.sendConsole("Schematic lookup for player '" + sender.getName() +"' failed.");
            return 0;
        }
        return wgu.compareRegionToSchematic(sender, pr, schem);
    }
    
    public double getBuildWorth(CommandSender sender){
        String schematic = getBuildSchematicName(sender.getName());
        if (schematic == null){
            plugin.sendConsole("Schematic lookup for player '" + sender.getName() +"' failed in getBuildWorth()");
            return 0;
        }
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT worth FROM `schematics` WHERE filename='"+schematic+"';");
            if (rs.first()){
                return rs.getInt("worth");
            }
        } catch (SQLException ex) {
           plugin.sendConsole("Failed to get worth for schematic '"+schematic+"'\n" + ex);
        }
        return 0;
    }
    
    public ProtectedRegion getCurrentBuild(String player){
        if (world==null){
            plugin.sendConsole("Failed getting world called '"+world_build+"' in getCurrentBuild()");
            return null;
        }
        return plugin.worldGuard.getRegionManager(world).getRegion("buildplot_"+player.toLowerCase());
    }
}
