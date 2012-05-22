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
		if(autoComplete) p = plugin.util.expandName(p); //If the admin has chosen to do so, autocomplete the name!
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
			String banMsgVictim = config.getString("messages.banMsgFailed", 
			"Player %victim% is already banned!");
			if(victim == null){
			banMsgVictim = banMsgVictim.replaceAll("%victim%", p);
			}else{
			banMsgVictim = banMsgVictim.replaceAll("%victim%", victim.getName());
			}
			sender.sendMessage(plugin.util.formatMessage(banMsgVictim));
			return true;
		}
		if(victim == null){
			victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
			if(victim != null){
				if(victim.hasPermission( "ultraban.override.ban")){
					sender.sendMessage(ChatColor.RED + "Your ban has been denied!");
					return true;
				}
			}
			String banMsgVictim = config.getString("messages.banMsgVictim", "You have been banned by %admin%. Reason: %reason%");
			banMsgVictim = banMsgVictim.replaceAll("%admin%", admin);
			banMsgVictim = banMsgVictim.replaceAll("%reason%", reason);
			plugin.bannedPlayers.add(p.toLowerCase());
			plugin.db.addPlayer(p, reason, admin, 0, 0);
			log.log(Level.INFO, "[UltraBan] " + admin + " banned player " + p + ".");
		}else{
			if(victim.getName() == admin){
				sender.sendMessage(ChatColor.RED + "You cannot emoban yourself!");
				return true;
			}
			if(victim.hasPermission( "ultraban.override.ban")){
				sender.sendMessage(ChatColor.RED + "Your ban has been denied! Player Notified!");
				victim.sendMessage(ChatColor.RED + "Player:" + admin + " Attempted to ban you!");
				return true;
			}	
			String banMsgVictim = config.getString("messages.banMsgVictim", "You have been banned by %admin%. Reason: %reason%");
			banMsgVictim = banMsgVictim.replaceAll("%admin%", admin);
			banMsgVictim = banMsgVictim.replaceAll("%reason%", reason);
			victim.kickPlayer(plugin.util.formatMessage(banMsgVictim));
			plugin.bannedPlayers.add(victim.getName().toLowerCase());
			plugin.db.addPlayer(victim.getName(), reason, admin, 0, 0);
			log.log(Level.INFO, "[UltraBan] " + admin + " banned player " + victim.getName() + ".");
		}
		if(broadcast){
			String banMsgBroadcast = config.getString("messages.banMsgBroadcast", "%victim% was banned by %admin%. Reason: %reason%");
			banMsgBroadcast = banMsgBroadcast.replaceAll("%admin%", admin);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%reason%", reason);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%victim%", p);
			plugin.getServer().broadcastMessage(plugin.util.formatMessage(banMsgBroadcast));
		}else{
			String banMsgBroadcast = config.getString("messages.banMsgBroadcast", "%victim% was banned by %admin%. Reason: %reason%");
			banMsgBroadcast = banMsgBroadcast.replaceAll("%admin%", admin);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%reason%", reason);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%victim%", p);
			sender.sendMessage(plugin.util.formatMessage(":S:" + banMsgBroadcast));
		}
		return true;
	}
}
