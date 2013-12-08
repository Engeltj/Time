/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.tengel.time.mysql.Homes;
import com.tengel.time.profs.TimeProfession;
import com.tengel.time.structures.TimePlayer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Tim
 */
public class TimeCommands implements Listener{
    private Time plugin;
    private CommandSender sender;
    private Command cmd;
    private String label;
    private String[] args;
    
    public TimeCommands(){
        
    }
    
    public TimeCommands(Time plugin, CommandSender sender, Command cmd, String label, String[] args) {
        this.sender = sender;
        this.plugin = plugin;
        this.cmd = cmd;
        this.label = label;
        this.args = args;
    }
    
    
    public static String convertSecondsToTime(double seconds){
        double minutes = 0;
        double hours = 0;
        double days = 0;
        double weeks = 0;
        double years = 0;

        if (seconds > 60){
            minutes = Math.floor(seconds/60);
            seconds -= minutes*60;
        }
        if (minutes > 60){
            hours = Math.floor(minutes/60);
            minutes -= hours*60;
        }
        if (hours > 24){
            days = Math.floor(hours/24);
            hours -= days*24;
        }
        if (days > 7){
            weeks = Math.floor(days/7);
            days -= weeks*7;
        }
        if (weeks > 52){
            years = Math.floor(weeks/52);
            weeks -= years*52;
        }

        return String.format("%04.0f·%02.0f·%01.0f·%02.0f·%02.0f·%02.0f", years,weeks,days,hours,minutes,seconds);
        
    }
    
    public boolean executeCommand(){
        String command = cmd.getName();
        if(command.equalsIgnoreCase("ghost") && args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return true;
            }
            // After checking to make sure that the sender is a Player, we can safely case it to one.
            Player s = (Player) sender;

            // Gets the player who shouldn't see the sender.
            Player target = Bukkit.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("Player " + args[0] + " is not online.");
                return true;
            }
            // Hides a given Player (s) from someone (target).
            target.hidePlayer(s);
            return true;
        }
        if (!command.equalsIgnoreCase("life"))
            return false;
        else if (args.length == 0){
            sender.sendMessage(ChatColor.YELLOW + "Time format: YYYY/WW/DD/HH/MM/SS");
            sender.sendMessage(ChatColor.BOLD + "" + ChatColor.GREEN + "Your options are: ");
            sender.sendMessage(ChatColor.GRAY + "/"+command+" age" + ChatColor.GREEN + "  > How long you've played");
            sender.sendMessage(ChatColor.GRAY + "/"+command+" left" + ChatColor.GREEN + "  > How long you have left to live");
            sender.sendMessage(ChatColor.GRAY + "/"+command+" bounty" + ChatColor.GREEN + "  > The bounty on your head to be captured");
            sender.sendMessage(ChatColor.GRAY + "/"+command+" bail" + ChatColor.GREEN + "  > Pays off the price on your head so you may leave jail");
            sender.sendMessage(ChatColor.GRAY + "/"+command+" password" + ChatColor.GREEN + "  > Sets your website account password");
            sender.sendMessage(ChatColor.GRAY + "/"+command+" job" + ChatColor.GREEN + "  > Profession specific commands");
        }
        
        else if (args[0].equalsIgnoreCase("age")){
            double seconds = plugin.getPlayer(sender.getName()).getAge();
            String time = convertSecondsToTime(seconds);
            sender.sendMessage(ChatColor.AQUA + time);
        }
        else if (args[0].equalsIgnoreCase("left")){
            double seconds = plugin.getEconomy().getBalance(sender.getName());
            String time = convertSecondsToTime(seconds);
            sender.sendMessage(ChatColor.DARK_GREEN + time); 
        }
        else if (args[0].equalsIgnoreCase("bounty")){
            TimePlayer tp = plugin.getPlayer(sender.getName());
            double bounty = tp.getBounty();
            String time = convertSecondsToTime(bounty);
            if (bounty > 0)
                sender.sendMessage(ChatColor.RED + time);
            else
                sender.sendMessage(ChatColor.GREEN + "You are not on the bounty list");
        }
        else if (args[0].equalsIgnoreCase("bail")){
            TimePlayer tp = plugin.getPlayer(sender.getName());
            if (tp.getJailed()){
                int bounty = tp.getBounty();
                EconomyResponse es = plugin.getEconomy().withdrawPlayer(sender.getName(), bounty);
                if (es.transactionSuccess()){
                    sender.sendMessage(ChatColor.GREEN + "You've been freed at the cost of " + ChatColor.RED + tp.getBounty());
                    //free user
                } else
                    sender.sendMessage(ChatColor.RED + "You cannot afford bail, you must wait this one out.");
            } else
                sender.sendMessage(ChatColor.GREEN + "You are not in jail");
        } else if (args[0].equalsIgnoreCase("unemploy")){
            final TimePlayer tp = plugin.getPlayer(sender.getName());
            Runnable usetJobLeave = new BukkitRunnable() {
                public void run() {
                    tp.flagConfirm = false;
                }
            };
            
            plugin.getServer().getScheduler().runTaskLater(plugin, usetJobLeave, 20*10);
            TimeProfession job = TimeProfession.UNEMPLOYED;
            int cost = job.getUnemployCost(tp.getZone());
            
            if (!tp.flagConfirm){
                tp.flagConfirm = true;
                sender.sendMessage(ChatColor.GREEN + "Type '/"+command+" unemploy' again to leave your job at the cost of " + 
                                        ChatColor.RED + convertSecondsToTime(cost));
            } else {
                EconomyResponse es = plugin.getEconomy().withdrawPlayer(sender.getName(), cost);
                if (es.transactionSuccess()){
                    tp.removeJob(TimeProfession.UNEMPLOYED);
                    sender.sendMessage(ChatColor.GREEN + "You have left your job! You are now unemployed.");
                } else
                    sender.sendMessage(ChatColor.RED + "It seems you cannot afford to lose your job.");
            }
        } else if (args[0].equalsIgnoreCase("home")){
            if (args.length == 1){
                sender.sendMessage(ChatColor.GRAY + "rent" + ChatColor.GREEN + "  > Rent the home you are currently standing in");
                sender.sendMessage(ChatColor.GRAY + "buy" + ChatColor.GREEN + "  > Purchase the home to get a cut of the income from renters");
            } else {
                Homes h = new Homes(plugin);
                h.commands(sender, args);
            }
        } else if (args[0].equalsIgnoreCase("test")){
            plugin.prof_builder.createBuild(sender, "test.schematic");
        } else if (args[0].equalsIgnoreCase("password")){
            if (args.length > 1){
                plugin.getSql().addPlayer(sender.getName(), args[1]);
                sender.sendMessage(ChatColor.GREEN + "Password has been set, visit " + ChatColor.GRAY + "http://depthsonline.com/minecraft" + ChatColor.GREEN + " to login");
            } else 
                sender.sendMessage(ChatColor.RED + "Please specify a password!");
        } else if (args[0].equalsIgnoreCase("job")){
            HashMap<TimeProfession, Integer> jobs = plugin.getPlayer(sender.getName()).getJobs();
            Iterator it = jobs.entrySet().iterator();
             while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                TimeProfession job = (TimeProfession) pairs.getKey();
                if (args.length == 1){
                    sender.sendMessage("- - - - - - - - - - - - - - - - - -");
                    sender.sendMessage("Your current job is a " + ChatColor.GREEN + job.toString().toLowerCase());
                    sender.sendMessage("- - - - - - - - - - - - - - - - - -");
                }
                if (job == TimeProfession.BUILDER)
                    plugin.prof_builder.commands(command, sender, args);
                if (job == TimeProfession.LANDLORD){
                    plugin.prof_landlord.commands(command, sender, args);
                }
            }
        } else if (args[0].equalsIgnoreCase("admin")){
            adminCommand(sender, args);
        } else
            sender.sendMessage(ChatColor.GRAY + "Invalid command, type " + ChatColor.GREEN + "/life" + ChatColor.GRAY + " for more info");
        return true;
    }
    
    private void adminCommand(CommandSender sender, String[] args){
        if (args.length == 1){
            sender.sendMessage(ChatColor.GRAY + "home" + ChatColor.GREEN + "  > Home related commands");
            sender.sendMessage(ChatColor.GRAY + "update" + ChatColor.GREEN + "  > Updates schematic prices");
            sender.sendMessage(ChatColor.GRAY + "createspawn [difficulty]" + ChatColor.GREEN + "  > Creates a spawn of select difficulty (1-5, 5=hardest)");
        } else if (args[1].equalsIgnoreCase("update")){
            WorldGuardUtil wgu = new WorldGuardUtil(plugin, plugin.prof_builder.getWorld());
            wgu.updateBuildWorth(plugin.prof_builder.getSchematics());
        } else if (args[1].equalsIgnoreCase("home")){
            Homes h = new Homes(plugin);
            h.adminCommands(sender, args);
        } else if (args[1].equalsIgnoreCase("createspawn")){
            int difficulty;
            try {
                difficulty = Integer.parseInt(args[2]);
            } catch (Exception ex){
                sender.sendMessage(ChatColor.RED + "Please specify a valid difficulty level from 1 to 5");
                return;
            }
            if (!plugin.getMobControl().createSpawn(sender, difficulty)){
                sender.sendMessage("Failed to create the spawn .. I don't know why");
                plugin.sendConsole("Failed to createSpawn of difficulty " + difficulty);
            } else
                sender.sendMessage(ChatColor.GREEN + "Spawn created with difficulty " + difficulty);
        }
    }
    
    
    
}
