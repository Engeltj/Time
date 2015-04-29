/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.bukkit.BukkitCommandSender;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Tim
 */

public class WorldEditUtil {
	public static WorldEditPlugin wep;

	public static boolean hasWorldEdit() {
		return wep != null;
	}

	public static void setWorldEdit(Plugin plugin) {
		wep = (WorldEditPlugin) plugin;
	}

	public static Selection getSelection(Player player) {
		return wep.getSelection(player);
	}

	public static WorldEditPlugin getWorldEditPlugin() {
		return wep;
	}

//	public class ConsolePlayer extends BukkitCommandSender {
//		final LocalWorld world;
//		public ConsolePlayer(WorldEditPlugin plugin, ServerInterface server,CommandSender sender, World w) {
//			super(plugin, server, sender);
//			world = BukkitUtil.getLocalWorld(w);
//		}
//
//		@Override
//		public boolean isPlayer() {
//			return true;
//		}
//		@Override
//		public LocalWorld getWorld() {
//			return world;
//		}
//	}
}

