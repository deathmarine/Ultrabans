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

public class Warn implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.warn";
	public Warn(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean auth = false;
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

		String p = args[0]; // Get the victim's name from the command
		if(plugin.autoComplete)
			p = plugin.util.expandName(p); //If the admin has chosen to do so, autocomplete the name!
		Player victim = plugin.getServer().getPlayer(p); // What player is really the victim?
		// Reason stuff
		String reason = config.getString("defReason", "not sure");
		
		boolean broadcast = true;
		if(args.length > 1){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = plugin.util.combineSplit(2, args, " ");
			}else
				reason = plugin.util.combineSplit(1, args, " ");
			
		}
		if(victim != null){
			if(victim.getName() == admin){
				sender.sendMessage(ChatColor.RED + "You cannot emowarn yourself!");
				return true;
			}
			if(victim.hasPermission( "ultraban.override.warn")){
				sender.sendMessage(ChatColor.RED + "Your warning has been denied! Player Notified!");
				victim.sendMessage(ChatColor.RED + "Player:" + admin + " Attempted to warn you!");
				return true;
			}	
			if(config.getBoolean("enableMaxWarn", false)){
				Integer max = config.getInt("maxWarnings", 5);
				if(plugin.db.maxWarns(victim.getName()) != null &&  plugin.db.maxWarns(victim.getName()).size() > max){
					plugin.bannedPlayers.add(victim.getName().toLowerCase());
					plugin.db.addPlayer(victim.getName(), "Max Warnings", "Ultrabans", 0, 0);
					log.log(Level.INFO, "[UltraBan] player " + victim.getName() + " was banned for max warnings.");
					if(broadcast){
						String banMsgBroadcast = config.getString("messages.banMsgBroadcast", "%victim% was banned by Ultrabans. Reason: %reason%");
						banMsgBroadcast = banMsgBroadcast.replaceAll("%admin%", admin);
						banMsgBroadcast = banMsgBroadcast.replaceAll("%reason%", "Reached Max Warnings");
						banMsgBroadcast = banMsgBroadcast.replaceAll("%victim%", victim.getName());
						victim.kickPlayer(plugin.util.formatMessage(banMsgBroadcast));
						plugin.getServer().broadcastMessage(plugin.util.formatMessage(banMsgBroadcast));
					}
					return true;
				}	
			}
			plugin.db.addPlayer(victim.getName(), reason, admin, 0, 2);
			log.log(Level.INFO, "[UltraBan] " + admin + " warned player " + victim.getName() + ".");
			if(broadcast){ 
				String warnMsgBroadcast = config.getString("messages.warnMsgBroadcast", "%victim% was warned by %admin%. Reason: %reason%");
				warnMsgBroadcast = warnMsgBroadcast.replaceAll("%admin%", admin);
				warnMsgBroadcast = warnMsgBroadcast.replaceAll("%reason%", reason);
				warnMsgBroadcast = warnMsgBroadcast.replaceAll("%victim%", victim.getName());
				plugin.getServer().broadcastMessage(plugin.util.formatMessage(warnMsgBroadcast));
				return true;
			}else{
					String warnMsgVictim = config.getString("messages.warnMsgVictim", "You have been warned by %admin%. Reason: %reason%");
					warnMsgVictim = warnMsgVictim.replaceAll("%admin%", admin);
					warnMsgVictim = warnMsgVictim.replaceAll("%reason%", reason);
					String warnMsgBroadcast = config.getString("messages.warnMsgBroadcast", "%victim% was warned by %admin%. Reason: %reason%");
					warnMsgBroadcast = warnMsgBroadcast.replaceAll("%admin%", admin);
					warnMsgBroadcast = warnMsgBroadcast.replaceAll("%reason%", reason);
					warnMsgBroadcast = warnMsgBroadcast.replaceAll("%victim%", victim.getName());
					sender.sendMessage(plugin.util.formatMessage(":S:" + warnMsgBroadcast));
				return true;
				
			}	
		}else{
			victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
			if(victim != null){
				if(victim.hasPermission( "ultraban.override.warn")){
					sender.sendMessage(ChatColor.RED + "Your warning has been denied!");
					return true;
				}
			}
			plugin.db.addPlayer(p, reason, admin, 0, 2);
			log.log(Level.INFO, "[UltraBan] " + admin + " warned player " + p + ".");
			if(broadcast){ 
				String warnMsgBroadcast = config.getString("messages.warnMsgBroadcast", "%victim% was warned by %admin%. Reason: %reason%");
				warnMsgBroadcast = warnMsgBroadcast.replaceAll("%admin%", admin);
				warnMsgBroadcast = warnMsgBroadcast.replaceAll("%reason%", reason);
				warnMsgBroadcast = warnMsgBroadcast.replaceAll("%victim%", p);
				plugin.getServer().broadcastMessage(plugin.util.formatMessage(warnMsgBroadcast));
				return true;
			}else{
				String warnMsgVictim = config.getString("messages.warnMsgVictim", "You have been warned by %admin%. Reason: %reason%");
				warnMsgVictim = warnMsgVictim.replaceAll("%admin%", admin);
				warnMsgVictim = warnMsgVictim.replaceAll("%reason%", reason);
				String warnMsgBroadcast = config.getString("messages.warnMsgBroadcast", "%victim% was warned by %admin%. Reason: %reason%");
				warnMsgBroadcast = warnMsgBroadcast.replaceAll("%admin%", admin);
				warnMsgBroadcast = warnMsgBroadcast.replaceAll("%reason%", reason);
				warnMsgBroadcast = warnMsgBroadcast.replaceAll("%victim%", p);
				sender.sendMessage(plugin.util.formatMessage(":S:" + warnMsgBroadcast));
				return true;
			}
		}
	}
}
