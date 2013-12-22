/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.profs;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.tengel.time.Time;
import com.tengel.time.TimeCommands;
import com.tengel.time.WorldGuardUtil;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tim
 */
public class Builder {
    private final Time plugin;
    private static final String world_build = "Build";
    private final World world;

    public Builder(Time plugin){
        this.plugin = plugin;
        this.world = plugin.getServer().getWorld(world_build);
        if (world == null)
            plugin.sendConsole("Problematic: World '"+world_build+"' appears to not exist! Builder object will encounter to problems");
        
    }
    
    public void commands(String command, CommandSender sender, String [] args){
        DecimalFormat df = new DecimalFormat("#.##");
        int build_id = getPlayerBuildSchematicId(sender.getName());
        ProtectedRegion pr = getPlayerCurrentBuild(sender.getName());
        
        if ((build_id > 0) && (pr == null)){
            pr = createBuild(sender, this.getBuildSchematicName(build_id));
        }
        if ((build_id == 0) && (pr != null)){
            WorldGuardUtil wgu = new WorldGuardUtil(plugin, world);
            wgu.deleteRegion(pr.getId());
        }
        if (pr == null)
            sender.sendMessage(ChatColor.RED + "Visit "+ChatColor.GRAY+"http://depthsonline.com/minecraft/"+ChatColor.RED+" to sign up for a build");
        if (args.length == 1){
            command = "/"+ command + " " + args[0] + " ";
            sender.sendMessage(ChatColor.GRAY + command + "teleport" + ChatColor.GREEN + "  > Teleports you to your construct");
            sender.sendMessage(ChatColor.GRAY + command + "check" + ChatColor.GREEN + "  > Checks the % completion/correctness of your construct");
            sender.sendMessage(ChatColor.GRAY + command + "done" + ChatColor.GREEN + "  > If you're finished your construct, run this");
        } else if (pr == null){
        } else if (args[1].equalsIgnoreCase("teleport")){
            BlockVector bv_s = pr.getMinimumPoint();
            BlockVector bv_e = pr.getMaximumPoint();
            Location loc = new Location(world, (bv_s.getX()+bv_e.getX())/2, bv_e.getY()+4, (bv_s.getZ()+bv_e.getZ())/2);
            plugin.getServer().getPlayer(sender.getName()).teleport(loc);
                
        } else if (args[1].equalsIgnoreCase("check")){
            double progress = checkPlayerBuildProgress(sender);
            double pay = getPlayerBuildWorth(sender) * progress/100*progress/100;
            sender.sendMessage(ChatColor.GREEN + "Progress: "+ChatColor.GRAY+df.format(progress)+ChatColor.RED+"%");
            sender.sendMessage(ChatColor.GREEN + "If you "+ChatColor.GRAY+"/life job done"+ChatColor.GREEN+" now, you will be awarded "+
                    ChatColor.GRAY+TimeCommands.convertSecondsToTime(pay)+ChatColor.GREEN+" ("+df.format(progress*progress/100)+"% of maximum earnings)");
        } else if (args[1].equalsIgnoreCase("done")){
            double progress = checkPlayerBuildProgress(sender);
            double pay = getPlayerBuildWorth(sender) * progress/100*progress/100;
            setPlayerBuildComplete(sender.getName(), pr);
            EconomyResponse er = plugin.getEconomy().depositPlayer(sender.getName(),pay);
            if (er.transactionSuccess()){
                sender.sendMessage(ChatColor.GREEN + "Congratulations! You have been given " + ChatColor.GRAY + TimeCommands.convertSecondsToTime(pay) +
                        ChatColor.GREEN + " life!");
                sender.sendMessage(ChatColor.GREEN + "You managed to complete " + ChatColor.GRAY + df.format(progress) + ChatColor.GREEN + "% of this construct");
            } else {
                sender.sendMessage(ChatColor.RED + "A problem occurred while trying to reward you with life. Speak to an admin!");
                plugin.sendConsole("Failed rewarding '"+sender.getName()+"' with "+df.format(pay)+" life!");
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
        } catch (Exception ex) {
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
        } catch (Exception ex) {
           plugin.sendConsole("Failed to get schematics in getSchematics()\n" + ex);
        }
        return arr;
    }
    
    public World getWorld(){
        return this.world;
    }
    
    public ProtectedRegion createBuild(CommandSender sender, String schematic){
        WorldGuardUtil wgu = new WorldGuardUtil(plugin, world);
        try {
            Vector vec = wgu.getSchematicDimensions(schematic);
            //plugin.sendConsole(String.valueOf(vec));
            Location start = getNextStart();
            plugin.sendConsole(start.toString() + "createBuild");
            Location end = new Location(world, start.getX()+vec.getX(), start.getBlockY()+vec.getY(), start.getZ()+vec.getZ());
            ProtectedRegion pr = wgu.createBuildRegion(sender.getName(), start, end);
            wgu.pasteFirstLayer(pr, schematic);
            addPlayerBuild(sender.getName(),schematic, start, end);
            return pr;
        } catch (Exception ex) {
            Logger.getLogger(TimeCommands.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public int getPlayerBuildSchematicId(String player){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT schematic_id FROM `job_builder` WHERE player='"+player+"' AND completed=0;");
            if (rs.first())
                return rs.getInt("schematic_id");
        } catch (Exception ex) {
           plugin.sendConsole("Failed to obtain player current build for '"+player+"' in getPlayerBuildSchematicId\n" + ex);
        }
        return 0;
    }
    
    private int getBuildSchematicId(String schematic){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        int id = 0;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT id FROM `schematics` WHERE filename='"+schematic+"';");
            if (rs.first())
                id = rs.getInt("id");
        } catch (Exception ex) {
           plugin.sendConsole("Failed to obtain schematic id for '"+schematic+"' in getBuildSchematicId\n" + ex);
        }
        return id;
    }
    
    private boolean addPlayerBuild(String player, String schematic, Location start, Location end){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int id = getBuildSchematicId(schematic);
            int updated;
            if (getPlayerBuildSchematicName(player) == null){
                String values = String.format("'%s',%d,%.0f,%.0f,%.0f,%.0f,%.0f,%.0f", player, id, start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ());
                updated = st.executeUpdate("INSERT `job_builder` (player,schematic_id,x1,y1,z1,x2,y2,z2) VALUES ("+values+");");
            }else{
                String values = String.format("x1=%.0f,y1=%.0f,z1=%.0f,x2=%.0f,y2=%.0f,z2=%.0f", start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ());
                updated = st.executeUpdate("UPDATE `job_builder` SET "+values+" WHERE player='"+player+"' AND completed=0;");
            }
            return (updated > 0);
        } catch (Exception ex) {
           plugin.sendConsole("Failed to insert/update new build project for '"+player+"' in `job_builder`\n" + ex);
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
        } catch (Exception ex) {
           plugin.sendConsole("Failed to get largest x2 coordinate from `job_builder`\n" + ex);
        }
        return new Location(world, x+1, 65, 0);
    }
    
    public String getBuildSchematicName(int id){
        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT filename FROM `schematics` WHERE id="+id+";");
            if (rs.first()){
                return rs.getString("filename");
            }
        } catch (Exception ex) {
           plugin.sendConsole("Failed to get build schematic name of id '"+id+"'\n" + ex);
        }
        return null;
    }
    
    public String getPlayerBuildSchematicName(String player){
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
        } catch (Exception ex) {
           plugin.sendConsole("Failed to get current player build schematic name for '"+player+"'\n" + ex);
        }
        return null;
    }
    
    public double checkPlayerBuildProgress(CommandSender sender){
        ProtectedRegion pr = getPlayerCurrentBuild(sender.getName());
        WorldGuardUtil wgu = new WorldGuardUtil(plugin, world);
        String schematic = getPlayerBuildSchematicName(sender.getName());
        if (schematic == null){
            plugin.sendConsole("Schematic lookup for player '" + sender.getName() +"' failed.");
            return 0;
        }
        return wgu.compareRegionToSchematic(sender, pr, schematic);
    }
    
    public double getPlayerBuildWorth(CommandSender sender){
        String schematic = getPlayerBuildSchematicName(sender.getName());
        if (schematic == null){
            plugin.sendConsole("Schematic lookup for player '" + sender.getName() +"' failed in getPlayerBuildWorth()");
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
        } catch (Exception ex) {
           plugin.sendConsole("Failed to get worth for schematic '"+schematic+"'\n" + ex);
        }
        return 0;
    }
    
    public ProtectedRegion getPlayerCurrentBuild(String player){
        if (world==null){
            plugin.sendConsole("Failed getting world called '"+world_build+"' in getPlayerCurrentBuild()");
            return null;
        }
        ProtectedRegion pr = plugin.worldGuard.getRegionManager(world).getRegion("buildplot_"+player.toLowerCase());
        if (pr != null && pr.getId().equalsIgnoreCase("buildplot_"+player.toLowerCase()))
            return pr;
        return null;
    }
}
