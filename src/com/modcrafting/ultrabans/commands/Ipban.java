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
import com.nijikokun.bukkit.Permissions.Permissions;

public class Ipban implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	
	public Ipban(UltraBan ultraBan) {
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
		String reason = "not sure";
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.ipban")){
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

		String p = args[0];
		if(validIP(p)){
			plugin.bannedIPs.add(p);
			String pname = plugin.db.getName(p);
			if (pname != null){
				plugin.db.addPlayer(pname, reason, admin, 0, 1);
			}else{
				plugin.db.setAddress("failedname", p);
				plugin.db.addPlayer("failedname", reason, admin, 0, 1);
			}
			plugin.db.addPlayer(pname, reason, admin, 0, 1);
			Bukkit.banIP(p);
			String banMsgBroadcast = config.getString("messages.banMsgBroadcast", "%victim% was banned by %admin%. Reason: %reason%");
			banMsgBroadcast = banMsgBroadcast.replaceAll("%admin%", admin);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%reason%", reason);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%victim%", p);
			plugin.getServer().broadcastMessage(formatMessage(banMsgBroadcast));
			return true;
		}
		
		
		if(autoComplete)
			p = expandName(p);
		Player victim = plugin.getServer().getPlayer(p); 
		if(victim == null){
			victim = Bukkit.getOfflinePlayer(p).getPlayer();
		}
		boolean broadcast = true;
		if(args.length > 1){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = combineSplit(2, args, " ");
			}else
				reason = combineSplit(1, args, " ");
		}

		if(plugin.bannedPlayers.contains(p.toLowerCase())){
			sender.sendMessage(ChatColor.BLUE + p +  ChatColor.GRAY + " is already banned for " + reason);
			return true;
		}
		String victimip = plugin.db.getAddress(p);
		plugin.bannedPlayers.add(p.toLowerCase());
		Bukkit.getOfflinePlayer(p).setBanned(true);
		if(victimip != null){
		plugin.bannedIPs.add(victimip);
		Bukkit.banIP(victimip);
		}else{
			sender.sendMessage(ChatColor.GRAY + "IP address not found by Ultrabans for " + p);
			sender.sendMessage(ChatColor.GRAY + "Processed as a normal ban for " + p);
			plugin.db.addPlayer(p, reason, admin, 0, 0);
			log.log(Level.INFO, "[UltraBan] " + admin + " banned player " + p + ".");
			return true;
		}
		plugin.db.addPlayer(p, reason, admin, 0, 1);
		log.log(Level.INFO, "[UltraBan] " + admin + " banned player " + p + ".");
		String banMsgVictim = config.getString("messages.banMsgVictim", "You have been banned by %admin%. Reason: %reason%");
		banMsgVictim = banMsgVictim.replaceAll("%admin%", admin);
		banMsgVictim = banMsgVictim.replaceAll("%reason%", reason);
		victim.kickPlayer(formatMessage(banMsgVictim));
		if(broadcast){
			String banMsgBroadcast = config.getString("messages.banMsgBroadcast", "%victim% was banned by %admin%. Reason: %reason%");
			banMsgBroadcast = banMsgBroadcast.replaceAll("%admin%", admin);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%reason%", reason);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%victim%", p);
			plugin.getServer().broadcastMessage(formatMessage(banMsgBroadcast));
		}else{
			String banMsgBroadcast = config.getString("messages.banMsgBroadcast", "%victim% was banned by %admin%. Reason: %reason%");
			banMsgBroadcast = banMsgBroadcast.replaceAll("%admin%", admin);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%reason%", reason);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%victim%", p);
			sender.sendMessage(formatMessage(":S:" + banMsgBroadcast));
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
