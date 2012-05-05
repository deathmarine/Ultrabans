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

public class Kick implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.kick";
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

		String p = args[0].toLowerCase();
		if(autoComplete)
			p = expandName(p);
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

		if(p.equals("*")){
			if (sender instanceof Player)
				if(player.hasPermission("ultrabans.kick.all") || player.isOp()) auth = true;
			log.log(Level.INFO, "[UltraBan] " + admin + " kicked Everyone Reason: " + reason);
			Player[] pl = plugin.getServer().getOnlinePlayers();
			for (int i=0; i<pl.length; i++){
				if (pl[i] != player){
				String adminMsg = config.getString("messages.kickAllMsg", "Everyone has been kicked by %admin%. Reason: %reason%");
				adminMsg = adminMsg.replaceAll("%admin%", admin);
				adminMsg = adminMsg.replaceAll("%reason%", reason);
				pl[i].kickPlayer(formatMessage(adminMsg));
				}
			}
			return true;
		}
		if(plugin.autoComplete)
			p = expandName(p);
		Player victim = plugin.getServer().getPlayer(p);
		if(victim == null){
			sender.sendMessage(ChatColor.GRAY + "Player must be online!");
			return true;
		}
		plugin.db.addPlayer(victim.getName(), reason, admin, 0, 3);
		log.log(Level.INFO, "[UltraBan] " + admin + " kicked player " + victim.getName() + ". Reason: " + reason);
		String adminMsg = config.getString("messages.kickMsgVictim", "You have been kicked by %admin%. Reason: %reason%");
		adminMsg = adminMsg.replaceAll("%admin%", admin);
		adminMsg = adminMsg.replaceAll("%reason%", reason);
		victim.kickPlayer(formatMessage(adminMsg));
	
		if(broadcast){
			String kickMsgBroadcast = config.getString("messages.kickMsgBroadcast", "%victim% has been kicked by %admin%. Reason: %reason%");
			kickMsgBroadcast = kickMsgBroadcast.replaceAll("%admin%", admin);
			kickMsgBroadcast = kickMsgBroadcast.replaceAll("%victim%", victim.getName());
			kickMsgBroadcast = kickMsgBroadcast.replaceAll("%reason%", reason);
			plugin.getServer().broadcastMessage(formatMessage(kickMsgBroadcast));
		}else{
			String kickMsgBroadcast = config.getString("messages.kickMsgBroadcast", "%victim% has been kicked by %admin%. Reason: %reason%");
			kickMsgBroadcast = kickMsgBroadcast.replaceAll("%admin%", admin);
			kickMsgBroadcast = kickMsgBroadcast.replaceAll("%victim%", victim.getName());
			kickMsgBroadcast = kickMsgBroadcast.replaceAll("%reason%", reason);
			sender.sendMessage(formatMessage(":S:" + kickMsgBroadcast));
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
