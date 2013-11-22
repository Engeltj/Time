/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.tengel.time.profs.TimeProfession;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Tim
 */
public class ShopSigns extends YamlConfiguration {
    private Player player;
    private Time plugin;
    private File configFile;
    
    public ShopSigns(Time plugin, Player player){
        super();
        this.plugin = plugin;
        this.player = player;
        this.configFile = new File(plugin.getDataFolder() + "\\signs.yml").getAbsoluteFile();        
        if (!this.configFile.exists()){
            try {
                this.configFile.createNewFile();
            } catch (Exception e){
                plugin.sendConsole("Error creating signs.yml");
            }
        } else
            try{
                super.load(this.configFile);
            }catch (Exception e){}
    }
    
    private boolean buyLicense(Material m, int cost){
        ConfigPlayer config = new ConfigPlayer(plugin, player);
        EconomyResponse es = getPlugin().getEconomy().withdrawPlayer(player.getName(), cost*60);
        if (es.transactionSuccess()){
            if (config.addLicense(m.getId())){
                player.sendMessage(plugin.getPluginName() + ChatColor.BLUE + m.name().toLowerCase() + ChatColor.YELLOW + " license aquired!");
            }
            else {
                player.sendMessage(plugin.getPluginName() + "It appears you already own the license to mine " + m.name().toLowerCase());
                getPlugin().getEconomy().depositPlayer(player.getName(), cost*60);
            }
            return true;
        }
        return false;
    }
    
    private void buyItem(Material m, int cost){
        EconomyResponse es = getPlugin().getEconomy().withdrawPlayer(player.getName(), cost*60);
        if (es.transactionSuccess()){
            ItemStack item = new ItemStack(m);
            player.getInventory().addItem(item);
            player.updateInventory();
            player.sendMessage(getPlugin().getPluginName() + ChatColor.YELLOW + "You have just purchased 1x " + ChatColor.BLUE + m.name().toLowerCase());
        } else
            player.sendMessage(plugin.getPluginName() + ChatColor.RED + "Insufficent life, transaction cancelled");
    }
    
    public void buyProfession(String prof){
        if (plugin.getPlayerListener().checkPermissions(player, "buy.profession", false)){
            TimeProfession tp;
            try {
                tp = TimeProfession.valueOf(prof.toUpperCase());
                tp.give(plugin, player);
            } catch (Exception e){
                player.sendMessage(plugin.getPluginName() + ChatColor.RED + "Invalid profession, report sign to an admin");
                plugin.sendConsole("Invalid profession sign activated by " + player.getName() + " at: " + (int)player.getLocation().getX() + "," +
                        (int)player.getLocation().getY() + "," + (int)player.getLocation().getZ());
            }
            
        } else
            player.sendMessage(plugin.getPluginName() + ChatColor.RED + "You do not have permissions to do that!");
    }
    
    public void buy(Block block, int cost){
        Sign s = (Sign) block.getState();
        Material m = plugin.getItemMaterial(s.getLine(1));
        if (m == null){
            player.sendMessage(plugin.getPluginName() + ChatColor.RED+"It appears this sign is wrong, please report it to an admin.");
            //dropSign(block.getLocation());
            return;
        }
        
        if (plugin.getEconomy().getBalance(player.getName()) >= cost){
            ConfigPlayer configFile = plugin.getTimePlayers().getPlayerConfig(player.getName());
            if (s.getLine(0).contains("License")){
                if (plugin.getPlayerListener().checkPermissions(player, "buy.license", false))
                    buyLicense(m, cost);
                else
                    player.sendMessage(plugin.getPluginName() + ChatColor.RED + "You do not have permissions to do that!");
            } else if (s.getLine(0).contains("Shop")){
                if (plugin.getPlayerListener().checkPermissions(player, "buy.items", false))
                    buyItem(m, cost);
                else
                    player.sendMessage(plugin.getPluginName() + ChatColor.RED + "You do not have permissions to do that!");
            }
        }
    }
    
    private boolean validateSign(SignChangeEvent event){
        Block block = event.getBlock();
        
        int cost = 0;
        if (getPlugin().getPlayerListener().checkPermissions(player, "create.signshop", false)){
            if (event.getLine(0).contains("[Job]")){
                event.setLine(0, ChatColor.BOLD + "" + ChatColor.AQUA + "  [Job]");
                return true;
            }
            
            Material m = plugin.getItemMaterial(event.getLine(1));
            //plugin.sendConsole("line: " + s.getLine(1) +".");
            if (m == null){
                player.sendMessage(plugin.getPluginName() + ChatColor.RED + "Invalid item name or ID on line 2");
                dropSign(block.getLocation());
                return false;
            }
            
            try {
                cost = Integer.parseInt(event.getLine(2));
            }catch (Exception e){
                player.sendMessage(getPlugin().getPluginName() + "Invalid cost on line 3.");
                dropSign(block.getLocation());
                return false;
            }
            if (event.getLine(0).contains("[License]"))
                event.setLine(0, ChatColor.BOLD + "" + ChatColor.BLUE + "  [License]");
            else if (event.getLine(0).contains("[Shop]")){
                event.setLine(0, ChatColor.BOLD + "" + ChatColor.BLUE + "  [Shop]");
                ConfigShop sc = new ConfigShop(plugin);
                sc.updateItem(m.getId(), cost);
            }
            event.setLine(1, m.name());
            event.setLine(2, ChatColor.GREEN + String.valueOf(cost) + " mins");
            Sign s = (Sign) block.getState();
            s.update();
        } else{
            player.sendMessage(getPlugin().getPluginName() + ChatColor.RED + "You do not have permissions to do that!");
            dropSign(block.getLocation());
            return false;
        }
        
        return true;
    }
    
    public void create(SignChangeEvent event){
        Block block = event.getBlock();
        if (validateSign(event)){
            Location l = block.getLocation();
            String path = Integer.toString(l.getBlockX())+","+Integer.toString(l.getBlockY())+","+Integer.toString(l.getBlockZ());
            this.set(path, l.getWorld().getName());
        }
        save();
    }
    
    public void remove(Block block){
        
    }
    
    public void dropSign(Location location) {
        location.getBlock().setType(Material.AIR);
        location.getWorld().dropItemNaturally(location, new ItemStack(Material.SIGN, 1));
    }
    
    public Time getPlugin(){
        return this.plugin;
    }
    
    public File getConfig(){
        return this.configFile;
    }
    
    public void save(){
        try{
            super.save(configFile);
        }
        catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    
}
