/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;


import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitCommandSender;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.registry.WorldData;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

/**
 *
 * @author Tim
 */

public class WorldGuardUtil {
    private static WorldGuardPlugin wgp;
    private final Time plugin;
    private final World world;
    public static boolean hasWorldGuard = false;
    static Map<String,Set<String>> trackedRegions = new ConcurrentHashMap<String,Set<String>>();

    public WorldGuardUtil(Time plugin, World world){
        wgp = plugin.worldGuard;
        this.world = world;
        this.plugin = plugin;
    }

    public void updateBuildWorth(ArrayList<String> schematics){
        for (String schematic : schematics)
            updateBuildWorth(schematic);
    }

    public boolean updateBuildWorth(String schematic){
        Clipboard cc = getClipboard(schematic);
        if (cc == null){
            plugin.sendConsole("schematic file '"+schematic+"' seems to be missing.. failed to update home");
            return false;
        }
        Vector size = cc.getSize();
        int earnings = 0;
        for (int x=0;x<size.getX();x++)
            for (int y=0;y<size.getY();y++)
                for (int z=0;z<size.getZ();z++){
                    Vector pos = new Vector(x,y,z);
                    earnings = earnings + plugin.prof_miner.getBlockWorth(cc.getBlock(pos).);
                }

        Connection con = plugin.getSql().getConnection();
        Statement st;
        try {
            st = con.createStatement();
            int updated = st.executeUpdate("UPDATE `schematics` SET worth="+earnings*5+" WHERE filename='"+schematic+"';");
            return (updated > 0);
        } catch (Exception ex) {
           plugin.sendConsole("Failed to update worth for '"+schematic+"' in updateBuildWorth\n" + ex);
        }
        return false;
    }

    //public ProtectedRegion updateProtectedRegion(String playerName, Location start, Location end) throws Exception {
    //	return createRegion(playerName, start, end);
    //}

    public ProtectedRegion createBuildRegion(String player, Location start, Location end) throws Exception{
        String id = "buildplot_"+player;
        RegionManager mgr = wgp.getRegionContainer().get(world);
        mgr.removeRegion(id);
        ProtectedRegion region;

        BlockVector bv_start = new BlockVector(start.getX(), start.getY(), start.getZ());
        BlockVector bv_end = new BlockVector(end.getX(), end.getY(), end.getZ());
        region = new ProtectedCuboidRegion(id, bv_start, bv_end);
        region.setPriority(11); /// some relatively high priority
        //region.setFlag(DefaultFlag.PVP,State.ALLOW);
        mgr.addRegion(region);
        DefaultDomain dd = region.getOwners();
        dd.addPlayer(player);
        region.setOwners(dd);
        mgr.save();
        return region;
    }

    public ProtectedRegion createRegion(String name, Location start, Location end) throws Exception{
        String id = name;
        RegionManager mgr = wgp.getRegionManager(start.getWorld());
        mgr.removeRegion(id);
        ProtectedRegion region;
        BlockVector bv_start = new BlockVector(start.getX(), start.getY(), start.getZ());
        BlockVector bv_end = new BlockVector(end.getX(), end.getY(), end.getZ());
        region = new ProtectedCuboidRegion(id, bv_start, bv_end);
        //region.setPriority(11); /// some relatively high priority
        //region.setFlag(DefaultFlag.PVP,State.ALLOW);
        mgr.addRegion(region);

        mgr.save();
        return region;
    }
    
    public com.sk89q.worldedit.BlockVector convertToSk89qBV(Location location){
        return new com.sk89q.worldedit.BlockVector(location.getX(),location.getY(),location.getZ());
    }

    public ProtectedRegion createRegionFromSelection(Player p, String rgName){

        RegionManager mgr = wgp.getRegionContainer().get(world);
        WorldEditPlugin wep = plugin.worldEdit;
        //final LocalSession session = wep.getSession(p);
        //LocalWorld w = BukkitUtil.getLocalWorld(p.getWorld());
        ProtectedRegion region=null;
        try {
            Selection sel = wep.getSelection(p);
            if (sel instanceof Polygonal2DSelection) {
                Polygonal2DSelection polySel = (Polygonal2DSelection) sel;
                int minY = polySel.getNativeMinimumPoint().getBlockY();
                int maxY = polySel.getNativeMaximumPoint().getBlockY();
                region = new ProtectedPolygonalRegion(rgName, polySel.getNativePoints(), minY, maxY);
                System.out.println("POLY!!");
            } else { /// default everything to cuboid
                region = new ProtectedCuboidRegion(rgName, convertToSk89qBV(sel.getMinimumPoint()), convertToSk89qBV(sel.getMaximumPoint()));
                System.out.println("CUBOID!!");
            }
            region.setPriority(11); /// some relatively high priority
            wgp.getRegionManager(world).addRegion(region);
            mgr.save();
        } catch (Exception e){
            System.out.println("createRegionFromSelection encountered error: " + e);
        }
        return region;
    }


    public static void clearRegion(ProtectedRegion pr, String world) {
        World w = Bukkit.getWorld(world);
        if (w==null)
                return;
        if (pr == null)
                return;

        Location l;
        for (Item entity: w.getEntitiesByClass(Item.class)) {
            l = entity.getLocation();
            if (pr.contains(l.getBlockX(), l.getBlockY(), l.getBlockZ())){
                    entity.remove();
            }
        }
    }

    public static boolean isLeavingArea(ProtectedRegion pr, final Location from, final Location to) {
    return pr != null && (!pr.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ()) && pr.contains(from.getBlockX(), from.getBlockY(), from.getBlockZ()));
    }

    public static Flag<?> getWGFlag(String flagString){
            for (Flag<?> f: DefaultFlag.getFlags()){
                    if (f.getName().equalsIgnoreCase(flagString)){
                            return f;
                    }
            }
            throw new IllegalStateException("Worldguard flag " + flagString +" not found");
    }
    public static StateFlag getStateFlag(String flagString){
            for (Flag<?> f: DefaultFlag.getFlags()){
                    if (f.getName().equalsIgnoreCase(flagString) && f instanceof StateFlag){
                            return (StateFlag) f;
                    }
            }
            throw new IllegalStateException("Worldguard flag " + flagString +" not found");
    }

    public static boolean setFlag(ProtectedRegion pr, String worldName, String flag, boolean enable) {
            World w = Bukkit.getWorld(worldName);
            if (w == null)
                    return false;
            if (pr == null)
                    return false;
            StateFlag f = getStateFlag(flag);
            State newState = enable ? State.ALLOW : State.DENY;
            State state = pr.getFlag(f);

            if (state == null || state != newState){
                    pr.setFlag(f, newState);}
            return true;
    }

    public static boolean allowEntry(ProtectedRegion pr, Player player, String regionWorld) {
            World w = Bukkit.getWorld(regionWorld);
            if (w == null)
                    return false;
            if (pr == null)
                    return false;
            DefaultDomain dd = pr.getMembers();
            dd.addPlayer(player.getName());
            pr.setMembers(dd);
            return true;
    }

    public static boolean addMember(ProtectedRegion pr, String playerName, String regionWorld) {
            return changeMember(pr,playerName,regionWorld,true);
    }

    public static boolean removeMember(ProtectedRegion pr, String playerName, String regionWorld) {
            return changeMember(pr,playerName,regionWorld,false);
    }

    private static boolean changeMember(ProtectedRegion pr, String name, String regionWorld, boolean add){
            World w = Bukkit.getWorld(regionWorld);
            if (w == null)
                    return false;
            if (pr == null)
                    return false;

            DefaultDomain dd = pr.getMembers();
            if (add){
                    dd.addPlayer(name);
            } else {
                    dd.removePlayer(name);
            }
            pr.setMembers(dd);
            return true;
    }

    public void deleteRegion(String region) {
            RegionManager mgr = wgp.getRegionManager(world);
            if (mgr == null)
                    return;
            mgr.removeRegion(region);
    }

    public void pasteFirstLayer(Player p, ProtectedRegion pr, String schematic){
        final WorldEditPlugin wep = plugin.worldEdit;
        wep.getWorldEdit().
//        Player bcs = new ConsolePlayer(wep, Bukkit.getConsoleSender(), world);
        final LocalSession session = wep.getSession(p);
        session.setUseInventory(false);
        EditSession editSession = wep.createEditSession(p);
        Vector pos = new Vector(pr.getMinimumPoint());
        try {
            session.setClipboard(null);
            session.setClipboard(getClipboard(p, schematic));
            session.getClipboard().paste(editSession, pos, false, false);  
            for (double x= pr.getMinimumPoint().getX(); x < pr.getMaximumPoint().getX(); x++){
                for (double y= pr.getMinimumPoint().getY()+2; y < pr.getMaximumPoint().getY(); y++){
                    for (double z= pr.getMinimumPoint().getZ(); z < pr.getMaximumPoint().getZ(); z++){
                        Location loc = new Location(world,x,y,z);
                        loc.getBlock().setType(Material.AIR);
                    }
                }
            }
            clearRegion(pr, world.getName());
        } catch (Exception ex) {
            plugin.sendConsole("Failed to pasteSchematic() of name '"+schematic+"', "+ex);
        }
    }

    public boolean pasteSchematic(Player p, ProtectedRegion pr, String schematic) {
        return pasteSchematic(p, pr, schematic, "");
    }

    public boolean pasteSchematic(Player p, ProtectedRegion pr, String schematic, String subdir) {
        final WorldEditPlugin wep = plugin.worldEdit;
        wep.getWorldEdit().getSessionManager().
        EditSession editSession = wep.createEditSession(p);
        Vector pos = new Vector(pr.getMinimumPoint());
        try {

            session.setClipboard(getClipboard(schematic, subdir));
            session.getClipboard().paste(editSession, pos, false, false);
            clearRegion(pr, world.getName());
            return true;
        } catch (Exception ex) {
            plugin.sendConsole("Failed to pasteSchematic() of name '"+schematic+"', "+ex);
        }
        return false;
    }
    
    public static class ConsolePlayer extends BukkitCommandSender {
        final LocalWorld world;
        
        public ConsolePlayer(WorldEditPlugin plugin, CommandSender sender, World w) {
            super(plugin, sender);
            world = BukkitUtil.getLocalWorld(w);
        }
        
        @Override
        public boolean isPlayer() {
                return true;
        }
    }


    public double compareRegionToSchematic(CommandSender sender, ProtectedRegion pr, String schematic){
        CuboidClipboard c_region = getClipboard(sender, pr);
        CuboidClipboard c_schematic = getClipboard(schematic);
        Vector size = c_region.getSize();
        double same = 0;
        double total = 0;
        for (int x = 0; x < size.getX(); x++)
            for (int y = 2; y < size.getY(); y++)
                for (int z = 0; z < size.getZ(); z++){
                    Vector vec = new Vector(x,y,z);
                    if (c_schematic.getBlock(vec).getType() > 0){ //NOT AIR :)
                        if (c_region.getBlock(vec).getType() == c_schematic.getBlock(vec).getType())
                            same++;
                        total++;
                    }
                }                
        return (same/total*100D);
    }

    public double getBuildWorth(CommandSender sender, ProtectedRegion pr, String schematic){
        CuboidClipboard c_region = getClipboard(sender, pr);
        CuboidClipboard c_schematic = getClipboard(schematic);
        Vector size = c_region.getSize();
        double same = 0;
        double total = 0;
        for (int x = 0; x < size.getX(); x++)
            for (int y = 2; y < size.getY(); y++)
                for (int z = 0; z < size.getZ(); z++){
                    Vector vec = new Vector(x,y,z);
                    if (c_schematic.getBlock(vec).getType() > 0){ //NOT AIR :)
                        if (c_region.getBlock(vec).getType() == c_schematic.getBlock(vec).getType())
                            same++;
                        total++;
                    }
                }                
        return (same/total*100D);
    }   
    
    public boolean saveSchematic(Player p, ProtectedRegion region, String subdir, String schematicName){
        WorldEditPlugin wep = plugin.worldEdit;
        final LocalSession session = wep.getSession(p);
        final BukkitPlayer lPlayer = wep.wrapPlayer(p);
        EditSession editSession = session.createEditSession(lPlayer);
        try {
            Vector min = region.getMinimumPoint();
            Vector max = region.getMaximumPoint();
            CuboidClipboard clipboard = new CuboidClipboard(
                            max.subtract(min).add(new Vector(1, 1, 1)),
                            min, new Vector(0,0,0));
            clipboard.copy(editSession);
            session.setClipboard(clipboard);

            String args2[] = {"save", "mcedit", schematicName};
            CommandContext cc = new CommandContext(args2);
            save(cc, session, lPlayer, subdir);
            return true;
        } catch (Exception ex) {
            plugin.sendConsole("Failed to saveSchematic of name '" + schematicName +"', reason: " + ex);
            return false;
        }
    }

    public boolean saveSchematic(Player p, String subdir, String schematicName){
        WorldEditPlugin wep = plugin.worldEdit;
        final LocalSession session = wep.getSession(p);
        final BukkitPlayer lPlayer = wep.wrapPlayer(p);
        EditSession editSession = session.createEditSession(lPlayer);
        try {
            Region region = session.getSelection(lPlayer.getWorld());
            Vector min = region.getMinimumPoint();
            Vector max = region.getMaximumPoint();
            CuboidClipboard clipboard = new CuboidClipboard(
                            max.subtract(min).add(new Vector(1, 1, 1)),
                            min, new Vector(0,0,0));
            clipboard.copy(editSession);
            session.setClipboard(clipboard);

            String args2[] = {"save", "mcedit", schematicName};
            CommandContext cc = new CommandContext(args2);
            save(cc, session, lPlayer, subdir);
            return true;
        } catch (Exception ex) {
            plugin.sendConsole("Failed to saveSchematic of name '" + schematicName +"', reason: " + ex);
            return false;
        }
    }

    private void save(CommandContext args, LocalSession session, LocalPlayer player, String subdir) throws WorldEditException, CommandException {
        WorldEditPlugin wep = plugin.worldEdit;
        WorldEdit we = wep.getWorldEdit();
        SchematicFormat format = SchematicFormat.getFormat(args.getString(0));
        String filename = args.getString(args.argsLength() - 1);
        File dir = new File(plugin.getDataFolder() + File.separator + "schematics" + File.separator + subdir);
        File f = we.getSafeSaveFile(player, dir, filename, "schematic", "schematic");
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                player.printError(plugin.getDataFolder() + File.separator + "schematics" + File.separator + subdir);
                player.printError("The storage folder could not be created.");
                return;
            }
        }
        try {
            // Create parent directories
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new CommandException("Could not create folder for schematics!");
                }
            }
            format.save(session.getClipboard(), f);
            plugin.sendConsole(player.getName() + " saved " + f.getCanonicalPath());
            player.print(filename + " saved.");
        } catch (DataException se) {
            player.printError("Save error: " + se.getMessage());
        } catch (IOException e) {
            player.printError("Schematic could not written: " + e.getMessage());
        }
        }

        public Vector getSchematicDimensions(String schematic){
        return getSchematicDimensions(schematic, "");
    }


    public Vector getSchematicDimensions(String schematic, String subdir){
        WorldEditPlugin wep = plugin.worldEdit;
        WorldEdit we = wep.getWorldEdit();
        LocalPlayer bcs = new ConsolePlayer(wep,wep.getServerInterface(),Bukkit.getConsoleSender(), plugin.getServer().getWorld("Build"));
        File dir = new File(plugin.getDataFolder() + File.separator + "schematics"+File.separator+subdir);
        try {
        File f = we.getSafeOpenFile(bcs, dir, schematic, "schematic", "schematic");
            SchematicFormat format = SchematicFormat.getFormat(f);
            CuboidClipboard cc = format.load(f);
            return new Vector(cc.getWidth(), cc.getHeight(), cc.getLength());
        } catch (Exception e){
            plugin.sendConsole("Something went wrong getting schemtic dimensions on: " + schematic);
        }
        return null;
    }

    public Clipboard getClipboard(Player p, String schematic){
        return getClipboard(p, schematic, "");
    }

    public Clipboard getClipboard(Player p, String schematic, String subdir){
        final WorldEditPlugin wep = plugin.worldEdit;
        final WorldEdit we = wep.getWorldEdit();
        File dir = new File(plugin.getDataFolder() + File.separator + "schematics"+File.separator+subdir);
        try {
            File f = we.getSafeOpenFile(p, dir, schematic, "schematic", new String[]{"schematic"});
            FileInputStream fis = (FileInputStream)closer.register(new FileInputStream(f));
            BufferedInputStream bis = (BufferedInputStream)closer.register(new BufferedInputStream(fis));
            ClipboardReader reader = format.getReader(bis);
            WorldData worldData = player.getWorld().getWorldData();
            Clipboard clipboard = reader.read(player.getWorld().getWorldData());
        } catch (Exception e){
            printError(bcs,"Error : " + e.getMessage());
        }
        return null;
    }

    public CuboidClipboard getClipboard(CommandSender sender, ProtectedRegion pr){
        final WorldEditPlugin wep = plugin.worldEdit;
        LocalPlayer bcs = new ConsolePlayer(wep,wep.getServerInterface(), sender, world);
        final LocalSession session = wep.getWorldEdit().getSession(bcs);
        Vector min = pr.getMinimumPoint();
        Vector max = pr.getMaximumPoint();
        CuboidClipboard cc = new CuboidClipboard(max.subtract(min),min, new Vector(0,0,0));

        session.setUseInventory(false);
        session.setClipboard(cc);
        EditSession editSession = session.createEditSession(bcs);
        cc.copy(editSession);
        return cc;
    }

    private static void printError(LocalPlayer player, String msg){
            if (player != null)
                    player.printError(msg);
    }

    public static boolean contains(Location location, ProtectedRegion pr) {
    return pr != null && pr.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static boolean hasPlayer(String playerName, ProtectedRegion pr) {
            if (pr == null)
                    return true;
            DefaultDomain dd = pr.getMembers();
            if (dd.contains(playerName))
                    return true;
            dd = pr.getOwners();
            return dd.contains(playerName);
    }

}