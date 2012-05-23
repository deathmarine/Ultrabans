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
				sender.sendMessage(ChatColor.RED + "You cannot warn yourself!");
				return true;
			}
			if(victim.hasPermission( "ultraban.override.warn")){
				sender.sendMessage(ChatColor.RED + "Your warning has been denied! Player Notified!");
				victim.sendMessage(ChatColor.RED + "Player:" + admin + " Attempted to warn you!");
				return true;
			}
			//Max Warning System
			if(config.getBoolean("enableMaxWarn", false)){
				Integer max = config.getInt("maxWarnings", 5);
				String idoit = victim.getName();
				if(plugin.db.maxWarns(idoit) != null && plugin.db.maxWarns(idoit).size() > max){
					plugin.bannedPlayers.add(idoit.toLowerCase());
					plugin.db.addPlayer(idoit, "Max Warnings", "Ultrabans", 0, 0);
					log.log(Level.INFO, "[UltraBan] player " + idoit + " was banned for max warnings.");
					if(broadcast){
						
						String cmd =config.getString("maxWarnResult", "ban");
						if(cmd.equalsIgnoreCase("ban") || cmd.equalsIgnoreCase("kick") || cmd.equalsIgnoreCase("ipban") || cmd.equalsIgnoreCase("jail") || cmd.equalsIgnoreCase("permaban")){
							player.getPlayer().performCommand(cmd + " " + idoit + " " + "-s" + " " + " Max Warnings");
						}else if(cmd.equalsIgnoreCase("tempban") || cmd.equalsIgnoreCase("tempipban") || cmd.equalsIgnoreCase("tempjail")){
							player.getPlayer().performCommand(cmd + " " + idoit + " " + "-s" + " "
									+ config.getString("maxWarnResulttime.amt", "5") + " " 
									+ config.getString("maxWarnResulttime.mode", "day") + " "
									+ "Max Warnings");
						}else{
							sender.sendMessage(ChatColor.RED + "Max Warnings improperly configured Defaulting to ban.");
							player.getPlayer().performCommand("ban" + " " + idoit + " " + " Max Warnings");
						}
						String banMsgBroadcast = "%victim% was %cmd% by Ultrabans. Reason: %reason%";
						banMsgBroadcast = banMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
						banMsgBroadcast = banMsgBroadcast.replaceAll(plugin.regexReason, "Reached Max Warnings");
						banMsgBroadcast = banMsgBroadcast.replaceAll(plugin.regexVictim, idoit);
						banMsgBroadcast = banMsgBroadcast.replaceAll("%cmd%", cmd);
						plugin.getServer().broadcastMessage(plugin.util.formatMessage(banMsgBroadcast));
					}
					return true;
				}	
			}
			
			plugin.db.addPlayer(victim.getName(), reason, admin, 0, 2);
			log.log(Level.INFO, "[UltraBan] " + admin + " warned player " + victim.getName() + ".");
			if(broadcast){ 
				String warnMsgBroadcast = config.getString("messages.warnMsgBroadcast", "%victim% was warned by %admin%. Reason: %reason%");
				warnMsgBroadcast = warnMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
				warnMsgBroadcast = warnMsgBroadcast.replaceAll(plugin.regexReason, reason);
				warnMsgBroadcast = warnMsgBroadcast.replaceAll(plugin.regexVictim, victim.getName());
				plugin.getServer().broadcastMessage(plugin.util.formatMessage(warnMsgBroadcast));
				return true;
			}else{
					String warnMsgVictim = config.getString("messages.warnMsgVictim", "You have been warned by %admin%. Reason: %reason%");
					warnMsgVictim = warnMsgVictim.replaceAll(plugin.regexAdmin, admin);
					warnMsgVictim = warnMsgVictim.replaceAll(plugin.regexReason, reason);
					String warnMsgBroadcast = config.getString("messages.warnMsgBroadcast", "%victim% was warned by %admin%. Reason: %reason%");
					warnMsgBroadcast = warnMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
					warnMsgBroadcast = warnMsgBroadcast.replaceAll(plugin.regexReason, reason);
					warnMsgBroadcast = warnMsgBroadcast.replaceAll(plugin.regexVictim, victim.getName());
					sender.sendMessage(plugin.util.formatMessage(":S:" + warnMsgBroadcast));
				return true;
				
			}	
		}else{

			String warnMsgVictim = config.getString("messages.warnMsgVictim", "You have been warned by %admin%. Reason: %reason%");
			warnMsgVictim = warnMsgVictim.replaceAll(plugin.regexAdmin, admin);
			warnMsgVictim = warnMsgVictim.replaceAll(plugin.regexReason, reason);
			String warnMsgBroadcast = config.getString("messages.warnMsgBroadcast", "%victim% was warned by %admin%. Reason: %reason%");
			warnMsgBroadcast = warnMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
			warnMsgBroadcast = warnMsgBroadcast.replaceAll(plugin.regexReason, reason);
			warnMsgBroadcast = warnMsgBroadcast.replaceAll(plugin.regexVictim, p);
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
				plugin.getServer().broadcastMessage(plugin.util.formatMessage(warnMsgBroadcast));
				return true;
			}else{
				sender.sendMessage(plugin.util.formatMessage(ChatColor.ITALIC + warnMsgBroadcast));
				return true;
			}
		}
	}
}
