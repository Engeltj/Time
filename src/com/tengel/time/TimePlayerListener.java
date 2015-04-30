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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
        OfflinePlayer p = event.getPlayer();
        String msg = event.getMessage();
        if (msg.equalsIgnoreCase("I love Depths")){
            p.getPlayer().sendMessage("Depths loves you!");
        }
        else if (msg.equalsIgnoreCase("balance"))
            p.getPlayer().sendMessage(Double.toString(plugin.getEconomy().getBalance(p)));
    }
    
    @EventHandler(priority= EventPriority.NORMAL)
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
        if (b.getType().equals(Material.SIGN) || b.getType().equals(Material.SIGN_POST) || b.getType().equals(Material.WALL_SIGN)) {
            Sign s = (Sign) b.getState();
            String type = s.getLine(0);
            if (type.contains("[License]") || type.contains("[Buy]") || type.contains("[Job]") || type.contains("[Sell]")){
                event.setCancelled(true);
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
        int cost_days = event.getExpLevelCost();
        int cost_secs = event.getExpLevelCost()*24*60*60;
        TimePlayer tp = plugin.getPlayer(event.getEnchanter().getName());
        if (tp.confirmEnchantment(event.getItem())){
            EconomyResponse er = plugin.getEconomy().withdrawPlayer(tp.getPlayer(), cost_secs);
            if (er.transactionSuccess()){
                event.setExpLevelCost(0);
            } else {
                tp.sendMessage(ChatColor.RED + "You require " + ChatColor.GRAY + Commands.convertSecondsToTime(cost_secs - tp.getBalance()) +
                        ChatColor.RED + " more time to do this.");
                event.setCancelled(true);
            }
                
            
        } else {
            tp.sendMessage(ChatColor.YELLOW + "Enchanting this item will cost you " + ChatColor.RED + cost_days + " days" +
                    ChatColor.YELLOW + " of life, to confirm try again");
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onRepair(PrepareItemCraftEvent event){
        if (event.isRepair()){
            //event.get
        }
    }
    
    @EventHandler
    public void onWorldSave(WorldSaveEvent event){
        plugin.save();
        plugin.sendConsole("Time saved via WorldSaveEvent.");
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onGameModeChange(PlayerGameModeChangeEvent event){
        TimePlayer tp = plugin.getPlayer(event.getPlayer().getName());
        if (tp != null && tp.isLoaded())
            tp.getPlayerInventory().switchInventory(event.getNewGameMode());
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
                if (!tp.hasBlockLicense(block))//(plugin.prof_miner.getMinerBlacklist().contains(block.getType())){
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
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerTouchRequired(InventoryClickEvent event){
        Inventory inv = event.getInventory();
        List<HumanEntity> viewers = inv.getViewers();
        if (viewers.size()>0){
            TimePlayer tp = plugin.getPlayer(viewers.get(0).getName());
            if (tp != null){
                if (tp.getJobs().contains(TimeProfession.OFFICER)){
                    ItemStack is = event.getCurrentItem();
                    if (is.getType().equals(Material.STICK)){
                        tp.sendMessage(ChatColor.RED + "You may not move this item, required for you to do your job!");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerDropRequired(PlayerDropItemEvent event){
        Player p = event.getPlayer();
        TimePlayer tp = plugin.getPlayer(p.getName());
        if (tp != null){
            ItemStack is = event.getItemDrop().getItemStack();
            if (tp.getJobs().contains(TimeProfession.OFFICER)){
                if (is.getType().equals(Material.STICK) && is.getItemMeta().getDisplayName() != null && is.getItemMeta().getDisplayName().equals("Baton")){ 
                    tp.sendMessage(ChatColor.RED + "You may not move this item, required for you to do your job!");
                    event.setCancelled(true);
                }
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
    
    public void setPlayerSpeed(Player p, int level){
        float walkspeed = level*0.0010F+0.2F;
        if (walkspeed < 0.21F) 
            walkspeed=0.21F;
        p.setWalkSpeed(walkspeed);
    }
    
    public void setPlayerAttributes(Player p, int level){
        if (level > 150) //limit abilities to max out at 150
            level = 150;
        //double hp = 0.1598*Math.pow(level,2.0)+10;
        //if (hp > 9999D) hp = 9999; //be sure to max HP at 9999
        //p.setMaxHealth(hp);
        //p.setHealthScale(20);
        setPlayerSpeed(p, level);
    }
    
    public void setPlayerAttributes(Player p){
        setPlayerAttributes(p, p.getLevel());
    }
    
    private void updatePlayerScoreboardHealth(Player p){
        Scoreboard board = plugin.getServer().getScoreboardManager().getMainScoreboard();
        Objective obj = board.getObjective(DisplaySlot.BELOW_NAME);
        obj.getScore(p.getName()).setScore((int) Math.round(p.getHealth()/p.getMaxHealth()*100));
    }
    
    private void updatePlayerScoreboardLevel(Player p){
        Scoreboard board = plugin.getServer().getScoreboardManager().getMainScoreboard();
        board.getObjective(DisplaySlot.PLAYER_LIST).getScore(p.getName()).setScore(p.getLevel());
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
        final Player p = event.getPlayer();
        TimePlayer tp = plugin.getPlayer(p.getName());
        tp.getPlayerInventory().reloadInventory();
        tp.updatePlayer(p);
        updatePlayerScoreboardHealth(p);
        if (tp.hasDied())
            playerOutOfTime_message(p);
        if (tp.isJailed()){
            final int zone = tp.getZone();
            plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                public void run() {
                    Location loc_jail = plugin.getLocation(zone, "jail");
                    if (loc_jail != null)
                       p.teleport(loc_jail);
                }
            }, 20*5);
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        Player killer = event.getEntity().getKiller();
        Player p = event.getEntity();
        TimePlayer tp = plugin.getPlayer(p.getName());
        if (tp.isJailed()){ //cant die in jail, death cancel method
            p.setHealth(1D);
            event.setDeathMessage(null); 
            return;
        }
        event.setKeepLevel(true);
        event.setDroppedExp(0);
        
        tp.getPlayerInventory().performSerialization();
        for (ItemStack is : event.getDrops())
            is.setType(Material.AIR);
            
        float exp = p.getExp();
        float exp_lost = exp * 0.05f;
        p.setExp(exp - exp_lost);
        float balance = tp.getBalance();
        float balance_lost = 0;
        if (!tp.hasDied())
            balance_lost = (float) Math.floor(balance * 0.05);
        plugin.getEconomy().withdrawPlayer(tp.getPlayer(), balance_lost);
        
        if (killer != null){
            TimePlayer tp_killer = plugin.getPlayer(killer.getName());
            tp_killer.sendMessage("You've leeched " + ChatColor.GREEN + Commands.convertSecondsToTime(balance_lost) + ChatColor.RESET + " from this kill");
            if (balance_lost < 30*60)
                balance_lost = 30*60;
            tp_killer.addBounty(balance_lost*2);
            event.setDeathMessage(ChatColor.RED + "" + ChatColor.BOLD + killer.getName() + ChatColor.RESET + 
                    ChatColor.RED + " has murdered " + ChatColor.BOLD + tp.getName());
        } else
            event.setDeathMessage(null);
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event){
        Player p = event.getPlayer();
        if (p.getWalkSpeed() < 0.21)
            setPlayerAttributes(p);
    }
    
    @EventHandler(priority=EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event){
        Block b;
        TimePlayer tp = plugin.getPlayer(event.getPlayer().getName());
        if (tp != null && !tp.getAdminMode()){
            if (!event.hasBlock()) {
                try {
                    Set<Material> set = new HashSet<Material>();
                    set.add(Material.AIR);
                    b = event.getPlayer().getTargetBlock(set, 5);
                } catch (Exception e) {
                    return;
                }
            } else
                b = event.getClickedBlock();

            if (b.getType().equals(Material.SIGN_POST) || b.getType().equals(Material.WALL_SIGN)) {
                Sign s = (Sign) b.getState();
                String type = s.getLine(0);

                if (type.contains("[License]")){
                    TimeSigns ss = plugin.getShopSigns();
                    ss.buyBlockLicense(tp, s);
                    event.setCancelled(true);
                } else if (type.contains("[Buy]")){
                    boolean donate = (event.getAction()==Action.LEFT_CLICK_BLOCK);
                    TimeSigns ss = plugin.getShopSigns();
                    ss.buyItem(tp, s, donate);
                    event.setCancelled(true);
                } else if (type.contains("[Job]")){
                    TimeSigns ss = plugin.getShopSigns();
                    ss.buyProfession(tp, s.getLine(1));
                    event.setCancelled(true);
                } else if(type.contains("[Sell]")){
                    TimeSigns ss = plugin.getShopSigns();
                    ss.sellItem(tp, s);
                    event.setCancelled(true);
                }
            } else if (b.getType().equals(Material.ANVIL) && event.getAction() == Action.RIGHT_CLICK_BLOCK){
                tp.sendMessage(ChatColor.RED + "Anvils are currently disabled, repair system is in development, please be patient! (Keep your damaged items)");
                event.setCancelled(true);
            } else if (b.getType().equals(Material.STONE_PLATE)){
                Location loc = b.getLocation();
                if (loc.getBlockX() == 39 && loc.getBlockY() == 65 && loc.getBlockZ() == 35){
                    tp.sendMessage("Promotion.");
                }
            }
        }
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event){
        final Player p = event.getPlayer();
        final TimePlayer tp = plugin.addPlayer(p.getName());
        setPlayerAttributes(p);
        
        if (tp.hasDied()){
            playerOutOfTime_message(p);
            if (p.getMaxHealth() != 2)
                p.setMaxHealth(2L);
        } else {
            if (p.getMaxHealth() != 20)
                p.setMaxHealth(20L);
        }
        
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                setPlayerAttributes(p);
                updatePlayerScoreboardLevel(p);
                updatePlayerScoreboardHealth(p);
                p.sendMessage(ChatColor.GRAY+""+ChatColor.BOLD+"====> This server is Time injected <====");
                p.sendMessage(ChatColor.GRAY+"The currency is represented by a countdown timer to your death. You may live forever so long as you upkeep your clock!");
            }
        }, 20*1);
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event){
        Player p = event.getPlayer();
        TimePlayer tp = plugin.getPlayer(p.getName());
        tp.save();
        plugin.sendConsole(tp.getName() + " saved.");
        plugin.removePlayer(p.getName());
    }
    
    @EventHandler(priority=EventPriority.HIGH)
    public void onTeleportWhileJailed(PlayerTeleportEvent event){
        if (event.getCause() == TeleportCause.COMMAND){
            Player p = event.getPlayer();
            TimePlayer tp = plugin.getPlayer(p.getName());
            if (tp.isJailed()){
                tp.sendMessage(ChatColor.RED + "You may not teleport anywhere while in jail");
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public boolean copArrest(EntityDamageByEntityEvent event){
        if (!(event.getDamager() instanceof Player))
            return false;
        if (!(event.getEntity() instanceof Player))
            return false;
        
        Player p = (Player) event.getDamager();
        TimePlayer tp = plugin.getPlayer(p.getName());
        if (tp.hasJob(TimeProfession.OFFICER)){
            ItemStack is = p.getItemInHand();
            if ((is.getType() == Material.STICK) && (is.getItemMeta().getDisplayName().equals("Baton"))){
                Player defender = (Player) event.getEntity();
                Police police = new Police(plugin);
                police.arrestPlayer(p, defender);
                return true;
            }
        }
        return false;
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
            double dmg = event.getDamage();
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
