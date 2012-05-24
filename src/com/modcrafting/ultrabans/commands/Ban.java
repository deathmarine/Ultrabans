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
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean auth = false;
		boolean broadcast = true;
		Player player = null;
		String admin = config.getString("defAdminName", "server");
		String reason = config.getString("defReason", "not sure");
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission) || player.isOp()) auth = true;
			admin = player.getName();
		}else{
			auth = true;
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		if (args.length < 1) return false;
		
		String p = args[0];
		
		if(plugin.autoComplete) p = plugin.util.expandName(p);
		Player victim = plugin.getServer().getPlayer(p);
		
		//Additional Argument Check
		if(args.length > 1){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = plugin.util.combineSplit(2, args, " ");
			}else{
				if(args[1].equalsIgnoreCase("-a")){
					admin = config.getString("defAdminName", "server");
					reason = plugin.util.combineSplit(2, args, " ");
				}else{
					reason = plugin.util.combineSplit(1, args, " ");
				}
			}
		}
		
		//Previous Check
		if(plugin.bannedPlayers.contains(p.toLowerCase())){
			String banMsgVictim = config.getString("messages.banMsgFailed");
			if(banMsgVictim.contains(plugin.regexVictim)){
				if(victim == null){
					banMsgVictim = banMsgVictim.replaceAll(plugin.regexVictim, p);
				}else{
					banMsgVictim = banMsgVictim.replaceAll(plugin.regexVictim, victim.getName());
				}				
			}
			if(banMsgVictim != null) sender.sendMessage(plugin.util.formatMessage(banMsgVictim));
			return true;
		}
		
		/* 
		 * Offline Ban
		 */
		if(victim == null){
			victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
			if(victim != null){
				if(victim.hasPermission( "ultraban.override.ban")){
					sender.sendMessage(ChatColor.RED + "Your ban has been denied!");
					return true;
				}
			}
			String banMsgBroadcast = config.getString("messages.banMsgBroadcast", "%victim% was banned by %admin%. Reason: %reason%");
			if(banMsgBroadcast.contains(plugin.regexAdmin)) banMsgBroadcast = banMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
			if(banMsgBroadcast.contains(plugin.regexReason)) banMsgBroadcast = banMsgBroadcast.replaceAll(plugin.regexReason, reason);
			if(banMsgBroadcast.contains(plugin.regexVictim)) banMsgBroadcast = banMsgBroadcast.replaceAll(plugin.regexVictim, p.toLowerCase());
			plugin.bannedPlayers.add(p.toLowerCase());
			plugin.db.addPlayer(p.toLowerCase(), reason, admin, 0, 0);
			log.log(Level.INFO, "[UltraBan] " + admin + " banned player " + p + ".");
			if(broadcast){
				plugin.getServer().broadcastMessage(plugin.util.formatMessage(banMsgBroadcast));
			}else{
				sender.sendMessage(ChatColor.ITALIC + "Silent: " + plugin.util.formatMessage(banMsgBroadcast));
			}
			return true;			
		}
		/*
		 * End of Offline
		 */
		
		//Emo-Command
		if(victim.getName().equalsIgnoreCase(admin)){
			sender.sendMessage(ChatColor.RED + "You cannot ban yourself!");
			return true;
		}
		//Override
		if(victim.hasPermission("ultraban.override.ban")){
			sender.sendMessage(ChatColor.RED + "Your ban has been denied! Player Notified!");
			victim.sendMessage(ChatColor.RED + "Player: " + admin + " Attempted to ban you!");
			return true;
		}
		
		//Lowercase Cache / Normal Case Storage
		plugin.bannedPlayers.add(victim.getName().toLowerCase());
		plugin.db.addPlayer(victim.getName(), reason, admin, 0, 0);
		
		//Build Messages
		String banMsgVictim = config.getString("messages.banMsgVictim", "You have been banned by %admin%. Reason: %reason%");
		if(banMsgVictim.contains(plugin.regexAdmin)) banMsgVictim = banMsgVictim.replaceAll(plugin.regexAdmin, admin);
		if(banMsgVictim.contains(plugin.regexReason)) banMsgVictim = banMsgVictim.replaceAll(plugin.regexReason, reason);
		
		String banMsgBroadcast = config.getString("messages.banMsgBroadcast", "%victim% was banned by %admin%. Reason: %reason%");
		if(banMsgBroadcast.contains(plugin.regexAdmin)) banMsgBroadcast = banMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
		if(banMsgBroadcast.contains(plugin.regexReason)) banMsgBroadcast = banMsgBroadcast.replaceAll(plugin.regexReason, reason);
		if(banMsgBroadcast.contains(plugin.regexVictim)) banMsgBroadcast = banMsgBroadcast.replaceAll(plugin.regexVictim, p.toLowerCase());

		//Completion
		victim.kickPlayer(plugin.util.formatMessage(banMsgVictim));
		if(broadcast){
			plugin.getServer().broadcastMessage(plugin.util.formatMessage(banMsgBroadcast));
		}else{
			sender.sendMessage(ChatColor.ITALIC + "Silent: " + plugin.util.formatMessage(banMsgBroadcast));
		}
		
		//Log
		log.log(Level.INFO, "[UltraBan] " + admin + " banned player " + victim.getName() + ".");
		return true;
	}
}
