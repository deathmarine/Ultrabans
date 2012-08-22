/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.util;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import com.modcrafting.ultrabans.UltraBan;

public class Jailtools {
	UltraBan plugin;
    public Jailtools(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}

	public void setJail(Location location, String label) {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
        config.set(label+".x", (int) location.getX());
        config.set(label+".y", (int) location.getY());
        config.set(label+".z", (int) location.getZ());
        config.set(label+".world", location.getWorld().getName());
        plugin.saveConfig();

    }
    public Location getJail(String label){
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
        Location setlp = new Location(
                plugin.getServer().getWorld(
                	config.getString(label+".world", plugin.getServer().getWorlds().get(0).getName())),
                config.getInt(label+".x", 0),
                config.getInt(label+".y", 0),
                config.getInt(label+".z", 0));
        	return setlp;
    }
}
