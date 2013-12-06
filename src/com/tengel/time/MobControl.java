/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Tim
 */
public class MobControl implements Listener {
    private final Time plugin;
    private final World world;
    //private RegionManager rg_control;
    
    public MobControl(Time plugin, World world){
        this.plugin = plugin;
        this.world = world;
        //rg_control = new RegionControl();
    }
    
    public boolean createSpawn(CommandSender sender, int difficulty){
        Player p = plugin.getServer().getPlayer(sender.getName());
        int i = 1;
        WorldGuardUtil wgu = new WorldGuardUtil(plugin, p.getWorld());
        Map<String, ProtectedRegion> map = plugin.worldGuard.getRegionManager(world).getRegions();
        String name = "spawn_" + difficulty + "_";
        
        while (map.get(name+plugin.intToString(i, 3)) != null)
            i++;
        return (wgu.createRegionFromSelection(p, name+plugin.intToString(i, 3)) != null);
    }
    
    
    private void setArmours(LivingEntity creature, int zone, int difficulty){
        Random r_gen = new Random();
        ItemStack helmet;
        ItemStack chest;
        ItemStack legs;
        ItemStack boots;
        switch(zone){
            case 2:
                if (r_gen.nextInt(5) <= difficulty) helmet = new ItemStack(Material.DIAMOND_HELMET);
                else helmet = new ItemStack(Material.GOLD_HELMET);
                if (r_gen.nextInt(5) <= difficulty) chest = new ItemStack(Material.DIAMOND_CHESTPLATE);
                else chest = new ItemStack(Material.GOLD_CHESTPLATE);
                if (r_gen.nextInt(5) <= difficulty) legs = new ItemStack(Material.DIAMOND_LEGGINGS);
                else legs = new ItemStack(Material.GOLD_LEGGINGS);
                if (r_gen.nextInt(5) <= difficulty) boots = new ItemStack(Material.DIAMOND_BOOTS);
                else boots = new ItemStack(Material.GOLD_BOOTS);
                break;
            case 1:
                if (r_gen.nextInt(5) <= difficulty) helmet = new ItemStack(Material.IRON_HELMET);
                else helmet = new ItemStack(Material.GOLD_HELMET);
                if (r_gen.nextInt(5) <= difficulty) chest = new ItemStack(Material.IRON_CHESTPLATE);
                else chest = new ItemStack(Material.GOLD_CHESTPLATE);
                if (r_gen.nextInt(5) <= difficulty) legs = new ItemStack(Material.IRON_LEGGINGS);
                else legs = new ItemStack(Material.GOLD_LEGGINGS);
                if (r_gen.nextInt(5) <= difficulty) boots = new ItemStack(Material.IRON_BOOTS);
                else boots = new ItemStack(Material.GOLD_BOOTS);
                break;
            default:
                if (r_gen.nextInt(5) <= difficulty) helmet = new ItemStack(Material.LEATHER_HELMET);
                else helmet = new ItemStack(Material.CHAINMAIL_HELMET);
                if (r_gen.nextInt(5) <= difficulty) chest = new ItemStack(Material.LEATHER_CHESTPLATE);
                else chest = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
                if (r_gen.nextInt(5) <= difficulty) legs = new ItemStack(Material.LEATHER_LEGGINGS);
                else legs = new ItemStack(Material.CHAINMAIL_LEGGINGS);
                if (r_gen.nextInt(5) <= difficulty) boots = new ItemStack(Material.LEATHER_BOOTS);
                else boots = new ItemStack(Material.CHAINMAIL_BOOTS);
                break;
        }
        
        if (r_gen.nextInt(10)> 2)
            setHelmet(creature, helmet);
        if (r_gen.nextInt(10)> 2)
            setChestplate(creature, chest);
        if (r_gen.nextInt(10)> 2)
            setLeggings(creature, legs);
        if (r_gen.nextInt(10)> 2)
            setBoots(creature, boots);
    }
    
    public String getName(LivingEntity creature){
        String name = creature.getType().name();
        name = name.toLowerCase();
        name = WordUtils.capitalize(name);
        return name.replaceAll("_", " ");
    }
    
    public void setHealth(LivingEntity creature, int zone, int difficulty){
        String name = getName(creature);
        Random r_gen = new Random();
        double multiplier = r_gen.nextDouble()+0.5;
        int level = (zone * 25 + 1) + ((difficulty-1)*5) + (r_gen.nextInt(5)-5);
        //plugin.sendConsole(String.valueOf(zone) + " " + String.valueOf(difficulty));
        double newhp = creature.getHealth() * level * multiplier;
        if (multiplier > 1.2)
            creature.setCustomName(ChatColor.RED + "[Lvl "+level+"] " + ChatColor.WHITE + name);
        else if (multiplier > 0.8)
            creature.setCustomName(ChatColor.YELLOW + "[Lvl "+level+"] " + ChatColor.WHITE + name);
        else
            creature.setCustomName(ChatColor.GREEN + "[Lvl "+level+"] " + ChatColor.WHITE + name);
        creature.setCustomNameVisible(true);
        creature.setMaxHealth(newhp);
        creature.setHealth(newhp*0.999);
        creature.setRemoveWhenFarAway(false);
    }
    
    public int getSpawnDifficulty(String region){
        Pattern p = Pattern.compile("-?\\d+");
        Matcher m = p.matcher(region);
        int level = 1;
        try {
            if (m.find())
                level = Integer.parseInt(m.group());
        } catch (Exception ignored){}
        
        return level;
    }
    
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity creature = event.getEntity();
        
        if(!(creature instanceof Player)) {
            if ((event.getLocation().getWorld().getName().equalsIgnoreCase("Time") && (event.getSpawnReason() == SpawnReason.NATURAL)))
                event.setCancelled(true);
            RegionManager mgr = plugin.worldGuard.getRegionManager(event.getLocation().getWorld());
            for (ProtectedRegion rg : mgr.getApplicableRegions(event.getLocation())){
                if (rg.getId().contains("spawn_")){
                    for (Player p : plugin.getServer().getOnlinePlayers()){
                        Location loc = p.getLocation();
                        Vector v = new Vector(loc.getX(),loc.getY(),loc.getZ());
                        if (rg.contains(v)){
                            event.setCancelled(true);
                        }
                    }
                    int zone = plugin.getRegionControl().getZoneId(event.getLocation());
                    int difficulty = getSpawnDifficulty(rg.getId());
                    setArmours(creature, zone, difficulty);
                    setHealth(creature, zone, difficulty);
                    PotionEffect pe = new PotionEffect(PotionEffectType.SPEED,99999,zone);
                    event.getEntity().addPotionEffect(pe);
                    //creature
                    //plugin.sendConsole("Armors set");
                    return;
                }
            } 
        }
    }
    
    @EventHandler
    public void onCombust(EntityCombustEvent event){
        event.setCancelled(true);
    }
    
    public double getDamage(int level, double regularHit){
        return ((0.1598*Math.pow(level,2)+10.0)/8.0*(regularHit/10));
    }
    
    public int getLevel(Entity creature){
        if (creature instanceof Player){
            Player p = (Player) creature;
            return p.getLevel();
        } else {
            LivingEntity mob = (LivingEntity) creature;
            String name = mob.getCustomName();
            if (name != null && name.length() >0){
                Pattern p = Pattern.compile("-?\\d+");
                Matcher m = p.matcher(mob.getCustomName());
                if (m.find())
                    return Integer.valueOf(m.group());
            }
        }
        return 0;
    }
    
    
    public static void setHelmet(LivingEntity e, ItemStack helmet){
         EntityEquipment ee = e.getEquipment();
         ee.setHelmet(helmet);
         ee.setHelmetDropChance(0);
    }
    public static void setChestplate(LivingEntity e, ItemStack chest){
         EntityEquipment ee = e.getEquipment();
         ee.setChestplate(chest);
         ee.setChestplateDropChance(0);
    }
    public static void setBoots(LivingEntity e, ItemStack boots){
         EntityEquipment ee = e.getEquipment();
         ee.setBoots(boots);
         ee.setBootsDropChance(0);
    }
    public static void setLeggings(LivingEntity e, ItemStack legs){
         EntityEquipment ee = e.getEquipment();
         ee.setLeggings(legs);
         ee.setLeggingsDropChance(0);
    }
    
    public static ItemStack getHelmet(LivingEntity e){
        EntityEquipment ee = e.getEquipment();
        return ee.getHelmet();
    }
    public static ItemStack getChestplate(LivingEntity e){
        EntityEquipment ee = e.getEquipment();
        return ee.getChestplate();
    }
    public static ItemStack getLeggings(LivingEntity e){
        EntityEquipment ee = e.getEquipment();
        return ee.getLeggings();
    }
    public static ItemStack getBoots(LivingEntity e){
        EntityEquipment ee = e.getEquipment();
        return ee.getBoots();
    }
}
