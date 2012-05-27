package com.modcrafting.ultrabans.util;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import com.modcrafting.ultrabans.UltraBan;

public class Jailtools {
	UltraBan plugin;
    public Jailtools(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
    public Location setlp;

	public void setJail(Location location, String label) {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
        config.set(label+".x", (int) setlp.getX());
        config.set(label+".y", (int) setlp.getY());
        config.set(label+".z", (int) setlp.getZ());
        config.set(label+".world", setlp.getWorld().getName());
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
