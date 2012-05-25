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

public class Tempban implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.tempban";
	public Tempban(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean auth = false;
		boolean broadcast = true;
		Player player = null;
		String reason = config.getString("defReason", "not sure");
		String admin = config.getString("defAdminName", "server");
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
		if (args.length < 3) return false;
		// tempban arg0:1stname arg1:2nd time or expression arg2:3rdtime arg3:4th time or reason arg4:ifarg3=express reason
		long tempTime = 0;
		if(args.length > 3){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = plugin.util.combineSplit(4, args, " ");
				tempTime = plugin.util.parseTimeSpec(args[2],args[3]);
			}else if(args[1].equalsIgnoreCase("-a")){
				admin = config.getString("defAdminName", "server");
				reason = plugin.util.combineSplit(4, args, " ");
				tempTime = plugin.util.parseTimeSpec(args[2],args[3]);
			}else{
				tempTime = plugin.util.parseTimeSpec(args[1],args[2]);
				reason = plugin.util.combineSplit(3, args, " ");
				
			}
		}
		if(tempTime == 0) return false;
		
		String p = args[0];
		if(plugin.autoComplete) p = plugin.util.expandName(p);
		Player victim = plugin.getServer().getPlayer(p);
		long temp = System.currentTimeMillis()/1000+tempTime; //epoch time
		
		if(victim != null){
			if(victim.getName() == admin){
				sender.sendMessage(ChatColor.RED + "You cannot tempban yourself!");
				return true;
			}
			if(victim.hasPermission( "ultraban.override.tempban")){
				sender.sendMessage(ChatColor.RED + "Your tempban has been denied! Player Notified!");
				victim.sendMessage(ChatColor.RED + "Player: " + admin + " Attempted to tempban you!");
				return true;
			}	
			if(plugin.bannedPlayers.contains(victim.getName().toLowerCase())){
				sender.sendMessage(ChatColor.BLUE + victim.getName() +  ChatColor.GRAY + " is already banned for " + reason);
				return true;
			}
			plugin.tempBans.put(victim.getName().toLowerCase(), temp);
			plugin.db.addPlayer(victim.getName(), reason, admin, temp, 0);
			
			String tempbanMsgBroadcast = config.getString("messages.tempbanMsgBroadcast");
			if(tempbanMsgBroadcast.contains(plugin.regexAdmin)) tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
			if(tempbanMsgBroadcast.contains(plugin.regexReason)) tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll(plugin.regexReason, reason);
			if(tempbanMsgBroadcast.contains(plugin.regexVictim)) tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll(plugin.regexVictim, victim.getName());
			if(tempbanMsgBroadcast != null){
				if(broadcast){
					plugin.getServer().broadcastMessage(plugin.util.formatMessage(tempbanMsgBroadcast));
				}else{
					sender.sendMessage(ChatColor.ITALIC + "Silent: " + plugin.util.formatMessage(tempbanMsgBroadcast));
				}
			}

			String tempbanMsgVictim = config.getString("messages.tempbanMsgVictim", "You have been temp. banned by %admin%. Reason: %reason%!");
			if(tempbanMsgVictim.contains(plugin.regexAdmin)) tempbanMsgVictim = tempbanMsgVictim.replaceAll(plugin.regexAdmin, admin);
			if(tempbanMsgVictim.contains(plugin.regexReason)) tempbanMsgVictim = tempbanMsgVictim.replaceAll(plugin.regexReason, reason);
			victim.kickPlayer(plugin.util.formatMessage(tempbanMsgVictim));
			log.log(Level.INFO, "[UltraBan] " + admin + " tempbanned player " + victim.getName() + ".");
			return true;
		}else{
			victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
			if(victim != null){
				if(victim.hasPermission( "ultraban.override.tempban")){
					sender.sendMessage(ChatColor.RED + "Your tempban has been denied!");
					return true;
				}
			}
			if(plugin.bannedPlayers.contains(p.toLowerCase())){
				sender.sendMessage(ChatColor.BLUE + p +  ChatColor.GRAY + " is already banned for " + reason);
				return true;
			}
			plugin.tempBans.put(p.toLowerCase(), temp);
			plugin.db.addPlayer(p, reason, admin, temp, 0);
			
			String tempbanMsgBroadcast = config.getString("messages.tempbanMsgBroadcast");
			if(tempbanMsgBroadcast.contains(plugin.regexAdmin)) tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
			if(tempbanMsgBroadcast.contains(plugin.regexReason)) tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll(plugin.regexReason, reason);
			if(tempbanMsgBroadcast.contains(plugin.regexVictim)) tempbanMsgBroadcast = tempbanMsgBroadcast.replaceAll(plugin.regexVictim, p);
			if(tempbanMsgBroadcast != null){
				if(broadcast){
					plugin.getServer().broadcastMessage(plugin.util.formatMessage(tempbanMsgBroadcast));
				}else{
					sender.sendMessage(ChatColor.ITALIC + "Silent: " + plugin.util.formatMessage(tempbanMsgBroadcast));
				}
			}
			

			log.log(Level.INFO, "[UltraBan] " + admin + " tempbanned player " + p + ".");
			return true;
		}
	}
}
