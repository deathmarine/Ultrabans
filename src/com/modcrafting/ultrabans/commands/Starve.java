package com.modcrafting.ultrabans.commands;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;
import com.modcrafting.ultrabans.db.MySQLDatabase;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Starve implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	MySQLDatabase db;
	UltraBan plugin;
	
	public Starve(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
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
			if (Permissions.Security.permission(player, "ultraban.starve")){
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
			Player victim = plugin.getServer().getPlayer(p);
			sender.sendMessage(ChatColor.GRAY + p + " is now starving!");
			victim.sendMessage(ChatColor.GRAY + "You are now starving!");
			//player.setFoodLevel(victim.getFoodLevel());
			int st = 0;
			victim.setFoodLevel(st);
		}
		
		return true;
	}

}
