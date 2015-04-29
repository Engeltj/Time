/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tengel.time;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.session.ClipboardHolder;
/**
 *
 * @author Tim
 */
public class WorldGuardUtil2 {
	public void paste(Player player, LocalSession session, EditSession editSession, boolean ignoreAirBlocks, boolean atOrigin,
                   boolean selectPasted) throws WorldEditException {

            ClipboardHolder holder = session.getClipboard();
            Clipboard clipboard = holder.getClipboard();
            Region region = clipboard.getRegion();
            

            Vector to = atOrigin ? clipboard.getOrigin() : session.getPlacementPosition(player);
            Operation operation = holder
                    .createPaste(editSession, editSession.getWorld().getWorldData())
                    .to(to)
                    .ignoreAirBlocks(ignoreAirBlocks)
                    .build();
            Operations.completeLegacy(operation);

            if (selectPasted) {
                Vector max = to.add(region.getMaximumPoint().subtract(region.getMinimumPoint()));
                RegionSelector selector = new CuboidRegionSelector(player.getWorld(), to, max);
                session.setRegionSelector(player.getWorld(), selector);
                selector.learnChanges();
                selector.explainRegionAdjust(player, session);
            }

            player.print("The clipboard has been pasted at " + to);
        }

}
