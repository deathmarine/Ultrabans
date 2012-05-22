package com.modcrafting.ultrabans.commands;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Ipban implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.ipban";
	public Ipban(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
		
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean auth = false;
		boolean anon = false;
		Player player = null;
		String admin = config.getString("defAdminName", "server");
		String reason = config.getString("defReason", "not sure");
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
		

		if (args.length < 1) return false;

		String p = args[0];
		
		//Pulls for Resembling a true ip.
		if(plugin.util.validIP(p)){
			plugin.bannedIPs.add(p);
			String pname = plugin.db.getName(p);
			if (pname != null){
				plugin.db.addPlayer(pname, reason, admin, 0, 1);
			}else{
				plugin.db.setAddress("failedname", p);
				plugin.db.addPlayer("failedname", reason, admin, 0, 1);
			}
			String banMsgBroadcast = config.getString("messages.banMsgBroadcast", "%victim% was banned by %admin%. Reason: %reason%");
			banMsgBroadcast = banMsgBroadcast.replaceAll("%admin%", admin);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%reason%", reason);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%victim%", p);
			plugin.getServer().broadcastMessage(plugin.util.formatMessage(banMsgBroadcast));
			return true;
		}
		/*
		if(p.contains("*")){
			plugin.ipscope.combine(p);
			//really bad idea. 255 to 65025 entries!!!
		}
		*/
		boolean broadcast = true;
		
		// Silent for Reason Combining
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
		if(plugin.autoComplete)
			p = plugin.util.expandName(p);
		Player victim = plugin.getServer().getPlayer(p); 
		if(victim == null){
			victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
			if(victim != null){
				if(victim.hasPermission( "ultraban.override.ipban")){
					sender.sendMessage(ChatColor.RED + "Your ipban has been denied!");
					return true;
				}
			}else{
				sender.sendMessage(ChatColor.RED + "Unable to find player!");
				return true;
			}
		}else{
			if(victim.getName() == admin){
				sender.sendMessage(ChatColor.RED + "You cannot ipban yourself!");
				return true;
			}
			if(victim.hasPermission( "ultraban.override.ipban")){
				sender.sendMessage(ChatColor.RED + "Your ipban has been denied! Player Notified!");
				victim.sendMessage(ChatColor.RED + "Player:" + admin + " Attempted to ipban you!");
				return true;
			}	
		}
		
		String victimip = plugin.db.getAddress(victim.getName().toLowerCase());
		//Running Online
		if(plugin.bannedIPs.contains(victimip)){
			sender.sendMessage(ChatColor.BLUE + victim.getName() +  ChatColor.GRAY + " is already banned for " + reason);
			return true;
		}
		plugin.bannedPlayers.add(victim.getName().toLowerCase());
		if(victimip != null){
		plugin.bannedIPs.add(victimip);
		Bukkit.banIP(victimip);
		}else{
			sender.sendMessage(ChatColor.GRAY + "IP address not found by Ultrabans for " + victim.getName());
			sender.sendMessage(ChatColor.GRAY + "Processed as a normal ban for " + victim.getName());
			plugin.db.addPlayer(victim.getName(), reason, admin, 0, 0);
			log.log(Level.INFO, "[UltraBan] " + admin + " banned player " + victim.getName() + ".");
			String banMsgVictim = config.getString("messages.banMsgVictim", "You have been banned by %admin%. Reason: %reason%");
			banMsgVictim = banMsgVictim.replaceAll("%admin%", admin);
			banMsgVictim = banMsgVictim.replaceAll("%reason%", reason);
			victim.kickPlayer(plugin.util.formatMessage(banMsgVictim));
			return true;
		}
		plugin.db.addPlayer(victim.getName(), reason, admin, 0, 1);
		log.log(Level.INFO, "[UltraBan] " + admin + " banned player " + victim.getName() + ".");
		String banMsgVictim = config.getString("messages.banMsgVictim", "You have been banned by %admin%. Reason: %reason%");
		banMsgVictim = banMsgVictim.replaceAll("%admin%", admin);
		banMsgVictim = banMsgVictim.replaceAll("%reason%", reason);
		victim.kickPlayer(plugin.util.formatMessage(banMsgVictim));
		if(broadcast){
			String banMsgBroadcast = config.getString("messages.banMsgBroadcast", "%victim% was banned by %admin%. Reason: %reason%");
			banMsgBroadcast = banMsgBroadcast.replaceAll("%admin%", admin);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%reason%", reason);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%victim%", victim.getName());
			plugin.getServer().broadcastMessage(plugin.util.formatMessage(banMsgBroadcast));
		}else{
			String banMsgBroadcast = config.getString("messages.banMsgBroadcast", "%victim% was banned by %admin%. Reason: %reason%");
			banMsgBroadcast = banMsgBroadcast.replaceAll("%admin%", admin);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%reason%", reason);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%victim%", victim.getName());
			sender.sendMessage(plugin.util.formatMessage(":S:" + banMsgBroadcast));
		}

		return true;
	}	
	
	
}
