/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.tengel.time.profs.TimeProfession;
import com.tengel.time.structures.TimePlayer;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.World;
import org.bukkit.inventory.PlayerInventory;

/**
 *
 * @author Tim
 */
public class TimeSigns extends Config {
    private final Time plugin;
    private Map<String, Sign> signs;
    
    
    public TimeSigns(Time plugin){
        super(plugin);
        this.plugin = plugin;
        this.signs = new HashMap<String, Sign>();
        setConfigFile(new File(plugin.getDataFolder() + File.separator + "signs.yml").getAbsoluteFile());
        
        if (!getConfigFile().exists()){
            try {
                getConfigFile().createNewFile();
            } catch (Exception e){
                plugin.sendConsole("Error creating signs.yml");
            }
        } else
            try{
                load(getConfigFile());
            }catch (Exception ignored){}
    }
    
    public void load(){
        Map<String, Object> map = loadConfiguration(getConfigFile()).getValues(false);
        for (String key : map.keySet()){
            String[] coords = key.split(",");
            //System.out.println(key);
            String world = this.getString(key+".world");
            if (world == null)
                world = "Time";
            World w = plugin.getServer().getWorld(world);
            Material m = Material.getMaterial(this.getString(key+".item"));
            int price = this.getInt(key+".price");
            int quantity = this.getInt(key+".quantity");
            //int stock = this.getInt(key+".stock");
            Location loc = new Location(w, Integer.valueOf(coords[0]),Integer.valueOf(coords[1]),Integer.valueOf(coords[2]));
            Block b = loc.getBlock();
            if (b.getType().equals(Material.SIGN_POST) || b.getType().equals(Material.WALL_SIGN)) {
                Sign s = (Sign) b.getState();
                if (m != null)
                    setSignMaterial(s, m.name());
                if (price > 0)
                    setSignPrice(s, price);
                if (quantity > 0)
                    setSignQuantity(s, quantity);
                else if (s.getLine(0).contains("Buy")){
                    ConfigItemStock cis = plugin.getConfigItemStock();
                    setSignStock(s, cis.getStock(m.name()));
                }
                signs.put(key, s);
                    
            } else
                plugin.sendConsole("No sign found at location " + key + " on world " + w.getName());
        }
    }
    
    public void setSignPrice(Sign s, int price){
        s.setLine(2, ChatColor.GREEN+String.valueOf(price)+" mins");
        s.update();
    }
    
    public void setSignQuantity(Sign s, int quantity){
        s.setLine(3, "Quantity: "+String.valueOf(quantity));
        s.update();
    }
    
    public void setSignStock(Sign s, int quantity){
        s.setLine(3, "Stock: "+String.valueOf(quantity));
        s.update();
    }
    
    public void setSignMaterial(Sign s, String material){
        s.setLine(1, material);
        s.update();
    }
    
    private int getNumberFromLine(String line){
        Pattern p = Pattern.compile("-?\\d+");
        Matcher m = p.matcher(line);
        if (m.find()){
            return Integer.valueOf(m.group());
        } else{
            return 0;
        }
    }
    
    public void buyProfession(TimePlayer tp, String prof){
        if (plugin.getPlayerListener().checkPermissions(tp.getPlayer(), "buy.profession", false)){
            TimeProfession tprof;
            try {
                tprof = TimeProfession.valueOf(prof.toUpperCase());
                tprof.give(plugin, tp.getPlayer());
            } catch (IllegalArgumentException e){
                tp.sendMessage(ChatColor.RED + "Invalid profession, report sign to an admin");
                plugin.sendConsole("Invalid profession sign activated by " + tp.getName() + " at: " + (int)tp.getPlayer().getLocation().getX() + "," +
                        (int)tp.getPlayer().getLocation().getY() + "," + (int)tp.getPlayer().getLocation().getZ());
            }
            
        } else
            tp.sendMessage(ChatColor.RED + "You do not have permissions to do that!");
    }
    
    private boolean buyBlockLicense(TimePlayer tp, Material m, int cost){
        EconomyResponse es = plugin.getEconomy().withdrawPlayer(tp.getName(), cost*60);
        if (es.transactionSuccess()){
            if (tp.addBlockLicense(m.getId())){
                tp.sendMessage(ChatColor.BLUE + m.name().toLowerCase() + ChatColor.YELLOW + " license aquired!");
            } else {
                tp.sendMessage("It appears you already own the license to mine " + m.name().toLowerCase());
                plugin.getEconomy().depositPlayer(tp.getName(), cost*60);
            }
            return true;
        }
        return false;
    }
    
    public void buyBlockLicense(TimePlayer tp, Sign sign){
        if (plugin.getPlayerListener().checkPermissions(tp.getPlayer(), "buy.license", false)){
            double balance = plugin.getEconomy().getBalance(tp.getName());
            Material m = plugin.getItemMaterial(sign.getLine(1));
            int cost = Integer.valueOf(sign.getLine(3));
            if (balance >= cost)
                buyBlockLicense(tp, m, cost);
            else
                tp.sendMessage(ChatColor.RED + "Insufficient time");
        } else
            tp.sendMessage(ChatColor.RED + "You do not have permissions to do that!");
    }
    
    private void buyItem(TimePlayer tp, Sign sign, Material m, int cost){
        int savings = (int) Math.floor(Math.min(tp.getRep()/50000D, 1.00) * 0.15 * cost);
        cost -= savings;
        EconomyResponse es = plugin.getEconomy().withdrawPlayer(tp.getName(), cost*60);
        if (es.transactionSuccess()){
            ItemStack item = new ItemStack(m);
            int new_stock = plugin.getConfigItemStock().removeStock(m.name(), 1);
            setSignStock(sign, new_stock);
            tp.getPlayer().getInventory().addItem(item);
            tp.getPlayer().updateInventory();
            tp.sendMessage(ChatColor.YELLOW+"You have just purchased " + ChatColor.GREEN + m.name().toLowerCase()+ChatColor.YELLOW+" for " +
                    ChatColor.RED+String.valueOf(cost) + " mins" +ChatColor.YELLOW+", saving " + ChatColor.GREEN + (int)Math.ceil(savings) + " min(s)" + ChatColor.YELLOW+" due to your reputation");
        } else
            tp.sendMessage(ChatColor.RED + "You require " + (int)Math.ceil(cost - tp.getBalance()/60) + " more mins to purchase" );
    }
    
    private void donateItem(TimePlayer tp, Sign s, Material m){
        ItemStack is = new ItemStack(m);
        PlayerInventory pi = tp.getPlayer().getInventory();
        if (pi.containsAtLeast(is, 1)){
            ConfigReputation cr = plugin.getConfigReputation();
            ConfigItemStock cis = plugin.getConfigItemStock();
            int rep = cr.getItemRep(m.name());
            pi.removeItem(is);
            int new_stock = cis.addStock(m.name(), 1);
            setSignStock(s, new_stock);
            tp.getPlayer().updateInventory();
            tp.sendMessage(ChatColor.YELLOW + "You have just donated 1x " + ChatColor.GREEN + m.name().toLowerCase()+ChatColor.YELLOW+" for "+ChatColor.GREEN+ 
                    rep + ChatColor.YELLOW + " reputation");
            if (!tp.addRep(rep)){
                tp.sendMessage(ChatColor.GRAY + "It looks like you've reached your daily reputation gain limit (500). You will not gain any more reputation for today");
            }
        } else
            tp.sendMessage(ChatColor.RED + "You don't have any "+ChatColor.YELLOW + m.name() + ChatColor.RED+ " to donate");
    }
    
    public void buyItem(TimePlayer tp, Sign sign, boolean donate){
        if (plugin.getPlayerListener().checkPermissions(tp.getPlayer(), "buy.items", false)){
            Material m = plugin.getItemMaterial(sign.getLine(1));
            int cost = getNumberFromLine(sign.getLine(2));
            if (m == null || cost == 0){
                tp.sendMessage(ChatColor.RED+"It appears this sign is wrong, please report it to an admin.");
                return;
            }
            if (donate){
                donateItem(tp, sign, m);
            } else {
                if (plugin.getConfigItemStock().getStock(m.name()) > 0){
                    buyItem(tp, sign, m, cost);
                } else
                    tp.sendMessage(ChatColor.RED + "This item appears to be out of stock, a player must donate to this shop to increase stock level.");
            }
        } else
            tp.sendMessage(ChatColor.RED + "You do not have permissions to do that!");
    }
    
    private void sellItem(TimePlayer tp, Material m, int payment, int quantity){
        ItemStack is = new ItemStack(m, quantity);
        PlayerInventory pi = tp.getPlayer().getInventory();
        if (pi.containsAtLeast(is, 1)){
            Map<Integer, ItemStack> map = pi.removeItem(is);
            ItemStack is_left = map.get(0);
            if (is_left != null){
                quantity -= is_left.getAmount();
                plugin.sendConsole(is_left.toString());
            }
            payment *= quantity;
            tp.getPlayer().updateInventory();
            int bonus = (int) Math.floor(Math.min(tp.getRep()/50000D, 1.00) * 0.50 * payment);
            plugin.getEconomy().depositPlayer(tp.getName(), (payment+bonus)*60);
            tp.sendMessage(ChatColor.YELLOW + "You have just sold "+String.valueOf(quantity)+"x " + ChatColor.GREEN + m.name().toLowerCase()+ChatColor.YELLOW+" for "+ChatColor.GREEN+ 
                    String.valueOf(payment+bonus) +  " mins" + ChatColor.YELLOW+ " of time, earning an extra " + ChatColor.GREEN + bonus + " min(s)" + ChatColor.YELLOW+
                    " because of your reputation!");
        } else
            tp.sendMessage(ChatColor.RED + "You don't have any "+ChatColor.YELLOW + m.name() + ChatColor.RED+ " to sell");
    }
    
    public void sellItem(TimePlayer tp, Sign sign){
        if (plugin.getPlayerListener().checkPermissions(tp.getPlayer(), "sell.items", false)){
            Material m = plugin.getItemMaterial(sign.getLine(1));
            int quantity = getNumberFromLine(sign.getLine(3));
            if (quantity == 0) quantity = 1;
            int payment = getNumberFromLine(sign.getLine(2));
            if (m == null || payment == 0){
                tp.sendMessage(ChatColor.RED+"It appears this sign is wrong, please report it to an admin.");
                return;
            }
            sellItem(tp, m, payment, quantity);
        } else
            tp.sendMessage(ChatColor.RED + "You do not have permissions to do that!");
    }
    
    private boolean validateSign(TimePlayer tp, Sign sign){
        int cost,quantity=1;
        if (sign.getLine(0).contains("[Job]")){
            sign.setLine(0, ChatColor.AQUA + "" + ChatColor.BOLD + "[Job]");
            try {
                TimeProfession.valueOf(sign.getLine(1).toUpperCase());
                sign.update();
                return true;
            } catch (IllegalArgumentException e){
                tp.sendMessage(ChatColor.RED + "Invalid profession '" + sign.getLine(1) + "'");
                dropSign(sign.getLocation());
                return false;
            }
        }

        Material m = plugin.getItemMaterial(sign.getLine(1));
        if (m == null){
            tp.sendMessage(ChatColor.RED + "Invalid item name or ID on line 2");
            dropSign(sign.getLocation());
            return false;
        }

        try {
            cost = Integer.parseInt(sign.getLine(2));
        }catch (Exception e){
            tp.sendMessage(ChatColor.RED + "Invalid cost on line 3.");
            dropSign(sign.getLocation());
            return false;
        }

        quantity = getNumberFromLine(sign.getLine(3));
        if (sign.getLine(0).contains("[License]"))
            sign.setLine(0, ChatColor.BOLD + "" + ChatColor.BLUE + "  [License]");
        else if (sign.getLine(0).contains("[Buy]")){
            sign.setLine(0, ChatColor.BOLD + "" + ChatColor.BLUE + "  [Buy]");
            ConfigItemPrices cs = plugin.getConfigItemPrices();
            ConfigReputation cr = plugin.getConfigReputation();
            ConfigItemStock cis = plugin.getConfigItemStock();
            cs.updateItem(m.name(), cost);
            cr.verifyItem(m.name());
            setSignStock(sign, cis.getStock(m.name()));
        } else if (sign.getLine(0).contains("[Sell]")){
            sign.setLine(0, ChatColor.BOLD + "" + ChatColor.BLUE + "  [Sell]");
            setSignQuantity(sign, quantity);
        }
        setSignMaterial(sign, m.name());
        setSignPrice(sign, cost);
        return true;
    }
    
    public void create(TimePlayer tp, Sign sign){
        if (plugin.getPlayerListener().checkPermissions(tp.getPlayer(), "create.signshop", false)){
            if (validateSign(tp, sign)){
                Location loc = sign.getLocation();
                String world = loc.getWorld().getName();
                Material m =  Material.getMaterial(sign.getLine(1));
                int price = getNumberFromLine(sign.getLine(2));
                int quantity = getNumberFromLine(sign.getLine(3));
                String path = loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ();
                this.set(path + ".world", world);
                if (m != null)
                    this.set(path + ".item", m.toString());
                if (price > 0)
                    this.set(path + ".price", price);
                if (quantity > 0)
                    this.set(path + ".quantity", quantity);
                this.signs.put(path, sign);
                tp.sendMessage(ChatColor.GREEN + "TimeSign created!");
            }
        } else{
            tp.sendMessage(ChatColor.RED + "You do not have permissions to do that!");
            dropSign(sign.getLocation());
        }
    }
    
    public void remove(TimePlayer tp, Sign sign){
        if (plugin.getPlayerListener().checkPermissions(tp.getPlayer(), "create.signshop", false)){
            if (tp.getAdminMode()){
                Location loc = sign.getLocation();
                String path = loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ();
                this.set(path, null);
                this.signs.remove(path);
                dropSign(sign.getLocation());
                tp.sendMessage(ChatColor.GREEN + "TimeSign removed!");
            } else
                tp.sendMessage(ChatColor.RED + "You need to be in admin mode to remove this sign");
        } else{
            tp.sendMessage(ChatColor.RED + "You do not have permissions to do that!");
        }
    }
    
    public void dropSign(Location location) {
        location.getBlock().setType(Material.AIR);
        location.getWorld().dropItemNaturally(location, new ItemStack(Material.SIGN, 1));
    }
    
    public Map<String, Sign> getSigns(){
        return this.signs;
    }
    
    public void save(){
        try{
            super.save(getConfigFile());
        }
        catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    
}
