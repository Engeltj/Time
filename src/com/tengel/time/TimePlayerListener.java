/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.tengel.time.profs.Police;
import com.tengel.time.profs.TimeProfession;
import com.tengel.time.structures.TimePlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

/**
 *
 * @author Tim
 */
public class TimePlayerListener implements Listener {
    private final Time plugin;
    
    public TimePlayerListener(Time plugin) {
            this.plugin = plugin;
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
            TimePlayer tp = plugin.getPlayer(player.getName());
            event.setCancelled(true);
            if (tp.hasJob(TimeProfession.MINER)){
                if (!tp.hasBlockLicense(block.getType().getId()))//(plugin.prof_miner.getMinerBlacklist().contains(block.getType())){
                    player.sendMessage(ChatColor.RED + "You need a " + ChatColor.BLUE + "license" + ChatColor.RED + " to obtain this material");
                else {
                    int earned = plugin.prof_miner.getSkillEarned(block.getType());
                    tp.addSkill(TimeProfession.MINER, earned);
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
                player.sendMessage(ChatColor.RED + "You need a to be a miner to obtain this material");
            }
        }
    }
    
    /*@EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerPickupItem(PlayerPickupItemEvent event){
        Player player = event.getPlayer();
        if (event.getItem().getType() == EntityType.EXPERIENCE_ORB){
            //ConfigPlayer cp = plugin.getTimePlayers().getPlayerConfig(player.getName());
            //event.setCancelled(true);
            //event.getItem().remove();
        }
    }*/
    
    public void setPlayerAttributes(Player p, int level){
        double hp = 0.1598*Math.pow(level,2.0)+10;
        if (hp > 9999D) hp = 9999; //be sure to max HP at 9999
        p.setMaxHealth(hp);
        p.setHealthScale(20);
        float walkspeed = level*0.0015F+0.2F;
        if (walkspeed > 0.425F) walkspeed = 0.425F; //approx level 150's speed is limit
        if (walkspeed < 0.21F) walkspeed=0.21F;
        p.setWalkSpeed(walkspeed);
    }
    
    public void setPlayerAttributes(Player p){
        setPlayerAttributes(p, p.getLevel());
    }
    
    private void updatePlayerScoreboardHealth(Player p){
        Scoreboard board = plugin.getServer().getScoreboardManager().getMainScoreboard();
        Objective obj = board.getObjective(DisplaySlot.BELOW_NAME);
        obj.getScore(p).setScore((int) Math.round(p.getHealth()/p.getMaxHealth()*100));        
    }
    
    private void updatePlayerScoreboardLevel(Player p){
        Scoreboard board = plugin.getServer().getScoreboardManager().getMainScoreboard();
        board.getObjective(DisplaySlot.PLAYER_LIST).getScore(p).setScore(p.getLevel());
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerLevelUp(PlayerLevelChangeEvent event){
        Player p = event.getPlayer();
        int oldLevel = event.getOldLevel();
        int newLevel = event.getNewLevel();
        p.sendMessage(ChatColor.WHITE+"You advanced from level "+ChatColor.GRAY+oldLevel + ChatColor.WHITE+" to level "+ChatColor.GRAY+newLevel);
        setPlayerAttributes(p, newLevel);
        p.setHealth(p.getMaxHealth());
        updatePlayerScoreboardHealth(p);
        updatePlayerScoreboardLevel(p);
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerHealthRegen(EntityRegainHealthEvent event){
        Entity ent = event.getEntity();
        if (ent instanceof Player){
            Player p = (Player) ent;
            updatePlayerScoreboardHealth(p);
        }
            
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerHealthDamaged(EntityDamageEvent event){
        Entity ent = event.getEntity();
        if (ent instanceof Player){
            Player p = (Player) ent;
            updatePlayerScoreboardHealth(p);
        }
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event){
        Player p = event.getPlayer();
        if (p.getWalkSpeed() < 0.21)
            setPlayerAttributes(p);
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event){
        Block b;
        
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
                
                int cost;
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
        plugin.addPlayer(p.getName());
        setPlayerAttributes(p);
        updatePlayerScoreboardLevel(p);
        updatePlayerScoreboardHealth(p);
    }
    
    /*@EventHandler
    public void onNameTag(PlayerReceiveNameTagEvent event) {
        Player player = event.getNamedPlayer();
        if (player.getName().equalsIgnoreCase("Engeltj"))
            event.setTag("Notch");
    }*/
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event){
        Player p = event.getPlayer();
        plugin.getPlayer(p.getName()).save();
        plugin.removePlayer(p.getName());
    }
    
    public boolean copArrest(EntityDamageByEntityEvent event){
        if (!(event.getDamager() instanceof Player))
            return false;
        if (!(event.getEntity() instanceof Player))
            return false;
        
        Player p = (Player) event.getDamager();
        TimePlayer tp = plugin.getPlayer(p.getName());
        
        if ((p.getItemInHand().getType() == Material.STICK) && (tp.hasJob(TimeProfession.OFFICER))){
            Player defender = (Player) event.getEntity();
            Police police = new Police(plugin);
            police.arrestPlayer(p, defender);
            return true;
        } else return false;
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onAttack(EntityDamageByEntityEvent event){
        if (!copArrest(event)){
            Entity attacker = event.getDamager();
            
            if (attacker instanceof Projectile)
                attacker = ((Projectile)attacker).getShooter();
            //Entity defender = event.getEntity();
            if (attacker == null)
                event.setCancelled(true);
            int lvl_attacker = plugin.getMobControl().getLevel(attacker);
            //int lvl_defender = plugin.mobcontrol.getLevel(defender);
            
            event.setDamage(plugin.getMobControl().getDamage(lvl_attacker, event.getDamage()));
        }
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onDeath(EntityDeathEvent event){
        double lvl_dead = plugin.getMobControl().getLevel(event.getEntity());
        int exp = (int) Math.ceil(lvl_dead/12.0);
        event.setDroppedExp(exp);
        
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
            sender.sendMessage("You do not have the permissions to do this.");
        return false;
    }

}
