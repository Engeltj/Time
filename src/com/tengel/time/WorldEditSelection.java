/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.RegionSelector;
import static java.lang.Math.abs;
import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author Tim
 */
public class WorldEditSelection implements Selection {
    private Vector nativeMinPoint,nativeMaxPoint;
    private Location start,end;
    
    public WorldEditSelection(Location start, Location end){
        nativeMinPoint = new Vector(start.getX(),start.getY(),start.getZ());
        nativeMaxPoint = new Vector(end.getX(),end.getY(),end.getBlockZ());
        this.start = start;
        this.end = end;
    }
    
    public Location getMinimumPoint() {
        return start;
    }

    public Vector getNativeMinimumPoint() {
        return nativeMinPoint;
    }

    public Location getMaximumPoint() {
        return end;
    }

    public Vector getNativeMaximumPoint() {
        return nativeMaxPoint;
    }

    public RegionSelector getRegionSelector() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public World getWorld() {
        return start.getWorld();
    }

    public int getArea() {
        int x = abs(start.getBlockX() - end.getBlockX())+1;
        int y = abs(start.getBlockY() - end.getBlockY())+1;
        int z = abs(start.getBlockZ() - end.getBlockZ())+1;
        return x*y*z;
    }

    public int getWidth() {
        return abs(start.getBlockX() - end.getBlockX())+1;
    }

    public int getHeight() {
        return abs(start.getBlockY() - end.getBlockY())+1;
    }

    public int getLength() {
        return abs(start.getBlockZ() - end.getBlockZ())+1;
    }

    public boolean contains(Location lctn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
