/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Random;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.inventory.EntityEquipment;

/**
 *
 * @author Tim
 */
public class MobControl implements Listener {
    private final Time plugin;
    //private RegionManager rg_control;
    
    public MobControl(Time plugin){
        this.plugin = plugin;
        //rg_control = new RegionControl();
    }
    
    
    private void setArmours(LivingEntity creature, int zone){
        Random r_gen = new Random();
        ItemStack helmet;
        ItemStack chest;
        ItemStack legs;
        ItemStack boots;
        switch(zone){
            case 2:
                if (r_gen.nextInt(10)> 4) helmet = new ItemStack(Material.DIAMOND_HELMET);
                else helmet = new ItemStack(Material.GOLD_HELMET);
                if (r_gen.nextInt(10)> 4) chest = new ItemStack(Material.DIAMOND_CHESTPLATE);
                else chest = new ItemStack(Material.GOLD_CHESTPLATE);
                if (r_gen.nextInt(10)> 4) legs = new ItemStack(Material.DIAMOND_LEGGINGS);
                else legs = new ItemStack(Material.GOLD_LEGGINGS);
                if (r_gen.nextInt(10)> 4) boots = new ItemStack(Material.DIAMOND_BOOTS);
                else boots = new ItemStack(Material.GOLD_BOOTS);
                break;
            case 1:
                if (r_gen.nextInt(10)> 4) helmet = new ItemStack(Material.IRON_HELMET);
                else helmet = new ItemStack(Material.GOLD_HELMET);
                if (r_gen.nextInt(10)> 4) chest = new ItemStack(Material.IRON_CHESTPLATE);
                else chest = new ItemStack(Material.GOLD_CHESTPLATE);
                if (r_gen.nextInt(10)> 4) legs = new ItemStack(Material.IRON_LEGGINGS);
                else legs = new ItemStack(Material.GOLD_LEGGINGS);
                if (r_gen.nextInt(10)> 4) boots = new ItemStack(Material.IRON_BOOTS);
                else boots = new ItemStack(Material.GOLD_BOOTS);
                break;
            default:
                if (r_gen.nextInt(10)> 4) helmet = new ItemStack(Material.LEATHER_HELMET);
                else helmet = new ItemStack(Material.CHAINMAIL_HELMET);
                if (r_gen.nextInt(10)> 4) chest = new ItemStack(Material.LEATHER_CHESTPLATE);
                else chest = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
                if (r_gen.nextInt(10)> 4) legs = new ItemStack(Material.LEATHER_LEGGINGS);
                else legs = new ItemStack(Material.CHAINMAIL_LEGGINGS);
                if (r_gen.nextInt(10)> 4) boots = new ItemStack(Material.LEATHER_BOOTS);
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
        String name_fix = name.replaceAll("_", " ");
        return name_fix;
    }
    
    public void setHealth(LivingEntity creature, int zone){
        String name = getName(creature);
        Random r_gen = new Random();
        double multiplier = r_gen.nextDouble();
        double newhp = creature.getHealth()*(multiplier*zone +1);
        if (multiplier > 0.8)
            creature.setCustomName(ChatColor.RED + "[Elite] " + ChatColor.YELLOW + name);
        else
            creature.setCustomName(ChatColor.YELLOW + name);
        creature.setCustomNameVisible(true);
        creature.setHealth(newhp);
        creature.setMaxHealth(newhp);
    }
    
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity creature = event.getEntity();
        if(!(creature instanceof Player)) {
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
                    setArmours(creature, zone);
                    setHealth(creature, zone);
                    //plugin.sendConsole("Armors set");
                    return;
                }
            }
            if (event.getLocation().getWorld().getName().equalsIgnoreCase("Time"))
                event.setCancelled(true);
                //creature.remove();
                
        }
    }
    
    @EventHandler
    public void onCombust(EntityCombustEvent event){
        event.setCancelled(true);
    }
    
    
    public static void setHelmet(LivingEntity e, ItemStack helmet){
         EntityEquipment ee = e.getEquipment();
         ee.setHelmet(helmet);
         ee.setHelmetDropChance(0);
    }
    public static void setBoots(LivingEntity e, ItemStack boots){
         EntityEquipment ee = e.getEquipment();
         ee.setBoots(boots);
         ee.setBootsDropChance(0);
    }
    public static void setChestplate(LivingEntity e, ItemStack chest){
         EntityEquipment ee = e.getEquipment();
         ee.setChestplate(chest);
         ee.setChestplateDropChance(0);
    }
    public static void setLeggings(LivingEntity e, ItemStack legs){
         EntityEquipment ee = e.getEquipment();
         ee.setLeggings(legs);
         ee.setLeggingsDropChance(0);
    }
}
