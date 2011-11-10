package com.modcrafting.ultrabans.commands;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Kick implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	
	public Kick(UltraBan ultraBan) {
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
		String admin = "server";
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.kick")){
				auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no permissions or node
			}
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		// Has enough arguments?
		if (args.length < 1) return false;

		String p = args[0].toLowerCase();
		if(autoComplete)
			p = expandName(p);
		// Reason stuff
		String reason = "not sure";
		boolean broadcast = true;
		if(args.length > 1){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = combineSplit(2, args, " ");
			}else
				reason = combineSplit(1, args, " ");
		}

		if(p.equals("*")){
			if (sender instanceof Player)
				if (!Permissions.Security.permission(player, "ultraban.kick.all")) return true;
			log.log(Level.INFO, "[UltraBan] " + admin + " kicked Everyone Reason: " + reason);
			for (Player pl : plugin.getServer().getOnlinePlayers()) {
				pl.kickPlayer(admin + " has kicked Everyone for: " + reason);
				return true;
			}
		}
		if(plugin.autoComplete)
			p = expandName(p);
		Player victim = plugin.getServer().getPlayer(p);
		if(victim == null){
			sender.sendMessage(ChatColor.GRAY + "Player must be online!");
			return true;
		}
		plugin.db.addPlayer(p, reason, admin, 0, 3);
		log.log(Level.INFO, "[UltraBan] " + admin + " kicked player " + p + ". Reason: " + reason);
		victim.kickPlayer(admin + " has kicked you for: " + reason);
	
		if(broadcast){
			plugin.getServer().broadcastMessage(ChatColor.BLUE + p + ChatColor.GRAY + " was kicked by " + 
					ChatColor.DARK_GRAY + admin + ChatColor.GRAY + ": " + reason);
		}
		return true;
	}
	public String combineSplit(int startIndex, String[] string, String seperator) {
		StringBuilder builder = new StringBuilder();

		for (int i = startIndex; i < string.length; i++) {
			builder.append(string[i]);
			builder.append(seperator);
		}

		builder.deleteCharAt(builder.length() - seperator.length()); // remove
		return builder.toString();
	}
	
}
