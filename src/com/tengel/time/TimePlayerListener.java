/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.tengel.time.profs.Police;
import com.tengel.time.profs.TimeProfession;
import com.tengel.time.structures.TimeMonster;
import com.tengel.time.structures.TimePlayer;
import java.util.UUID;
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
import org.bukkit.event.player.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

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
        if (type.contains("[License]") || type.contains("[Buy]") || type.contains("[Job]") || type.contains("[Sell]")){
            String [] lines = event.getLines();
            TimePlayer tp = plugin.getPlayer(event.getPlayer().getName());
            TimeSigns ss = plugin.getShopSigns();
            Sign s = (Sign) event.getBlock().getState();
            event.setCancelled(true);
            for (int i=0;i<lines.length;i++)
                s.setLine(i, lines[i]);
            ss.create(tp, s);
        }
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onSignDestroy(BlockBreakEvent event){
        Block b = event.getBlock();
        if (b.getType().equals(Material.SIGN_POST) || b.getType().equals(Material.WALL_SIGN)) {
            Sign s = (Sign) b.getState();
            String type = s.getLine(0);
            if (type.contains("[License]") || type.contains("[Buy]") || type.contains("[Job]") || type.contains("[Sell]")){
                TimePlayer tp = plugin.getPlayer(event.getPlayer().getName());
                TimeSigns ss = plugin.getShopSigns();
                ss.remove(tp, s);
            }
        }
    }
    
    
    //@EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerThrowMonsterEgg(PlayerInteractEvent event){
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK){
            if(event.getItem().getType() == Material.MONSTER_EGG){
                Player p = event.getPlayer();
                if (plugin.getPlayer(p.getName()).getAdminMode()){
                    Block b = event.getClickedBlock();
                    if (b == null){
                        event.setCancelled(true);
                        return;
                    }
                    String monster = event.getItem().getData().toString();
                    Pattern pattern = Pattern.compile(".*\\{(.*)\\}");
                    Matcher matcher = pattern.matcher(monster);
                    if (matcher.find()) {
                        monster = matcher.group(1);
                        EntityType.valueOf(monster);
                        Location loc = b.getLocation();
                        plugin.getMobControl().addMonsterSpawn(loc, monster);
                        p.sendMessage(ChatColor.GREEN + monster + " spawn added");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onEnchant(EnchantItemEvent event){
        int cost = event.getExpLevelCost()*60;
        TimePlayer tp = plugin.getPlayer(event.getEnchanter().getName());
        if (tp.confirmEnchantment(event.getItem())){
            EconomyResponse er = plugin.getEconomy().withdrawPlayer(tp.getName(), cost);
            if (er.transactionSuccess()){
                event.setExpLevelCost(0);
            } else {
                tp.sendMessage(ChatColor.RED + "It seems you cannot afford this enchantment");
                event.setCancelled(true);
            }
                
            
        } else {
            tp.sendMessage(ChatColor.YELLOW + "Enchanting this item will cost you " + ChatColor.RED + cost + " days " +
                    ChatColor.YELLOW + " of life, to confirm try again");
            event.setCancelled(true);
        }
        
    }
    
    @EventHandler
    public void onWorldSave(WorldSaveEvent event){
        plugin.save();
        plugin.sendConsole("Time saved.");
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onGameModeChange(PlayerGameModeChangeEvent event){
        TimePlayer tp = plugin.getPlayer(event.getPlayer().getName());
        if (tp != null){
            try{
                tp.getPlayerInventory().switchInventory(event.getNewGameMode());
            }catch(Exception e){
                plugin.sendConsole("onGameModeChange: " + e.toString());
            }
        }
    }
    
    //@EventHandler(priority=EventPriority.NORMAL)
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
        if (level > 150) //limit abilities to max out at 150
            level = 150;
        //double hp = 0.1598*Math.pow(level,2.0)+10;
        //if (hp > 9999D) hp = 9999; //be sure to max HP at 9999
        //p.setMaxHealth(hp);
        //p.setHealthScale(20);
        float walkspeed = level*0.0010F+0.2F;
        //if (walkspeed > 0.425F) walkspeed = 0.425F; //approx level 150's speed is limit
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
    public void onPlayerLevelChange(PlayerLevelChangeEvent event){
        Player p = event.getPlayer();
        int oldLevel = event.getOldLevel();
        int newLevel = event.getNewLevel();
        if (oldLevel < newLevel)
            p.sendMessage(ChatColor.WHITE+"You advanced from level "+ChatColor.GRAY+oldLevel + ChatColor.WHITE+" to level "+ChatColor.GRAY+newLevel);
        else if (oldLevel > newLevel)
            p.sendMessage(ChatColor.WHITE+"You downgraded from level "+ChatColor.GRAY+oldLevel + ChatColor.WHITE+" to level "+ChatColor.GRAY+newLevel);
        setPlayerAttributes(p, newLevel);
        //p.setHealth(p.getMaxHealth());
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
    
    private void playerOutOfTime_message(Player p){
        p.sendMessage(ChatColor.RED + "You are in a crippled state and will need to obtain 24 hours before getting better.");
        p.sendMessage("HINT: " + ChatColor.GRAY + "To obtain time, perhaps sell off some of your assets (gold, diamond, emerald, or food)");
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerReSpawn(PlayerRespawnEvent event){
        Player p = event.getPlayer();
        TimePlayer tp = plugin.getPlayer(p.getName());
        tp.updatePlayer(p);
        updatePlayerScoreboardHealth(p);
        playerOutOfTime_message(p);
    }
    
    @EventHandler
    public void onPlayerDeath(EntityDeathEvent event){
        Entity e = event.getEntity();
        if (e instanceof Player){
            Player p = (Player) e;
            if (p.getGameMode().equals(GameMode.CREATIVE))
                p.getInventory().clear();
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
        
//        if (event.getAction()!=Action.RIGHT_CLICK_BLOCK)
//            return;
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
            
            if (type.contains("[License]")){
                TimePlayer tp = plugin.getPlayer(event.getPlayer().getName());
                TimeSigns ss = plugin.getShopSigns();
                ss.buyBlockLicense(tp, s);
                event.setCancelled(true);
            } else if (type.contains("[Buy]")){
                System.out.println("Hey!");
                boolean donate = (event.getAction()==Action.LEFT_CLICK_BLOCK);
                TimePlayer tp = plugin.getPlayer(event.getPlayer().getName());
                TimeSigns ss = plugin.getShopSigns();
                ss.buyItem(tp, s, donate);
                event.setCancelled(true);
            } else if (type.contains("[Job]")){
                TimePlayer tp = plugin.getPlayer(event.getPlayer().getName());
                TimeSigns ss = plugin.getShopSigns();
                ss.buyProfession(tp, s.getLine(1));
                event.setCancelled(true);
            } else if(type.contains("[Sell]")){
                TimePlayer tp = plugin.getPlayer(event.getPlayer().getName());
                TimeSigns ss = plugin.getShopSigns();
                ss.sellItem(tp, s);
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event){
        final Player p = event.getPlayer();
        final TimePlayer tp = plugin.addPlayer(p.getName());
        setPlayerAttributes(p);
        
        if (tp.hasDied())
            playerOutOfTime_message(p);
        else {
            if (p.getMaxHealth() != 20)
                p.setMaxHealth(20L);
            if (p.getHealth() > 20)
                p.setHealth(20L);
        }
        
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                setPlayerAttributes(p);
                updatePlayerScoreboardLevel(p);
                updatePlayerScoreboardHealth(p);
            }
        }, 20*1);
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event){
        Player p = event.getPlayer();
        TimePlayer tp = plugin.getPlayer(p.getName());
        tp.save();
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
    
    //@EventHandler(priority=EventPriority.NORMAL)
    public void onAttack(EntityDamageByEntityEvent event){
        if (!copArrest(event)){
            Entity attacker = event.getDamager();
            if (attacker instanceof Player){
                Player p = (Player) attacker;
                if (plugin.getPlayer(p.getName()).getAdminMode()){
                    UUID uuid = event.getEntity().getUniqueId();
                    TimeMonster monster = plugin.getMobControl().getTimeMonster(uuid);
                    if (monster != null){
                        String type = monster.getMonster().getType().name();
                        if(plugin.getMobControl().removeMonsterSpawn(monster.getSpawnLocation(), type)){
                            event.getEntity().remove();
                            plugin.getMobControl().removeTimeMonster(uuid);
                            p.sendMessage(ChatColor.RED+ type + " spawn removed!!");
                        } else {
                            p.sendMessage(ChatColor.RED+ "Failed to remove spawn "+type+"!");
                        }
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            
            else if (attacker instanceof Projectile)
                attacker = (Player) ((Projectile)attacker).getShooter();
            if (attacker == null)
                event.setCancelled(true);
            int lvl_attacker = plugin.getMobControl().getLevel(attacker);            
            event.setDamage(plugin.getMobControl().getDamage(lvl_attacker, event.getDamage()));
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
            sender.sendMessage("You do not have the permissions to do this.");
        return false;
    }

}
