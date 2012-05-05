package com.modcrafting.ultrabans.commands;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

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
		String admin = config.getString("defAdminName", "server");
		
		if (sender instanceof Player){
			player = (Player)sender;
			if (plugin.setupPermissions()){
				if (plugin.permission.has(player, "ultraban.unban")) auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no vault doesn't take or node
			}
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		if (args.length < 1)return false;
		String p = args[0];
		
		//unban IPv4
		if(validIP(p)){
			plugin.bannedIPs.remove(p);
			String pname = plugin.db.getName(p);
			Bukkit.unbanIP(p);
			if (pname != null){
				String reason = plugin.db.getBanReason(plugin.getServer().getOfflinePlayer(pname).getName());
				plugin.db.removeFromBanlist(plugin.getServer().getOfflinePlayer(pname).getName());
				plugin.db.addPlayer(plugin.getServer().getOfflinePlayer(p).getName(), "Unbanned: " + reason, admin, 0, 5);
				log.log(Level.INFO, "[UltraBan] " + admin + " unbanned player " + plugin.getServer().getOfflinePlayer(p).getName() + ".");				
			}else{
				plugin.db.removeFromBanlist(pname);			
			}
			String unbanMsgBroadcast = config.getString("messages.unbanMsgBroadcast", "%victim% was unbanned by %admin%!");
			unbanMsgBroadcast = unbanMsgBroadcast.replaceAll("%admin%", admin);
			unbanMsgBroadcast = unbanMsgBroadcast.replaceAll("%victim%", p);
			sender.sendMessage(formatMessage(unbanMsgBroadcast));
			return true;
		}
		
		if(plugin.db.permaBan(p.toLowerCase())){
			sender.sendMessage(ChatColor.BLUE + p +  ChatColor.GRAY + " is PermaBanned.");
			log.log(Level.INFO, "[UltraBan] " + p + " is PermaBanned.");
			return true;
		}

		if(plugin.bannedPlayers.contains(p.toLowerCase())){
			plugin.bannedPlayers.remove(p.toLowerCase());
			String reason = plugin.db.getBanReason(plugin.getServer().getOfflinePlayer(p).getName());
			plugin.db.removeFromBanlist(plugin.getServer().getOfflinePlayer(p).getName());
			Bukkit.getOfflinePlayer(p).setBanned(false);
			String ip = plugin.db.getAddress(plugin.getServer().getOfflinePlayer(p).getName());
			if(plugin.bannedIPs.contains(ip)){
				plugin.bannedIPs.remove(ip);
				Bukkit.unbanIP(ip);
				System.out.println("Also removed the IP ban!");
			}
			plugin.db.addPlayer(plugin.getServer().getOfflinePlayer(p).getName(), "Unbanned: " + reason, admin, 0, 5);
			log.log(Level.INFO, "[UltraBan] " + admin + " unbanned player " + plugin.getServer().getOfflinePlayer(p).getName() + ".");
			String unbanMsgBroadcast = config.getString("messages.unbanMsgBroadcast", "%victim% was unbanned by %admin%!");
			unbanMsgBroadcast = unbanMsgBroadcast.replaceAll("%admin%", admin);
			unbanMsgBroadcast = unbanMsgBroadcast.replaceAll("%victim%", plugin.getServer().getOfflinePlayer(p).getName());
			sender.sendMessage(formatMessage(unbanMsgBroadcast));
			return true;
		}else{
			if(plugin.tempBans.containsKey(p.toLowerCase())){
			plugin.tempBans.remove(p.toLowerCase());
			plugin.db.removeFromBanlist(plugin.getServer().getOfflinePlayer(p).getName());
			Bukkit.getOfflinePlayer(p).setBanned(false);
			String ip = plugin.db.getAddress(plugin.getServer().getOfflinePlayer(p).getName());
			if(plugin.bannedIPs.contains(ip)){
				plugin.bannedIPs.remove(ip);
				Bukkit.unbanIP(ip);
				System.out.println("Also removed the IP ban!");
			}
			plugin.db.addPlayer(p, "Unbanned", admin, 0, 5);
			log.log(Level.INFO, "[UltraBan] " + admin + " unbanned player " + plugin.getServer().getOfflinePlayer(p).getName() + ".");
			String unbanMsgBroadcast = config.getString("messages.unbanMsgBroadcast", "%victim% was unbanned by %admin%!");
			unbanMsgBroadcast = unbanMsgBroadcast.replaceAll("%admin%", admin);
			unbanMsgBroadcast = unbanMsgBroadcast.replaceAll("%victim%", plugin.getServer().getOfflinePlayer(p).getName());
			sender.sendMessage(formatMessage(unbanMsgBroadcast));
			return true;
			}else{
			String unbanMsgFailed = config.getString("messages.unbanMsgFailed", "%victim% is already unbanned!");
			unbanMsgFailed = unbanMsgFailed.replaceAll("%admin%", admin);
			unbanMsgFailed = unbanMsgFailed.replaceAll("%victim%", plugin.getServer().getOfflinePlayer(p).getName());
			sender.sendMessage(formatMessage(unbanMsgFailed));
			return true;
			}
		}
	}

	public static boolean validIP(String ip) {
	    if (ip == null || ip.isEmpty()) return false;
	    ip = ip.trim();
	    if ((ip.length() < 6) & (ip.length() > 15)) return false;

	    try {
	        Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
	        Matcher matcher = pattern.matcher(ip);
	        return matcher.matches();
	    } catch (PatternSyntaxException ex) {
	        return false;
	    }
	}
	public String formatMessage(String str){
		String funnyChar = new Character((char) 167).toString();
		str = str.replaceAll("&", funnyChar);
		return str;
	}
}
