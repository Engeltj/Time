/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.profs;

import com.tengel.time.Time;
import com.tengel.time.structures.Home;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Tim
 */
public class Landlord {
    private final Time plugin;
    
    public Landlord(Time plugin){
        this.plugin = plugin;
    }
    
    public void commands(String command, CommandSender sender, String [] args){
        if (args.length == 1){
            command = "/"+ command + " " + args[0] + " ";
            sender.sendMessage(ChatColor.GRAY + command + "buy" + ChatColor.GREEN + "  > Buys the home you are standing in");
            sender.sendMessage(ChatColor.GRAY + command + "sell <player> <price>" + ChatColor.GREEN + "  > Sells the home you are standing in (ex: sell Engeltj 3w 6d 4h)");
            sender.sendMessage(ChatColor.GRAY + command + "disown" + ChatColor.GREEN + "  > Disowns the home you are standing in");
            sender.sendMessage(ChatColor.GRAY + command + "name <new name>" + ChatColor.GREEN + "  > Sets the display name for the home you are standing in");
            sender.sendMessage(ChatColor.GRAY + command + "price <new price>" + ChatColor.GREEN + "  > Sets the new daily " + ChatColor.DARK_GREEN + "rent" + ChatColor.GREEN+
                    " cost for the home you are standing in (ex: price 5h 43m)");
        } else {
            Player p = plugin.getServer().getPlayer(sender.getName());
            Location loc = p.getLocation();
            Home h = plugin.getHome(loc);
            if (h == null){
                sender.sendMessage(ChatColor.RED + "Please stand inside a home first.");
                return;
            }
            if (args[1].equalsIgnoreCase("buy")){
                h.buy(p);
            } else if (args[1].equalsIgnoreCase("sell")){
                if (args.length < 3)
                    sender.sendMessage(ChatColor.RED + "Please specify a player to sell to");
                else if (args.length < 4)
                    sender.sendMessage(ChatColor.RED + "Please enter an asking price (ex: 3w 6d 4h)");
                else{
                    Player newOwner = plugin.getServer().getPlayer(args[2]);
                    if (newOwner == null){
                        sender.sendMessage(ChatColor.RED + "Player doesn't exist, try again");
                    } else {
                        double price = getPriceFromArgs(args);
                        if (price == 0)
                            sender.sendMessage(ChatColor.RED + "Invalid price specified, try again");
                        else
                            h.sell(p, newOwner, price);
                    }
                }
            } else if (args[1].equalsIgnoreCase("disown")){
                h.disown(p);
            } else if (args[1].equalsIgnoreCase("name")){
//                if (!h.hasDisplayName(args[2]))
                    h.setDisplayName(args[2]);
//                else
//                    sender.sendMessage(ChatColor.RED + "Display name "+args[2]+" already taken, try another");
            } else if (args[1].equalsIgnoreCase("price")){
                int price = getPriceFromArgs(args);
                h.setPrice(price);
            }
        }
    }
    
    
    private int getPriceFromArgs(String[] args){
        Pattern p = Pattern.compile("-?\\d+");
        int total = 0;
        for (int i=3;i<args.length;i++){
            Matcher m = p.matcher(args[i]);
            if (m.find()){
                int value = Integer.valueOf(m.group());
                if (args[i].contains("y"))
                    total = total + value * 52 * 7 * 24 * 60 * 60;
                else if (args[i].contains("w"))
                    total = total + value * 7 * 24 * 60 * 60;
                else if (args[i].contains("d"))
                    total = total + value * 24 * 60 * 60;
                else if (args[i].contains("h"))
                    total = total + value * 60 * 60;
                else if (args[i].contains("m"))
                    total = total + value * 60;
                else
                    total = total + value;
            }
        }
        return total;
    }
    
}
