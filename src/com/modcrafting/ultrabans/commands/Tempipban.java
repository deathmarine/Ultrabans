package com.modcrafting.ultrabans.commands;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Tempipban implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.tempipban";
	public Tempipban(UltraBan ultraBan) {
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
		boolean anon = false;
		Player player = null;
		String admin = config.getString("defAdminName", "server");
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission) || player.isOp()) auth = true;
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		if (args.length < 3) return false;

		String p = args[0]; // Get the victim's potential name
		
		if(autoComplete)
			p = expandName(p);
		Player victim = plugin.getServer().getPlayer(p);
		
		String reason = config.getString("defReason", "not sure");
		boolean broadcast = true;
		if(args.length > 3){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = combineSplit(4, args, " ");
			}else{
				if(args[1].equalsIgnoreCase("-a")){
					anon = true;
					reason = combineSplit(4, args, " ");
				}else{
				reason = combineSplit(3, args, " ");
				}
			}
		}

		if (anon){
			admin = config.getString("defAdminName", "server");
		}

		long tempTime = parseTimeSpec(args[1],args[2]);
		if(tempTime == 0)
			return false;
		long temp = System.currentTimeMillis()/1000+tempTime; //epoch time
		//Separate for Online-Offline
		if(victim != null){
			if(plugin.bannedPlayers.contains(victim.getName().toLowerCase())){
				sender.sendMessage(ChatColor.BLUE + victim.getName() +  ChatColor.GRAY + " is already banned for " + reason);
				return true;
			}
			String offlineip = plugin.db.getAddress(p.toLowerCase());
			if(offlineip != null){
				plugin.bannedIPs.add(offlineip);
			}else{
				sender.sendMessage(ChatColor.GRAY + "IP address not found by Ultrabans for " + p);
				sender.sendMessage(ChatColor.GRAY + "Processed as a normal tempban for " + p);
				plugin.tempBans.put(victim.getName().toLowerCase(), temp);
				plugin.db.addPlayer(victim.getName(), reason, admin, temp, 0);
				log.log(Level.INFO, "[UltraBan] " + admin + " banned player " + p + ".");
				return true;
			}
			plugin.tempBans.put(victim.getName().toLowerCase(), temp);
			plugin.db.addPlayer(victim.getName(), reason, admin, temp, 1);
			log.log(Level.INFO, "[UltraBan] " + admin + " tempipbanned player " + victim.getName() + ".");
			String tempbanMsgVictim = config.getString("messages.tempipbanMsgVictim", "You have been temp. banned by %admin%. Reason: %reason%!");
			tempbanMsgVictim = tempbanMsgVictim.replaceAll("%admin%", admin);
			tempbanMsgVictim = tempbanMsgVictim.replaceAll("%reason%", reason);
			victim.kickPlayer(formatMessage(tempbanMsgVictim));
			if(broadcast){
				String tempbanMsgBroadcast = config.getString("messages.tempipbanMsgBroadcast", "%victim% was temp. banned by %admin%. Reason: %reason%!");
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%admin%", admin);
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%reason%", reason);
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%victim%", victim.getName());
				plugin.getServer().broadcastMessage(formatMessage(tempbanMsgBroadcast));
			}else{
				String tempbanMsgBroadcast = config.getString("messages.tempipbanMsgBroadcast", "%victim% was temp. banned by %admin%. Reason: %reason%!");
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%admin%", admin);
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%reason%", reason);
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%victim%", victim.getName());
				sender.sendMessage(formatMessage(":S:" + tempbanMsgBroadcast));
			}
		}else{
			if(plugin.bannedPlayers.contains(p.toLowerCase())){
				sender.sendMessage(ChatColor.BLUE + p +  ChatColor.GRAY + " is already banned for " + reason);
				return true;
			}
			plugin.tempBans.put(p.toLowerCase(), temp);
			plugin.db.addPlayer(p, reason, admin, temp, 1);
			log.log(Level.INFO, "[UltraBan] " + admin + " tempbanned player " + p + ".");
			if(broadcast){
				String tempbanMsgBroadcast = config.getString("messages.tempbanMsgBroadcast", "%victim% was temp. banned by %admin%. Reason: %reason%!");
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%admin%", admin);
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%reason%", reason);
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%victim%", p);
				plugin.getServer().broadcastMessage(formatMessage(tempbanMsgBroadcast));
			}else{
				String tempbanMsgBroadcast = config.getString("messages.tempbanMsgBroadcast", "%victim% was temp. banned by %admin%. Reason: %reason%!");
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%admin%", admin);
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%reason%", reason);
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%victim%", p);
				sender.sendMessage(formatMessage(":S:" + tempbanMsgBroadcast));
			}
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
	public static long parseTimeSpec(String time, String unit) {
		long sec;
		try {
			sec = Integer.parseInt(time)*60;
		} catch (NumberFormatException ex) {
			return 0;
		}
		if (unit.startsWith("hour"))
			sec *= 60;
		else if (unit.startsWith("day"))
			sec *= (60*24);
		else if (unit.startsWith("week"))
			sec *= (7*60*24);
		else if (unit.startsWith("month"))
			sec *= (30*60*24);
		else if (unit.startsWith("min"))
			sec *= 1;
		else if (unit.startsWith("sec"))
			sec /= 60;
		return sec;
	}
	public String formatMessage(String str){
		String funnyChar = new Character((char) 167).toString();
		str = str.replaceAll("&", funnyChar);
		return str;
	}
}
