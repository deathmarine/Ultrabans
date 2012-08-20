/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Kick implements CommandExecutor{
	UltraBan plugin;
	public Kick(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean broadcast = true;
		Player player = null;
		String admin = config.getString("defAdminName", "server");
		String reason = config.getString("defReason", "not sure");
		if (sender instanceof Player){
			player = (Player)sender;
			admin = player.getName();
		}
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		if (args.length < 1) return false;

		String p = args[0];
		if(plugin.autoComplete)	p = plugin.util.expandName(p);
		
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

		if(p.equals("*")){
			if(sender.hasPermission("ultrabans.kick.all")){
				plugin.getLogger().info(" " + admin + " kicked Everyone Reason: " + reason);
				for (Player players:plugin.getServer().getOnlinePlayers()){
					if (!players.hasPermission("ultraban.override.kick.all")){
					String adminMsg = config.getString("messages.kickAllMsg", "Everyone has been kicked by %admin%. Reason: %reason%");
					if(adminMsg.contains(plugin.regexAdmin)) adminMsg = adminMsg.replaceAll(plugin.regexAdmin, admin);
					if(adminMsg.contains(plugin.regexReason)) adminMsg = adminMsg.replaceAll(plugin.regexReason, reason);
					players.kickPlayer(plugin.util.formatMessage(adminMsg));
					}
				}
				
			}
			return true;
		}
		if(plugin.autoComplete) p = plugin.util.expandName(p);
		
		Player victim = plugin.getServer().getPlayer(p);
		if(victim == null){
			sender.sendMessage(ChatColor.GRAY + "Player must be online!");
			return true;
		}
		if(victim.getName() == admin){
			sender.sendMessage(ChatColor.RED + "You cannot kick yourself!");
			return true;
		}
		if(victim.hasPermission( "ultraban.override.kick")){
			sender.sendMessage(ChatColor.RED + "Your kick has been denied! Player Notified!");
			victim.sendMessage(ChatColor.RED + "Player: " + admin + " Attempted to kick you!");
			return true;
		}
		
		
		String kickMsgBroadcast = config.getString("messages.kickMsgBroadcast");
		if(kickMsgBroadcast.contains(plugin.regexAdmin)) kickMsgBroadcast = kickMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
		if(kickMsgBroadcast.contains(plugin.regexVictim)) kickMsgBroadcast = kickMsgBroadcast.replaceAll(plugin.regexVictim, victim.getName());
		if(kickMsgBroadcast.contains(plugin.regexReason)) kickMsgBroadcast = kickMsgBroadcast.replaceAll(plugin.regexReason, reason);
		if(kickMsgBroadcast != null){
			if(broadcast){
				plugin.getServer().broadcastMessage(plugin.util.formatMessage(kickMsgBroadcast));
			}else{
				sender.sendMessage(ChatColor.ITALIC + "Silent: " + plugin.util.formatMessage(kickMsgBroadcast));
			}
		}
		
		String adminMsg = config.getString("messages.kickMsgVictim", "You have been kicked by %admin%. Reason: %reason%");
		if(adminMsg.contains(plugin.regexAdmin)) adminMsg = adminMsg.replaceAll(plugin.regexAdmin, admin);
		if(adminMsg.contains(plugin.regexReason)) adminMsg = adminMsg.replaceAll(plugin.regexReason, reason);
		victim.kickPlayer(plugin.util.formatMessage(adminMsg));		
		
		plugin.db.addPlayer(victim.getName(), reason, admin, 0, 3);
		plugin.getLogger().info(" " + admin + " kicked player " + victim.getName() + ". Reason: " + reason);
		return true;
	}
}
