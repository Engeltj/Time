/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.tengel.time.mysql.Homes;
import com.tengel.time.profs.TimeProfession;
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
        if (days > 30){
            weeks = Math.floor(days/30);
            days -= weeks*30;
        }
        if (weeks > 52){
            years = Math.floor(weeks/52);
            weeks -= years*52;
        }

        return String.format("%04.0f·%02.0f·%02.0f·%01.0f·%02.0f·%02.0f", years,weeks,days,hours,minutes,seconds);
        
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
            sender.sendMessage(ChatColor.GRAY + "/life age" + ChatColor.GREEN + "  > How long you've played");
            sender.sendMessage(ChatColor.GRAY + "/life left" + ChatColor.GREEN + "  > How long you have left to live");
            sender.sendMessage(ChatColor.GRAY + "/life bounty" + ChatColor.GREEN + "  > The bounty on your head to be captured");
            sender.sendMessage(ChatColor.GRAY + "/life bail" + ChatColor.GREEN + "  > Pays off the price on your head so you may leave jail");
            sender.sendMessage(ChatColor.GRAY + "/life password" + ChatColor.GREEN + "  > Sets your website account password");
            sender.sendMessage(ChatColor.GRAY + "/life job" + ChatColor.GREEN + "  > Profession specific commands");
        }
        
        else if (args[0].equalsIgnoreCase("age")){
            double seconds = plugin.getTimePlayers().getPlayerConfig(sender.getName()).getPlayerAge();
            String time = convertSecondsToTime(seconds);
            sender.sendMessage(plugin.getPluginName() + ChatColor.AQUA + time);
        }
        else if (args[0].equalsIgnoreCase("left")){
            double seconds = plugin.getEconomy().getBalance(sender.getName());
            String time = convertSecondsToTime(seconds);
            sender.sendMessage(plugin.getPluginName() + ChatColor.DARK_GREEN + time); 
        }
        else if (args[0].equalsIgnoreCase("bounty")){
            ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(sender.getName());
            double bounty = cp.getBounty();
            String time = convertSecondsToTime(bounty);
            if (bounty > 0)
                sender.sendMessage(plugin.getPluginName() + ChatColor.RED + time);
            else
                sender.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "You are not on the bounty list");
        }
        else if (args[0].equalsIgnoreCase("bail")){
            ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(sender.getName());
            if (cp.isJailed()){
                int bounty = cp.getBounty();
                EconomyResponse es = plugin.getEconomy().withdrawPlayer(sender.getName(), bounty);
                if (es.transactionSuccess()){
                    sender.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "You've been freed at the cost of " + ChatColor.RED + cp.getBountyString());
                    //free user
                } else
                    sender.sendMessage(plugin.getPluginName() + ChatColor.RED + "You cannot afford bail, you must wait this one out.");
            } else
                sender.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "You are not in jail");
        } else if (args[0].equalsIgnoreCase("unemploy")){
            final ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(sender.getName());
            Runnable usetJobLeave = new BukkitRunnable() {
                public void run() {
                    cp.flag_jobLeave = false;
                }
            };
            
            plugin.getServer().getScheduler().runTaskLater(plugin, usetJobLeave, 20*10);
            TimeProfession tp = TimeProfession.UNEMPLOYED;
            int cost = tp.getUnemployCost(cp.getPlayerTimeZone());
            
            if (!cp.flag_jobLeave){
                cp.flag_jobLeave = true;
                sender.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "Type '/life unemploy' again to leave your job at the cost of " + 
                                        ChatColor.RED + convertSecondsToTime(cost));
            } else {
                EconomyResponse es = plugin.getEconomy().withdrawPlayer(sender.getName(), cost);
                if (es.transactionSuccess()){
                    cp.setProfession("UNEMPLOYED");
                    sender.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "You have left your job! You are now unemployed.");
                } else
                    sender.sendMessage(plugin.getPluginName() + ChatColor.RED + "It seems you cannot afford to lose your job.");
            }
        } else if (args[0].equalsIgnoreCase("home")){
            ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(sender.getName());
            Player p = plugin.getServer().getPlayer(sender.getName());
            if (args.length == 1)
                return true;
            if (args[1].equalsIgnoreCase("rent")){
                Homes h = new Homes(plugin);
                h.rent(p);
            } else if (args[1].equalsIgnoreCase("buy")){
                Homes h = new Homes(plugin);
                if (cp.getProfession() == TimeProfession.LANDLORD)
                    h.buy(p);
                else
                    sender.sendMessage(plugin.getPluginName() + ChatColor.RED + "You need to be a landlord to purchase this home");
                return true;                
            } else if (args[1].equalsIgnoreCase("create")){
                if (plugin.getPlayerListener().checkPermissions(sender, "home.create", false)){
                    if (args.length < 4){
                        sender.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "/life create <region> <price> [farm]");
                        return true;
                    } else {
                        Homes h = new Homes(plugin);
                        double price = 0;
                        try {
                            price = Double.valueOf(args[3]);
                        } catch (Exception e){
                            sender.sendMessage(plugin.getPluginName() + ChatColor.RED + "Invalid price specified");
                            return true;
                        }
                        h.create(p, args[2], price, (args.length == 5));
                        return true;
                    }
                } else
                    return false;
                
            }
        } else if (args[0].equalsIgnoreCase("test")){
            plugin.prof_builder.createBuild(sender, "test.schematic");
        } else if (args[0].equalsIgnoreCase("password")){
            if (args.length > 1){
                plugin.getSql().addPlayer(sender.getName(), args[1]);
                sender.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "Password has been set, visit " + ChatColor.GRAY + "http://depthsonline.com/minecraft" + ChatColor.GREEN + " to login");
            } else 
                sender.sendMessage(plugin.getPluginName() + ChatColor.RED + "Please specify a password!");
        } else if (args[0].equalsIgnoreCase("job")){
            if (plugin.getTimePlayers().getPlayerConfig(sender.getName()).getProfession() == TimeProfession.BUILDER)
                plugin.prof_builder.commands(command, sender, args);
            else
                sender.sendMessage("Commands for your profession aren't implemented yet.");
        } else if (args[0].equalsIgnoreCase("admin")){
            if ((args.length > 1) && (args[1].equalsIgnoreCase("update"))){
                WorldGuardUtil wgu = new WorldGuardUtil(plugin, plugin.prof_builder.getWorld());
                wgu.updateBuildWorth(sender, plugin.prof_builder.getSchematics());
            }
        }
        
        else
            sender.sendMessage(plugin.getPluginName() + ChatColor.GRAY + "Invalid command, type " + ChatColor.GREEN + "/life" + ChatColor.GRAY + " for more info");
        return true;
    }
    
    
    
}
