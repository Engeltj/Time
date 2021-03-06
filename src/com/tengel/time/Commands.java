/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.tengel.time.exceptions.HomeFailedToCreateException;
import com.tengel.time.profs.TimeProfession;
import com.tengel.time.structures.Home;
import com.tengel.time.structures.TimePlayer;
import java.util.List;
import java.util.Map;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Tim
 */
public class Commands implements Listener{
    private Time plugin;
    private CommandSender sender;
    private Command cmd;
    private String label;
    private String[] args;
    
    public Commands(){
        
    }
    
    public Commands(Time plugin, CommandSender sender, Command cmd, String label, String[] args) {
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
            sender.sendMessage(ChatColor.GRAY + "/"+command+" plot" + ChatColor.GREEN + "  > Creative plots related commands");
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
        else if (args[0].equalsIgnoreCase("plot")){
            if (args.length == 1){
                sender.sendMessage(ChatColor.GRAY + "create" + ChatColor.GREEN + "  > Claims a new plot where you stand");
                sender.sendMessage(ChatColor.GRAY + "destroy" + ChatColor.GREEN + "  > Unclaims plot where you stand, your blocks will be destroyed");
            } else
                return commandsPlot(sender, args);
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
            if (tp.isJailed()){
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
            if (tp.getJobs().size() == 0 || (tp.getJobs().size()==1 && tp.getJobs().get(0) == (TimeProfession.UNEMPLOYED))){
                sender.sendMessage(ChatColor.RED + "You are already unemployed");
            } else if (tp.getJobs().size() == 2 && args.length < 2){
                sender.sendMessage(ChatColor.RED + "It seems you have two jobs, please specify which you want to leave");
            } else {
                if (tp.getJobs().size() == 2){
                    try {
                        TimeProfession.valueOf(args[1].toUpperCase());
                    } catch (IllegalArgumentException ex){
                        sender.sendMessage(ChatColor.RED + "Profession '" + args[1] + " doesn't exist");
                        return true;
                    }
                }

                plugin.getServer().getScheduler().runTaskLater(plugin, new BukkitRunnable() {
                    public void run() {
                        tp.flagConfirm = false;
                    }
                }, 20*10);
                int cost = TimeProfession.getUnemployCost(tp.getZone());
                if (!tp.flagConfirm){
                    String job = "";
                    if (tp.getJobs().size() == 2)
                        job = args[1] + " ";
                    tp.flagConfirm = true;
                    sender.sendMessage(ChatColor.GREEN + "Type " + ChatColor.BOLD + "/"+command+" unemploy " + job + ChatColor.RESET + ChatColor.GREEN
                            + "again to leave your job at the cost of " + ChatColor.RED + convertSecondsToTime(cost));
                } else {
                    EconomyResponse es = plugin.getEconomy().withdrawPlayer(sender.getName(), cost);
                    if (es.transactionSuccess()){
                        if (tp.getJobs().size() == 2)
                            tp.removeJob(TimeProfession.valueOf(args[1].toUpperCase()));
                        else
                            tp.removeJob(tp.getJobs().get(0));
                        sender.sendMessage(ChatColor.GREEN + "You have left your job! You are now unemployed");
                    } else
                        sender.sendMessage(ChatColor.RED + "It seems you cannot afford to lose your job");
                }
            }

        } else if (args[0].equalsIgnoreCase("employ")){
            if (args.length < 2){
                sender.sendMessage(ChatColor.RED + "Specify the job title you wish to obtain");
            } else {
                try {
                    TimeProfession prof = TimeProfession.valueOf(args[1].toUpperCase());
                    TimePlayer tp = plugin.getPlayer(sender.getName());
                    if (tp != null)
                        plugin.getShopSigns().buyProfession(tp, prof.name());
                } catch (IllegalArgumentException ex){
                    sender.sendMessage(ChatColor.RED + "The profession '"+args[1]+"' does not exist, try again");
                }
            }

        } else if (args[0].equalsIgnoreCase("home")){
            if (args.length == 1){
                TimePlayer tp = plugin.getPlayer(sender.getName());
                Location loc = tp.getPlayer().getLocation();
                Home h = plugin.getHome(loc);
                if (h == null || !h.getRenter().equalsIgnoreCase(tp.getName()))
                    sender.sendMessage(ChatColor.GRAY + "rent" + ChatColor.GREEN + "  > Rent the home you are currently standing in");
                else
                    sender.sendMessage(ChatColor.GRAY + "unrent" + ChatColor.GREEN + "  > Unrent the home you are currently standing in");
                sender.sendMessage(ChatColor.GRAY + "buy" + ChatColor.GREEN + "  > Purchase the home to get a cut of the income from renters");
                sender.sendMessage(ChatColor.GRAY + "teleport <home>" + ChatColor.GREEN + "  > Teleports you to one of your specified homes");
            } else
                return commandsHome(sender, args);
        } else if (args[0].equalsIgnoreCase("test")){
            //plugin.prof_builder.createBuild(sender, "test.schematic");
            Location loc = plugin.getLocation(0, "jail");
            sender.sendMessage(loc.toString());
            plugin.getServer().getPlayer(sender.getName()).teleport(loc);
        } else if (args[0].equalsIgnoreCase("password")){
            if (args.length > 1){
                TimePlayer tp = plugin.getPlayer(sender.getName());
                tp.setPassword(args[1]);
                sender.sendMessage(ChatColor.GREEN + "Password has been set, visit " + ChatColor.GRAY + "http://depthsonline.com/minecraft" + ChatColor.GREEN + " to login");
            } else
                sender.sendMessage(ChatColor.RED + "Please specify a password!");
        } else if (args[0].equalsIgnoreCase("job")){
            List<TimeProfession> jobs = plugin.getPlayer(sender.getName()).getJobs();
            if (jobs.size() == 0 || (jobs.size() == 1 && jobs.get(0) == TimeProfession.UNEMPLOYED)){
                sender.sendMessage(ChatColor.RED+"You are unemployed, see your nearest hiring location");
            } else {
                for (TimeProfession prof : jobs){
                    if (args.length == 1){
                        sender.sendMessage("- - - - - - - - - - - - - - - - - -");
                        sender.sendMessage("Your current job is a " + ChatColor.GREEN + prof.toString().toLowerCase());
                        sender.sendMessage("- - - - - - - - - - - - - - - - - -");
                    }
                    if (prof == TimeProfession.BUILDER)
                        plugin.prof_builder.commands(command, sender, args);
                    if (prof == TimeProfession.LANDLORD)
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
            sender.sendMessage(ChatColor.GRAY + "on|off" + ChatColor.GREEN + "  > Set admin mode on/off");
            sender.sendMessage(ChatColor.GRAY + "home" + ChatColor.GREEN + "  > Home related commands");
            sender.sendMessage(ChatColor.GRAY + "update" + ChatColor.GREEN + "  > Updates schematic prices");
            sender.sendMessage(ChatColor.GRAY + "createspawn [difficulty]" + ChatColor.GREEN + "  > Creates a spawn of select difficulty (1-5, 5=hardest)");
            sender.sendMessage(ChatColor.GRAY + "spawn [monster name]" + ChatColor.GREEN + "  > Sets your location to spawn the specified monster");
            sender.sendMessage(ChatColor.GRAY + "save" + ChatColor.GREEN + "  > Saves plugin data to flatfiles and database");
        } else if (args[1].equalsIgnoreCase("on")){
            plugin.getPlayer(sender.getName()).setAdminMode(true);
            sender.sendMessage(ChatColor.GREEN+"Admin mode has been activated");
        } else if (args[1].equalsIgnoreCase("off")){
            plugin.getPlayer(sender.getName()).setAdminMode(false);
            sender.sendMessage(ChatColor.GREEN+"Admin mode has been de-activated");
        } else if (args[1].equalsIgnoreCase("update")){
            WorldGuardUtil wgu = new WorldGuardUtil(plugin, plugin.prof_builder.getWorld());
            wgu.updateBuildWorth(plugin.prof_builder.getSchematics());
            sender.sendMessage(ChatColor.GREEN+"Home prices updated");
        } else if (args[1].equalsIgnoreCase("home")){
            //Homes h = new Homes(plugin);
            adminCommandsHome(sender, args);
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
        } else if (args[1].equalsIgnoreCase("spawn")){
            try {
                String type = args[2].toUpperCase();
                EntityType.valueOf(type);
                Location loc = sender.getServer().getPlayer(sender.getName()).getLocation();
                //TimeMonster monster = new TimeMonster(plugin, loc, args[2]);
                plugin.getMobControl().addMonsterSpawn(loc, type);
                sender.sendMessage(ChatColor.GREEN+"Spawn added");
            } catch (Exception ex){
                sender.sendMessage(ChatColor.RED+"Invalid monster, your choices are: ");
                sender.sendMessage(ChatColor.RED+"BLAZE CAVE_SPIDER CREEPER ENDERMAN GHAST GIANT IRON_GOLEM MAGMA_CUBE PIG_ZOMBIE SILVERFISH SKELETON SLIME SPIDER WOLF ZOMBIE ");
            }
        } else if (args[1].equalsIgnoreCase("save")){
            plugin.save();
            sender.sendMessage(ChatColor.GREEN+"All data saved :) ");
        } else
            sender.sendMessage(ChatColor.RED+"Invalid option");
    }

    public boolean commandsPlot(CommandSender sender, String[] args){
        if (args[1].equalsIgnoreCase("create")){
            plugin.getCreativePlots().create(plugin.getServer().getPlayer(sender.getName()));
        } else if (args[1].equalsIgnoreCase("destroy")){
            plugin.getCreativePlots().destroy(plugin.getServer().getPlayer(sender.getName()));
        } else
            return false;
        return true;
    }


    public boolean commandsHome(CommandSender sender, String[] args){
        TimePlayer tp = plugin.getPlayer(sender.getName());
        Player p = plugin.getServer().getPlayer(sender.getName());
        Home h = plugin.getHome(p.getLocation());
        if (h == null){
            if (args.length >= 3)
                h = plugin.getHome(args[2]);
        }
        if (h == null)
            h = plugin.getHome(plugin.getPlayer(sender.getName()).getPlayer().getLocation());
        
        if (args[1].equalsIgnoreCase("rent")){
            if (h == null)
                p.sendMessage(ChatColor.RED + "Please specify a valid home, or stand in a home first");
            else
                h.rent(p);
        } else if (args[1].equalsIgnoreCase("unrent")){
            if (h == null)
                p.sendMessage(ChatColor.RED + "Please specify a valid home, or stand in a home first");
            else if (h.getRenter().equalsIgnoreCase(p.getName())){
                if (h.evict())
                    p.sendMessage(ChatColor.GREEN + "You have been evicted");
                else
                    p.sendMessage(ChatColor.GREEN + "Something went wrong with evict, please speak with an admin");
            }else
                p.sendMessage(ChatColor.RED + "You are currently not renting this homes");
        } else if (args[1].equalsIgnoreCase("buy")){
            if (tp.hasJob(TimeProfession.LANDLORD)){
                if (h == null)
                    p.sendMessage(ChatColor.RED + "Please specify a valid home, or stand in a home first");
                else
                    h.buy(p);
            } else
                sender.sendMessage(ChatColor.RED + "You need to be a landlord to purchase, you may only rent this home");
            
        } else if (args[1].equalsIgnoreCase("teleport")){
            Map<String, Home> homes = plugin.getHomesByOwner(sender.getName());
            if (homes.size() == 0)
                sender.sendMessage(ChatColor.RED + "You are currently not renting any homes");
            else if (!homes.containsValue(h)){
                sender.sendMessage("Choices:");
                for (String home : plugin.getHomesByOwner(sender.getName()).keySet()){
                    sender.sendMessage(ChatColor.GREEN+home);
                }
            } else {
                org.bukkit.util.Vector v = h.getDoor();
                Location loc = new Location(plugin.getServer().getWorld("Time"), v.getX(), v.getY(), v.getZ());
                plugin.getServer().getPlayer(sender.getName()).teleport(loc);
                sender.sendMessage(ChatColor.GREEN+"You've been taken to your front door");
            }
            
        } else
            return false;
        return true;       
    }
    
    public void adminCommandsHome(CommandSender sender, String[] args){
        Player p = plugin.getServer().getPlayer(sender.getName());
        Home home = plugin.getHome(p.getLocation());
        if (home == null){
            if (args.length >= 3)
                home = plugin.getHome(args[2]);
        }
        if (args.length == 2){
            sender.sendMessage(ChatColor.GRAY + "create <name> [type]" + ChatColor.GREEN + "  > Create a new home, defaults to type apartment");
            sender.sendMessage(ChatColor.GRAY + "remove <name>" + ChatColor.GREEN + "  > Create a new home, defaults to type apartment");
            sender.sendMessage(ChatColor.GRAY + "update <name>" + ChatColor.GREEN + "  > Update the region of a home");
            sender.sendMessage(ChatColor.GRAY + "reset <name>" + ChatColor.GREEN + "  > Reset home to factory state");
        } else {
            if (args[2].equalsIgnoreCase("create")){
                String name = "";
                String type = "";
                if (args.length >= 4)
                    name = args[3];
                if (args.length >= 5)
                    type = args[4];
                if (plugin.getHome(name) == null) {
                    Home new_home;
                    try {
                        new_home = new Home(plugin, p, name, type);
                        plugin.addHome(new_home);
                        new_home.setPrice(new_home.getRentWorth());
                        sender.sendMessage(ChatColor.GREEN+"Home created successfully, door is set where you were standing");
                    } catch (HomeFailedToCreateException ex) {
                        System.out.println(ex.getMessage());
                        sender.sendMessage(ChatColor.RED+"Failed to create home");
                    }
                    
                } else
                    sender.sendMessage(ChatColor.RED+"Home failed to create, name already taken");
            } else if (args[2].equalsIgnoreCase("update")){
                if (home == null)
                    sender.sendMessage(ChatColor.RED+"Please stand in or specify a valid home first");
                else{
                    Location loc = p.getLocation();
                    WorldGuardUtil wgu = new WorldGuardUtil(plugin, p.getWorld());
                    if (!wgu.saveSchematic(p, "homes", home.getName())){
                        sender.sendMessage(ChatColor.RED + "Failed to save schematic for home '"+home+"', aborting home update.");
                        return;
                    }
                    if (wgu.createRegionFromSelection(p, home.getName()) == null){
                        sender.sendMessage(ChatColor.RED + "Failed to create region for home '"+home+"', aborting home update.");
                        return;
                    }
                    home.setDoor(loc.toVector());
                    sender.sendMessage(ChatColor.GREEN + "Home successfully updated.");
                }
            } else if (args[2].equalsIgnoreCase("reset")){
                if (home != null)
                    home.reset();
                else
                    sender.sendMessage(ChatColor.RED+"Please stand in or specify a valid home first");
            } else if (args[2].equalsIgnoreCase("remove")){
                if (home != null){
                    home.remove();
                    sender.sendMessage(ChatColor.GREEN+"Home successfully reset and removed!");
                } else
                    sender.sendMessage(ChatColor.RED+"Please stand in or specify a valid home first");
            }
        }
    }
    
    
}
