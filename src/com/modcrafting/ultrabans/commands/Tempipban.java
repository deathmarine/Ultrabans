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
		
		if(plugin.autoComplete)
			p = plugin.util.expandName(p);
		Player victim = plugin.getServer().getPlayer(p);
		
		String reason = config.getString("defReason", "not sure");
		boolean broadcast = true;
		if(args.length > 3){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = plugin.util.combineSplit(4, args, " ");
			}else{
				if(args[1].equalsIgnoreCase("-a")){
					anon = true;
					reason = plugin.util.combineSplit(4, args, " ");
				}else{
				reason = plugin.util.combineSplit(3, args, " ");
				}
			}
		}

		if (anon){
			admin = config.getString("defAdminName", "server");
		}

		long tempTime = plugin.util.parseTimeSpec(args[1],args[2]);
		if(tempTime == 0)
			return false;
		long temp = System.currentTimeMillis()/1000+tempTime; //epoch time
		//Separate for Online-Offline
		if(victim != null){
			if(victim.getName() == admin){
				sender.sendMessage(ChatColor.RED + "You cannot emotempipban yourself!");
				return true;
			}
			if(victim.hasPermission( "ultraban.override.tempipban")){
				sender.sendMessage(ChatColor.RED + "Your tempipban has been denied! Player Notified!");
				victim.sendMessage(ChatColor.RED + "Player:" + admin + " Attempted to tempipban you!");
				return true;
			}	
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
			victim.kickPlayer(plugin.util.formatMessage(tempbanMsgVictim));
			if(broadcast){
				String tempbanMsgBroadcast = config.getString("messages.tempipbanMsgBroadcast", "%victim% was temp. banned by %admin%. Reason: %reason%!");
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%admin%", admin);
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%reason%", reason);
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%victim%", victim.getName());
				plugin.getServer().broadcastMessage(plugin.util.formatMessage(tempbanMsgBroadcast));
			}else{
				String tempbanMsgBroadcast = config.getString("messages.tempipbanMsgBroadcast", "%victim% was temp. banned by %admin%. Reason: %reason%!");
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%admin%", admin);
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%reason%", reason);
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%victim%", victim.getName());
				sender.sendMessage(plugin.util.formatMessage(":S:" + tempbanMsgBroadcast));
			}
		}else{
			victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
			if(victim != null){
				if(victim.hasPermission( "ultraban.override.tempipban")){
					sender.sendMessage(ChatColor.RED + "Your tempipban has been denied!");
					return true;
				}
			}
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
				plugin.getServer().broadcastMessage(plugin.util.formatMessage(tempbanMsgBroadcast));
			}else{
				String tempbanMsgBroadcast = config.getString("messages.tempbanMsgBroadcast", "%victim% was temp. banned by %admin%. Reason: %reason%!");
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%admin%", admin);
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%reason%", reason);
				tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll("%victim%", p);
				sender.sendMessage(plugin.util.formatMessage(":S:" + tempbanMsgBroadcast));
			}
		}
		return true;
	}
}
