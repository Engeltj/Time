/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.tengel.time.profs.Police;
import com.tengel.time.profs.TimeProfession;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Tim
 */
public class TimePlayerListener implements Listener {
    private final Time plugin;
    private final TimePlayers players;
    
    public TimePlayerListener(Time plugin, TimePlayers players) {
            this.plugin = plugin;
            this.players = players;
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        String msg = event.getMessage();
        if (msg.equalsIgnoreCase("I love Depths")){
            p.sendMessage("Depths loves you!");
        }
        else if (msg.equalsIgnoreCase("balance"))
            p.sendMessage(Double.toString(plugin.getEconomy().getBalance(p.getName())));
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onSignChange(SignChangeEvent event){
        String type = event.getLine(0);
        if (type.contains("[License]") || type.contains("[Shop]") || type.contains("[Job]")){
          ShopSigns ss = new ShopSigns(plugin, event.getPlayer());
          ss.create(event);
        }
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        World world = player.getWorld();
        Block block = event.getBlock();
        if (world.getName().equalsIgnoreCase("Mine")){
            ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(player.getName());
            event.setCancelled(true);
            if (cp.getProfession() == TimeProfession.MINER){
                if (!cp.hasLicense(block.getType().getId()))//(plugin.prof_miner.getMinerBlacklist().contains(block.getType())){
                    player.sendMessage(plugin.getPluginName() + ChatColor.RED + "You need a " + ChatColor.BLUE + "license" + ChatColor.RED + " to obtain this material");
                else {
                    int earned = plugin.prof_miner.getSkillEarned(block.getType());
                    cp.updateSkill(TimeProfession.MINER, earned);
                    //event.setExpToDrop(earned);
                    //player.setExp((float)earned);
                    event.getBlock().setType(Material.AIR);
                    for (int i=0;i<earned;i++){
                        Location loc = block.getLocation();
                        loc.setY(loc.getY()+1);
                        ExperienceOrb orb = (ExperienceOrb) world.spawnEntity(loc, EntityType.EXPERIENCE_ORB);
                        orb.setExperience(1);
                    }
                }
            } else {
                player.sendMessage(plugin.getPluginName() + ChatColor.RED + "You need a to be a miner to obtain this material");
            }
        }
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerPickupItem(PlayerPickupItemEvent event){
        Player player = event.getPlayer();
        if (event.getItem().getType() == EntityType.EXPERIENCE_ORB){
            ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(player.getName());
            //event.setCancelled(true);
            //event.getItem().remove();
        }
    }
    
    
    public void onPlayerMove(PlayerMoveEvent event){
        //event.
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event){
        Block b = null;
        
        if (event.getAction()!=Action.RIGHT_CLICK_BLOCK)
            return;
        if (!event.hasBlock()) {
            try {
                b = event.getPlayer().getTargetBlock(null, 5);
            } catch (Exception e) {
                return;
            }
        } else
            b = event.getClickedBlock();
        
        if (b.getType().equals(Material.SIGN_POST) || b.getType().equals(Material.WALL_SIGN)) {
            Sign s = (Sign) b.getState();
            String type = s.getLine(0);
            if (type.contains("[License]") || type.contains("[Shop]")){
                ShopSigns ss = new ShopSigns(plugin, event.getPlayer());
                
                int cost = 0;
                Pattern p = Pattern.compile("-?\\d+");
                Matcher m = p.matcher(s.getLine(2));
                if (m.find()){
                    cost = Integer.valueOf(m.group());
                } else{
                    plugin.sendConsole("SignInteract event, error reading cost");
                    return;
                }
                ss.buy(b, cost);
            } else if (type.contains("[Job]")){
                ShopSigns ss = new ShopSigns(plugin, event.getPlayer());
                ss.buyProfession(s.getLine(1));
            }
        }
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event){
        Player p = event.getPlayer();
        ConfigPlayer cp = players.addPlayer(p);
        if (cp == null){
            plugin.sendConsole("Problematic: ConfigPlayer object exists for newly joined player named " + p.getName());
        } else
            cp.loadPlayer();
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        players.getPlayerConfig(player.getName()).savePlayer();
        players.removePlayer(player);
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onAttack(EntityDamageByEntityEvent event){
        if (!(event.getDamager() instanceof Player))
            return;
        if (!(event.getEntity() instanceof Player))
            return;
        
        Player player = (Player) event.getDamager();
        TimeProfession prof = plugin.getTimePlayers().getPlayerConfig(player.getName()).getProfession();
        
        //player.sendMessage(prof);
        if ((player.getItemInHand().getType() == Material.STICK) && (prof == TimeProfession.OFFICER)){
            Player defender = (Player) event.getEntity();
            Police police = new Police(plugin);
            police.arrestPlayer(player, defender);
        }
        
   
    }
    
    public boolean playerExists(String playername){
        for (OfflinePlayer player : plugin.getServer().getOfflinePlayers()) {
            if (player.getName().equalsIgnoreCase(playername)) {
              return true;
            }
        }
        return false;
    }
    
    public boolean checkPermissions(CommandSender sender, String permission, boolean sendMessage){
        if (!(sender instanceof Player))
            return true;
        if (sender.hasPermission("time." + permission))
            return true;
        if (sendMessage)
            sender.sendMessage(plugin.getPluginName() + "You do not have the permissions to do this.");
        return false;
    }

}
