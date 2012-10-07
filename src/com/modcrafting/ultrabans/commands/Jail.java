/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Jail implements CommandExecutor{
	UltraBan plugin;
    public Jail(UltraBan ultraBan) {
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
		if(args[0].equalsIgnoreCase("setjail")){
			plugin.jail.setJail(player.getLocation(), "jail");
			sender.sendMessage(ChatColor.GRAY + "Jail has been set!");
			return true;
		}
		if(args[0].equalsIgnoreCase("setrelease")){
			plugin.jail.setJail(player.getLocation(), "release");
			sender.sendMessage(ChatColor.GRAY + "Release has been set!");
			return true;
		}
		String p = args[0];
		p = plugin.util.expandName(p);
		
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
		
		Player victim = plugin.getServer().getPlayer(p);
		if(victim == null){
			if(plugin.jailed.contains(p)){
				sender.sendMessage(ChatColor.GRAY + p + " is already in jail.");
				return true;
			}else{
				sender.sendMessage(ChatColor.GRAY + p + " was not found to be jailed.");
			}
			sender.sendMessage(ChatColor.GRAY + "Player Must be online to be jailed.");
			return true;
		}else{
			if(victim.getName() == admin){
				sender.sendMessage(ChatColor.RED + "You cannot jail yourself!");
				return true;
			}
			if(victim.hasPermission( "ultraban.override.jail")){
				sender.sendMessage(ChatColor.RED + "Your jail attempt has been denied! Player Notified!");
				victim.sendMessage(ChatColor.RED + "Player: " + admin + " Attempted to jail you!");
				return true;
			}
			if(plugin.jailed.contains(victim.getName().toLowerCase())){
				sender.sendMessage(ChatColor.GRAY + victim.getName() + " is already in jail.");
				return true;
			}else{
				sender.sendMessage(ChatColor.GRAY + victim.getName() + " was not found to be jailed.");
			}
			
			String adminMsgAll = config.getString("messages.jailMsgBroadcast", "%victim% was jailed by %admin%. Reason: %reason%");
			if(adminMsgAll.contains(plugin.regexAdmin)) adminMsgAll = adminMsgAll.replaceAll(plugin.regexAdmin, admin);
			if(adminMsgAll.contains(plugin.regexReason)) adminMsgAll = adminMsgAll.replaceAll(plugin.regexReason, reason);
			if(adminMsgAll.contains(plugin.regexVictim)) adminMsgAll = adminMsgAll.replaceAll(plugin.regexVictim, p);

			String jailMsgVictim = config.getString("messages.jailMsgVictim", "You have been jailed by %admin%. Reason: %reason%!");
			if(jailMsgVictim.contains(plugin.regexAdmin)) jailMsgVictim = jailMsgVictim.replaceAll(plugin.regexAdmin, admin);
			if(jailMsgVictim.contains(plugin.regexVictim)) jailMsgVictim = jailMsgVictim.replaceAll(plugin.regexVictim, p);
			
			if(adminMsgAll != null){
				if(broadcast){
					plugin.getServer().broadcastMessage(plugin.util.formatMessage(adminMsgAll));
				}else{
					if(jailMsgVictim != null) victim.sendMessage(plugin.util.formatMessage(jailMsgVictim));
					sender.sendMessage(ChatColor.ITALIC + "Silent: " + plugin.util.formatMessage(adminMsgAll));
				}
			}
			plugin.db.addPlayer(p, reason, admin, 0, 6);
			plugin.jailed.add(p.toLowerCase());
			Location stlp = plugin.jail.getJail("jail");
			victim.teleport(stlp);
		}	
		return true;
	}

}

        
