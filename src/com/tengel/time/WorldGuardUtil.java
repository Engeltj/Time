/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;


import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Countable;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitCommandSender;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.commands.SchematicCommands;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Tim
 */

public class WorldGuardUtil {
	private static WorldGuardPlugin wgp;
        private Time plugin;
        private World world;
	public static boolean hasWorldGuard = false;
	static Map<String,Set<String>> trackedRegions = new ConcurrentHashMap<String,Set<String>>();

        public WorldGuardUtil(Time plugin, World world){
            this.wgp = plugin.worldGuard;
            this.world = world;
            this.plugin = plugin;
        }
        
        public ProtectedRegion updateProtectedRegion(String playerName, Location start, Location end) throws Exception {
		return createRegion(playerName, start, end);
	}

	private ProtectedRegion createRegion(String playerName, Location start, Location end)
			throws ProtectionDatabaseException {
            String id = "buildplot_"+playerName;
            RegionManager mgr = wgp.getGlobalRegionManager().get(world);
            mgr.removeRegion(id);
            ProtectedRegion region;
            
            BlockVector bv_start = new BlockVector(start.getX(), start.getY(), start.getZ());
            BlockVector bv_end = new BlockVector(end.getX(), end.getY(), end.getZ());
                  // Detect the type of region from WorldEdit
            //if (sel instanceof Polygonal2DSelection) {
                //Polygonal2DSelection polySel = (Polygonal2DSelection) sel;
                //int minY = polySel.getNativeMinimumPoint().getBlockY();
                //int maxY = polySel.getNativeMaximumPoint().getBlockY();
                //region = new ProtectedPolygonalRegion(id, polySel.getNativePoints(), minY, maxY);
           // } else { /// default everything to cuboid
            region = new ProtectedCuboidRegion(id, bv_start, bv_end);
            //}
            region.setPriority(11); /// some relatively high priority
            //region.setFlag(DefaultFlag.PVP,State.ALLOW);
            wgp.getRegionManager(world).addRegion(region);
            DefaultDomain dd = region.getOwners();
            dd.addPlayer(playerName);
            region.setOwners(dd);
            mgr.save();
            //addMember(region, playerName, w.getName());
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
        
	public static boolean isLeavingArea(ProtectedRegion pr, final Location from, final Location to, final World w) {
		if (pr == null)
			return false;
		return (!pr.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ()) &&
				pr.contains(from.getBlockX(), from.getBlockY(), from.getBlockZ()));
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

	public static void deleteRegion(String worldName, String id) {
		World w = Bukkit.getWorld(worldName);
		if (w == null)
			return;
		RegionManager mgr = wgp.getRegionManager(w);
		if (mgr == null)
			return;
		mgr.removeRegion(id);
	}
        
        public void pasteFirstLayer(CommandSender sender, ProtectedRegion pr, String schematic){
            String args[] = {"load", schematic};
            final WorldEditPlugin wep = plugin.worldEdit;
            //final WorldEdit we = wep.getWorldEdit();
            LocalPlayer bcs = new ConsolePlayer(wep,wep.getServerInterface(), sender, world);
            final LocalSession session = wep.getWorldEdit().getSession(bcs);
            session.setUseInventory(false);
            EditSession editSession = session.createEditSession(bcs);
            Vector pos = new Vector(pr.getMinimumPoint());
            try {
                    //CommandContext cc = new CommandContext(args);
                    File f = new File(plugin.getDataFolder() + "/schematics/" + schematic);
                    SchematicFormat format = SchematicFormat.getFormat(f);
                    session.setClipboard(format.load(f));
                    session.getClipboard().paste(editSession, pos, false, false);  
                    for (double x= pr.getMinimumPoint().getX(); x < pr.getMaximumPoint().getX(); x++){
                        for (double y= pr.getMinimumPoint().getY()+2; y < pr.getMaximumPoint().getY(); y++){
                            for (double z= pr.getMinimumPoint().getZ(); z < pr.getMaximumPoint().getZ(); z++){
                                Location loc = new Location(world,x,y,z);
                                loc.getBlock().setType(Material.AIR);
                                clearRegion(pr, world.getName());
                            }
                        }
                    }
                    
                    //loadAndPaste(cc, we, session, bcs, editSession, pos);
            } catch (Exception e) {
            }
        }

	public boolean pasteSchematic(CommandSender sender, ProtectedRegion pr, String schematic, World world) {
		CommandContext cc = null;
		String args[] = {"load", schematic};
		final WorldEditPlugin wep = plugin.worldEdit;
		final WorldEdit we = wep.getWorldEdit();
		LocalPlayer bcs = new ConsolePlayer(wep,wep.getServerInterface(),sender, world);

		final LocalSession session = wep.getWorldEdit().getSession(bcs);
		session.setUseInventory(false);
		EditSession editSession = session.createEditSession(bcs);
		Vector pos = new Vector(pr.getMinimumPoint());
		try {
			cc = new CommandContext(args);
			return loadAndPaste(cc, we, session, bcs, editSession, pos);
		} catch (Exception e) {
                        
			//Log.printStackTrace(e);
			return false;
		}
	}

	public static class ConsolePlayer extends BukkitCommandSender {
		LocalWorld world;
		public ConsolePlayer(WorldEditPlugin plugin, ServerInterface server, CommandSender sender, World w) {
			super(plugin, server, sender);
			world = BukkitUtil.getLocalWorld(w);
		}

		@Override
		public boolean isPlayer() {
			return true;
		}
		@Override
		public LocalWorld getWorld() {
			return world;
		}
	}
        
        
        public double compareRegionToSchematic(CommandSender sender, ProtectedRegion r, String schematic){
                Vector min = r.getMinimumPoint();
                Vector max = r.getMaximumPoint();
		final WorldEditPlugin wep = plugin.worldEdit;
		LocalPlayer bcs = new ConsolePlayer(wep,wep.getServerInterface(), sender, world);
                CuboidClipboard c_region = new CuboidClipboard(max.subtract(min),min, new Vector(0,0,0));
		final LocalSession session = wep.getWorldEdit().getSession(bcs);
		session.setUseInventory(false);
                session.setClipboard(c_region);
		EditSession editSession = session.createEditSession(bcs);
                c_region.copy(editSession);
                CuboidClipboard c_schematic = load(bcs, schematic);
                
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
        
	public static boolean saveSchematic(Player p, String schematicName){
            CommandContext cc = null;
            WorldEditPlugin wep = WorldEditUtil.getWorldEditPlugin();
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

                    SchematicCommands sc = new SchematicCommands(wep.getWorldEdit());
                    String args2[] = {"save", "mcedit", schematicName};
                    cc = new CommandContext(args2);
                    sc.save(cc, session, lPlayer, editSession);
                    return true;
            } catch (Exception e) {
                    //Log.printStackTrace(e);
                    return false;
            }
	}
        
        public Vector getSchematicDimensions(CommandSender sender, String schematic){
            WorldEditPlugin wep = plugin.worldEdit;
            WorldEdit we = wep.getWorldEdit();
            LocalPlayer bcs = new ConsolePlayer(wep,wep.getServerInterface(),sender, plugin.getServer().getWorld("Build"));
            File dir = new File(plugin.getDataFolder() + "/schematics/");
            try {
		File f = we.getSafeOpenFile(bcs, dir, schematic, "schematic", new String[] {"schematic"});
                SchematicFormat format = SchematicFormat.getFormat(f);
                CuboidClipboard cc = format.load(f);
                Vector vec = new Vector(cc.getWidth(), cc.getHeight(), cc.getLength());
                return vec;
            } catch (Exception e){
                plugin.sendConsole("Something went wrong getting schemtic dimensions on: " + schematic);
            }
            return null;
        }

        
        public CuboidClipboard load(LocalPlayer bcs, String schematic){
            WorldEditPlugin wep = plugin.worldEdit;
            final WorldEdit we = wep.getWorldEdit();
            File dir = new File(plugin.getDataFolder() + "/schematics/");
            try {
		File f = we.getSafeOpenFile(bcs, dir, schematic, "schematic", new String[] {"schematic"});
                SchematicFormat format = SchematicFormat.getFormat(f);
                return format.load(f);
            } catch (Exception e){
                printError(bcs,"Error : " + e.getMessage());
            }
            return null;
        }
        
	/**
	 * This is just copied and pasted from world edit source, with small changes to also paste
	 * @param args
	 * @param we
	 * @param session
	 * @param player
	 * @param editSession
	 */
	public static boolean loadAndPaste(CommandContext args, WorldEdit we,
		LocalSession session, com.sk89q.worldedit.LocalPlayer player, EditSession editSession, Vector pos) {

		LocalConfiguration config = we.getConfiguration();

		String filename = args.getString(0);
		File dir = we.getWorkingDirectoryFile(config.saveDir);
		File f = null;
		try {
                    f = we.getSafeOpenFile(player, dir, filename, "schematic",new String[] {"schematic"});
                    String filePath = f.getCanonicalPath();
                    String dirPath = dir.getCanonicalPath();

                    if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                            printError(player,"Schematic could not read or it does not exist.");
                            return false;
                    }
                    SchematicFormat format = SchematicFormat.getFormat(f);
                    if (format == null) {
                            printError(player,"Unknown schematic format for file" + f);
                            return false;
                    }

                    if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                            printError(player,"Schematic could not read or it does not exist.");
                    } else {
                            session.setClipboard(format.load(f));
                            //				WorldEdit.logger.info(player.getName() + " loaded " + filePath);
                            //				print(player,filePath + " loaded");
                    }
                    session.getClipboard().paste(editSession, pos, false, true);
                    //session.getClipboard().
                    //			WorldEdit.logger.info(player.getName() + " pasted schematic" + filePath +"  at " + pos);
		} catch (DataException e) {
                    printError(player,"Load error: " + e.getMessage());
		} catch (IOException e) {
                    printError(player,"Schematic could not read or it does not exist: " + e.getMessage());
		} catch (Exception e){
                    //Log.printStackTrace(e);
                    printError(player,"Error : " + e.getMessage());
		}
		return true;
	}

	private static void printError(LocalPlayer player, String msg){
		if (player == null){
			//Log.err(msg);
		} else {
			player.printError(msg);
		}
	}

	public static boolean contains(Location location, ProtectedRegion pr) {
		if (pr == null)
			return false;
		return pr.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
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