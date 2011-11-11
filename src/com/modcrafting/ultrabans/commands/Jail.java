package com.modcrafting.ultrabans.commands;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;
import com.modcrafting.ultrabans.db.MySQLDatabase;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Jail implements CommandExecutor{

	public static final Logger log = Logger.getLogger("Minecraft");
	
	MySQLDatabase db;
	UltraBan plugin;
		
	public Jail(UltraBan ultraBan) {
			this.plugin = ultraBan;
		}
	public Location locJail;
	public boolean autoComplete;
	public String expandName(String p) {
			int m = 0;
			String Result = "";
			for (int n = 0; n < plugin.getServer().getOnlinePlayers().length; n++) {
				String str = plugin.getServer().getOnlinePlayers()[n].getName();
				if (str.matches("(?i).*" + p + ".*")) {
					m++;
					Result = str;
					if(m==2) {
						return null;
					}
				}
				if (str.equalsIgnoreCase(p))
					return str;
			}
			if (m == 1)
				return Result;
			if (m > 1) {
				return null;
			}
			if (m < 1) {
				return p;
			}
			return p;
		}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
			boolean auth = false;
			Player player = null;
			if (sender instanceof Player){
				player = (Player)sender;
				if (Permissions.Security.permission(player, "ultraban.jail")){
					auth = true;
				}else{
				 if (player.isOp()) auth = true; //defaulting to Op if no permissions or node
				}
			}else{
				auth = true; //if sender is not a player - Console
			}
			if(auth){
				if (args.length < 1) return false;
				String p = args[0];
				if(autoComplete)
					p = expandName(p);
					if(args[0] == "set"){
						Location setlp = player.getLocation();
						setJail(setlp);
						return true;
					}
				Player victim = plugin.getServer().getPlayer(p);
				sender.sendMessage(ChatColor.GRAY + p + " is now in Jail!");
				victim.sendMessage(ChatColor.GRAY + "You've been Arrested.");
				Location tlp = getJail();
				victim.teleport(tlp);
			}
			
			return true;
		}
	public void setJail(Location setlp){
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
        config.set("jail.x", (int) setlp.getX());
        config.set("jail.y", (int) setlp.getY());
        config.set("jail.z", (int) setlp.getZ());
        config.set("jail.world", setlp.getWorld().getName());
        plugin.saveConfig();
	}

	public Location getJail(){
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
        config.get("jail.x", (int) locJail.getX());
        config.get("jail.y", (int) locJail.getY());
        config.get("jail.z", (int) locJail.getZ());
        config.get("jail.world", locJail.getWorld().getName());
		return locJail;
	}
}