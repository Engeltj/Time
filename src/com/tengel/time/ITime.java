/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Tim
 */
public interface ITime extends Plugin {
    public Economy getEconomyManager();
}
