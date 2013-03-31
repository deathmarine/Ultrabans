/* COPYRIGHT (c) 2013 Deathmarine (Joshua McCurry)
 * This file is part of Ultrabans.
 * Ultrabans is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Ultrabans is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Ultrabans.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.modcrafting.ultrabans.util;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import com.modcrafting.ultrabans.Ultrabans;

public class Jailtools {
	Ultrabans plugin;
    public Jailtools(Ultrabans ultraBan) {
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
