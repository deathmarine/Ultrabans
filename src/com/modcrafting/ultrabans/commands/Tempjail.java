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

public class Tempjail implements CommandExecutor{
	UltraBan plugin;
	String permission = "ultraban.tempjail";
	public Tempjail(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean broadcast = true;
		String admin = config.getString("defAdminName", "server");
		String reason = config.getString("defReason", "not sure");
		if (sender instanceof Player){
			admin = sender.getName();
		}
		if (!sender.hasPermission(permission)){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		if (args.length < 3) return false;

		String p = args[0];
		
		if(plugin.autoComplete) p = plugin.util.expandName(p);
		Player victim = plugin.getServer().getPlayer(p);
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
		long temp = System.currentTimeMillis()/1000+tempTime;
		if(victim != null){
			if(victim.getName() == admin){
				sender.sendMessage(ChatColor.RED + "You cannot tempjail yourself!");
				return true;
			}
			if(victim.hasPermission( "ultraban.override.tempjail")){
				sender.sendMessage(ChatColor.RED + "Your tempjail has been denied! Player Notified!");
				victim.sendMessage(ChatColor.RED + "Player: " + admin + " Attempted to tempjail you!");
				return true;
			}	
			if(plugin.jailed.contains(victim.getName().toLowerCase())){
				sender.sendMessage(ChatColor.BLUE + victim.getName() +  ChatColor.GRAY + " is already jailed for " + reason);
				return true;
			}
			plugin.tempJail.put(victim.getName().toLowerCase(), temp);
			plugin.db.addPlayer(victim.getName(), reason, admin, temp, 6);
			plugin.jailed.add(p.toLowerCase());
			
			plugin.getLogger().info(admin + " tempjailed player " + victim.getName() + ".");
			String tempjailMsgVictim = config.getString("messages.tempjailMsgVictim", "You have been temp. jailed by %admin%. Reason: %reason%!");
			if(tempjailMsgVictim.contains(plugin.regexAdmin)) tempjailMsgVictim = tempjailMsgVictim.replaceAll(plugin.regexAdmin, admin);
			if(tempjailMsgVictim.contains(plugin.regexReason)) tempjailMsgVictim = tempjailMsgVictim.replaceAll(plugin.regexReason, reason);
			if(tempjailMsgVictim.contains(plugin.regexVictim)) tempjailMsgVictim = tempjailMsgVictim.replaceAll(plugin.regexVictim, victim.getName());
			if(tempjailMsgVictim != null){
				sender.sendMessage(ChatColor.ITALIC + "Silent: " + plugin.util.formatMessage(tempjailMsgVictim));
			}
			String tempjailMsgBroadcast = config.getString("messages.tempjailMsgBroadcast", "%victim% was temp. jailed by %admin%. Reason: %reason%!");
			if(tempjailMsgBroadcast.contains(plugin.regexAdmin)) tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
			if(tempjailMsgBroadcast.contains(plugin.regexReason)) tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll(plugin.regexReason, reason);
			if(tempjailMsgBroadcast.contains(plugin.regexVictim)) tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll(plugin.regexVictim, p);
			if(tempjailMsgBroadcast != null){
				if(broadcast){
					plugin.getServer().broadcastMessage(plugin.util.formatMessage(tempjailMsgBroadcast));
				}else{
					sender.sendMessage(ChatColor.ITALIC + "Silent: " + plugin.util.formatMessage(tempjailMsgBroadcast));
				}
			}
			victim.teleport(plugin.jail.getJail("jail"));
			plugin.getLogger().info(admin+ " temp jailed player " + p + ".");
			return true;
		}else{
			victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
			if(victim != null){
				if(victim.hasPermission( "ultraban.override.tempjail")){
					sender.sendMessage(ChatColor.RED + "Your tempjail has been denied!");
					return true;
				}
			}
			if(plugin.jailed.contains(p.toLowerCase())){
				sender.sendMessage(ChatColor.BLUE + p +  ChatColor.GRAY + " is already jailed for " + reason);
				return true;
			}
			plugin.tempJail.put(p.toLowerCase(), temp);
			plugin.jailed.add(p.toLowerCase());
			plugin.db.addPlayer(p, reason, admin, temp, 6);
			
			String tempjailMsgBroadcast = config.getString("messages.tempjailMsgBroadcast", "%victim% was temp. jailed by %admin%. Reason: %reason%!");
			if(tempjailMsgBroadcast.contains(plugin.regexAdmin)) tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
			if(tempjailMsgBroadcast.contains(plugin.regexReason)) tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll(plugin.regexReason, reason);
			if(tempjailMsgBroadcast.contains(plugin.regexVictim)) tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll(plugin.regexVictim, p);
			if(tempjailMsgBroadcast != null){
				if(broadcast){
					plugin.getServer().broadcastMessage(plugin.util.formatMessage(tempjailMsgBroadcast));
				}else{
					sender.sendMessage(ChatColor.ITALIC + "Silent: " + plugin.util.formatMessage(tempjailMsgBroadcast));
				}
			}

			plugin.getLogger().info(admin + " temp jailed player " + p + ".");
			return true;
		}
	}
}
