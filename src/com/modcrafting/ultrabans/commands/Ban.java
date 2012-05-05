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

public class Ban implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.ban";
	public Ban(UltraBan ultraBan) {
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
		// Has enough arguments?
		if (args.length < 1) return false;
		boolean autoComplete = config.getBoolean("auto-complete", true);
		String p = args[0]; // Get the victim's name
		if(autoComplete) p = expandName(p); //If the admin has chosen to do so, autocomplete the name!
		Player victim = plugin.getServer().getPlayer(p); // What player is really the victim?
		// Reason stuff
		String reason = config.getString("defReason", "not sure");
		boolean broadcast = true;
		if(args.length > 1){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = combineSplit(2, args, " ");
			}else{
				if(args[1].equalsIgnoreCase("-a")){
					anon = true;
					reason = combineSplit(2, args, " ");
				}else{
				reason = combineSplit(1, args, " ");
				}
			}
		}
		if (anon){
			admin = config.getString("defAdminName", "server");
		}
		if(plugin.bannedPlayers.contains(p.toLowerCase())){
			String banMsgVictim = config.getString("messages.banMsgFailed", 
			"Player %victim% is already banned!");
			if(victim == null){
			banMsgVictim = banMsgVictim.replaceAll("%victim%", p);
			}else{
			banMsgVictim = banMsgVictim.replaceAll("%victim%", victim.getName());
			}
			sender.sendMessage(formatMessage(banMsgVictim));
			return true;
		}
		if(victim == null){
			String banMsgVictim = config.getString("messages.banMsgVictim", "You have been banned by %admin%. Reason: %reason%");
			banMsgVictim = banMsgVictim.replaceAll("%admin%", admin);
			banMsgVictim = banMsgVictim.replaceAll("%reason%", reason);
			plugin.bannedPlayers.add(p.toLowerCase());
			plugin.db.addPlayer(p, reason, admin, 0, 0);
			log.log(Level.INFO, "[UltraBan] " + admin + " banned player " + p + ".");
		}
		if(victim != null){
			String banMsgVictim = config.getString("messages.banMsgVictim", "You have been banned by %admin%. Reason: %reason%");
			banMsgVictim = banMsgVictim.replaceAll("%admin%", admin);
			banMsgVictim = banMsgVictim.replaceAll("%reason%", reason);
			victim.kickPlayer(formatMessage(banMsgVictim));
			plugin.bannedPlayers.add(victim.getName().toLowerCase());
			plugin.db.addPlayer(victim.getName(), reason, admin, 0, 0);
			log.log(Level.INFO, "[UltraBan] " + admin + " banned player " + victim.getName() + ".");
		}
			
		
		if(broadcast){
			String banMsgBroadcast = config.getString("messages.banMsgBroadcast", "%victim% was banned by %admin%. Reason: %reason%");
			banMsgBroadcast = banMsgBroadcast.replaceAll("%admin%", admin);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%reason%", reason);
			if(victim == null){
			banMsgBroadcast = banMsgBroadcast.replaceAll("%victim%", p);
			}else{
				banMsgBroadcast = banMsgBroadcast.replaceAll("%victim%", victim.getName());
			}
			plugin.getServer().broadcastMessage(formatMessage(banMsgBroadcast));
		}else{
			String banMsgBroadcast = config.getString("messages.banMsgBroadcast", "%victim% was banned by %admin%. Reason: %reason%");
			banMsgBroadcast = banMsgBroadcast.replaceAll("%admin%", admin);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%reason%", reason);
			if(victim == null){
			banMsgBroadcast = banMsgBroadcast.replaceAll("%victim%", p);
			}else{
			banMsgBroadcast = banMsgBroadcast.replaceAll("%victim%", victim.getName());
			}
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
	public String formatMessage(String str){
		String funnyChar = new Character((char) 167).toString();
		str = str.replaceAll("&", funnyChar);
		return str;
	}
}
