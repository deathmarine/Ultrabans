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

public class Perma implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.permaban";
	public Perma(UltraBan ultraBan) {
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
		// Has enough arguments?
		if (args.length < 1) return false;
		boolean autoComplete = config.getBoolean("auto-complete", true);
		String p = args[0];
		if(autoComplete) p = plugin.util.expandName(p);
		Player victim = plugin.getServer().getPlayer(p); // What player is really the victim?
		// Reason stuff
		String reason = config.getString("defReason", "not sure");
		boolean broadcast = true;
		if(args.length > 1){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = plugin.util.combineSplit(2, args, " ");
			}else{
				if(args[1].equalsIgnoreCase("-a")){
					anon = true;
					reason = plugin.util.combineSplit(2, args, " ");
				}else{
				reason = plugin.util.combineSplit(1, args, " ");
				}
			}
		}
		if (anon){
			admin = config.getString("defAdminName", "server");
		}

		if(plugin.bannedPlayers.contains(p.toLowerCase())){
			String adminMsg = config.getString("messages.banMsgFailed", 
			"&8Player &4%victim% &8is already banned!");
				if(victim == null){
					adminMsg = adminMsg.replaceAll("%victim%", p);
				}else{
					adminMsg = adminMsg.replaceAll("%victim%", victim.getName());
				}
			sender.sendMessage(plugin.util.formatMessage(adminMsg));
			return true;
		}

		if(victim == null){
			victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
			if(victim != null){
				if(victim.hasPermission( "ultraban.override.permaban")){
					sender.sendMessage(ChatColor.RED + "Your permaban attempt has been denied!");
					return true;
				}
				String adminMsg = config.getString("messages.banMsgVictim", "You have been permabanned by %admin%. Reason: %reason%");
				adminMsg = adminMsg.replaceAll("%admin%", admin);
				adminMsg = adminMsg.replaceAll("%reason%", reason);
				plugin.bannedPlayers.add(victim.getName().toLowerCase());
				plugin.db.addPlayer(victim.getName(), reason, admin, 0, 9);
				log.log(Level.INFO, "[UltraBan] " + admin + " permabanned player " + p + ".");				
			}else{
				sender.sendMessage(ChatColor.RED + "Unable to find player!");
			}
			
		}else{ 
			if(victim.getName() == admin){
				sender.sendMessage(ChatColor.RED + "You cannot permaban yourself!");
				return true;
			}
			if(victim.hasPermission( "ultraban.override.permaban")){
				sender.sendMessage(ChatColor.RED + "Your permaban has been denied! Player Notified!");
				victim.sendMessage(ChatColor.RED + "Player:" + admin + " Attempted to permaban you!");
				return true;
			}
			String adminMsg = config.getString("messages.banMsgVictim", "You have been permabanned by %admin%. Reason: %reason%");
			adminMsg = adminMsg.replaceAll("%admin%", admin);
			adminMsg = adminMsg.replaceAll("%reason%", reason);
			victim.kickPlayer(plugin.util.formatMessage(adminMsg));
			plugin.bannedPlayers.add(victim.getName().toLowerCase()); // Add name to HASHSET (RAM) Locally
			plugin.db.addPlayer(victim.getName(), reason, admin, 0, 9);
			log.log(Level.INFO, "[UltraBan] " + admin + " permabanned player " + victim.getName() + ".");
		}
		if(broadcast){
			String permbanMsgBroadcast = config.getString("messages.permbanMsgBroadcast", "%victim% has been permabanned by %admin%. Reason: %reason%");
			permbanMsgBroadcast = permbanMsgBroadcast.replaceAll("%admin%", admin);
			permbanMsgBroadcast = permbanMsgBroadcast.replaceAll("%reason%", reason);
			permbanMsgBroadcast = permbanMsgBroadcast.replaceAll("%victim%", victim.getName());
			plugin.getServer().broadcastMessage(plugin.util.formatMessage(permbanMsgBroadcast));
		}else{
			String permbanMsgBroadcast = config.getString("messages.permbanMsgBroadcast", "%victim% has been permabanned by %admin%. Reason: %reason%");
			permbanMsgBroadcast = permbanMsgBroadcast.replaceAll("%admin%", admin);
			permbanMsgBroadcast = permbanMsgBroadcast.replaceAll("%reason%", reason);
			permbanMsgBroadcast = permbanMsgBroadcast.replaceAll("%victim%", victim.getName());
			sender.sendMessage(plugin.util.formatMessage(permbanMsgBroadcast));
		}
		return true;
	}
}
