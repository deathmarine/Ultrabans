package com.modcrafting.ultrabans.commands;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Unban implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	public Unban(UltraBan ultraBan) {
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
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean auth = false;
		Player player = null;
		String admin = "server";
		
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.unban")){
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
		if (args.length < 1)return false;
		String p = args[0];
		
		if(plugin.db.permaBan(p.toLowerCase())){
			sender.sendMessage(ChatColor.BLUE + p +  ChatColor.GRAY + " is PermaBanned.");
			log.log(Level.INFO, "[UltraBan] " + p + " is PermaBanned.");
			return true;
		}
			
		if(plugin.bannedPlayers.remove(p.toLowerCase())){
			plugin.db.removeFromBanlist(p);
			plugin.db.addPlayer(p, "Unbanned", admin, 0, 5);
			Bukkit.getOfflinePlayer(p).setBanned(false);
			if(plugin.tempBans.containsKey(p.toLowerCase()))
				plugin.tempBans.remove(p.toLowerCase());
			String ip = plugin.db.getAddress(p);
			if(plugin.bannedIPs.contains(ip)){
				plugin.bannedIPs.remove(ip);
				Bukkit.unbanIP(ip);
				System.out.println("Also removed the IP ban!");
			}
			log.log(Level.INFO, "[UltraBan] " + admin + " unbanned player " + p + ".");
			String unbanMsgBroadcast = config.getString("messages.unbanMsgBroadcast", "%victim% was unbanned by %admin%!");
			unbanMsgBroadcast = unbanMsgBroadcast.replaceAll("%admin%", admin);
			unbanMsgBroadcast = unbanMsgBroadcast.replaceAll("%victim%", p);
			sender.sendMessage(formatMessage(unbanMsgBroadcast));
			return true;
		}else{
			String unbanMsgFailed = config.getString("messages.unbanMsgFailed", "%victim% is already unbanned!");
			unbanMsgFailed = unbanMsgFailed.replaceAll("%admin%", admin);
			unbanMsgFailed = unbanMsgFailed.replaceAll("%victim%", p);
			sender.sendMessage(formatMessage(unbanMsgFailed));
			return true;
		}
	}
	public String formatMessage(String str){
		String funnyChar = new Character((char) 167).toString();
		str = str.replaceAll("&", funnyChar);
		return str;
	}
}
