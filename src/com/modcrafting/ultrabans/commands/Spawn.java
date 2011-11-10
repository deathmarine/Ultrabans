package com.modcrafting.ultrabans.commands;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.modcrafting.ultrabans.UltraBan;
import com.modcrafting.ultrabans.db.MySQLDatabase;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Spawn implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	MySQLDatabase db;
	UltraBan plugin;
	public boolean autoComplete;
	public Spawn(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
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
		String admin = "server";
		autoComplete = plugin.properties.getBoolean("auto-complete", true);
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.spawn")){
				auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no permissions or node
			}
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if(auth){
			if (args.length < 1) return false;
			String p = args[0]; //type name correct or 
			if(autoComplete) p = expandName(p); 
			Player victim = plugin.getServer().getPlayer(p);
				sender.sendMessage(ChatColor.GRAY + admin + " has sent " + p + " to spawn!");
				victim.sendMessage(ChatColor.GRAY + admin + " has sent you to spawn!");
				//Further Research	
				World wtlp = victim.getWorld();
				Location tlp = wtlp.getSpawnLocation();
				victim.teleport(tlp);
		}
		
		return true;
	}

}
