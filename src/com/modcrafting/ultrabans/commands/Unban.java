/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Unban implements CommandExecutor{
	UltraBan plugin;
	public Unban(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		Player player = null;
		String admin = config.getString("defAdminName", "server");
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
		
		//unban IPv4
		if(plugin.util.validIP(p)){
			plugin.bannedIPs.remove(p);
			String pname = plugin.db.getName(p);
			Bukkit.unbanIP(p);
			if (pname != null){
				String reason = plugin.db.getBanReason(plugin.getServer().getOfflinePlayer(pname).getName());
				plugin.db.removeFromBanlist(plugin.getServer().getOfflinePlayer(pname).getName());
				plugin.db.addPlayer(plugin.getServer().getOfflinePlayer(p).getName(), "Unbanned: " + reason, admin, 0, 5);
				plugin.getLogger().info(admin + " unbanned player " + plugin.getServer().getOfflinePlayer(p).getName() + ".");				
			}else{
				plugin.db.removeFromBanlist(pname);			
			}
			String unbanMsgBroadcast = config.getString("messages.unbanMsgBroadcast", "%victim% was unbanned by %admin%!");
			unbanMsgBroadcast = unbanMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
			unbanMsgBroadcast = unbanMsgBroadcast.replaceAll(plugin.regexVictim, p);
			plugin.getServer().broadcastMessage(plugin.util.formatMessage(unbanMsgBroadcast));
			return true;
		}
		
		if(plugin.db.permaBan(p.toLowerCase())){
			sender.sendMessage(ChatColor.BLUE + p +  ChatColor.GRAY + " is PermaBanned.");
			plugin.getLogger().info(p + " is PermaBanned.");
			return true;
		}

		if(plugin.bannedPlayers.contains(p.toLowerCase())){
			plugin.bannedPlayers.remove(p.toLowerCase());
			String reason = plugin.db.getBanReason(plugin.getServer().getOfflinePlayer(p).getName());
			plugin.db.removeFromBanlist(plugin.getServer().getOfflinePlayer(p).getName());
			Bukkit.getOfflinePlayer(p).setBanned(false);
			String ip = plugin.db.getAddress(plugin.getServer().getOfflinePlayer(p).getName());
			if(plugin.bannedIPs.contains(ip)){
				plugin.bannedIPs.remove(ip);
				Bukkit.unbanIP(ip);
				System.out.println("Also removed the IP ban!");
			}
			plugin.db.addPlayer(plugin.getServer().getOfflinePlayer(p).getName(), "Unbanned: " + reason, admin, 0, 5);
			plugin.getLogger().info(admin + " unbanned player " + plugin.getServer().getOfflinePlayer(p).getName() + ".");
			String unbanMsgBroadcast = config.getString("messages.unbanMsgBroadcast", "%victim% was unbanned by %admin%!");
			unbanMsgBroadcast = unbanMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
			unbanMsgBroadcast = unbanMsgBroadcast.replaceAll(plugin.regexVictim, plugin.getServer().getOfflinePlayer(p).getName());
			plugin.getServer().broadcastMessage(plugin.util.formatMessage(unbanMsgBroadcast));
			return true;
		}else{
			if(plugin.tempBans.containsKey(p.toLowerCase())){
			plugin.tempBans.remove(p.toLowerCase());
			plugin.db.removeFromBanlist(plugin.getServer().getOfflinePlayer(p).getName());
			Bukkit.getOfflinePlayer(p).setBanned(false);
			String ip = plugin.db.getAddress(plugin.getServer().getOfflinePlayer(p).getName());
			if(plugin.bannedIPs.contains(ip)){
				plugin.bannedIPs.remove(ip);
				Bukkit.unbanIP(ip);
				System.out.println("Also removed the IP ban!");
			}
			plugin.db.addPlayer(p, "Unbanned", admin, 0, 5);
			plugin.getLogger().info(admin + " unbanned player " + plugin.getServer().getOfflinePlayer(p).getName() + ".");
			String unbanMsgBroadcast = config.getString("messages.unbanMsgBroadcast", "%victim% was unbanned by %admin%!");
			unbanMsgBroadcast = unbanMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
			unbanMsgBroadcast = unbanMsgBroadcast.replaceAll(plugin.regexVictim, plugin.getServer().getOfflinePlayer(p).getName());
			plugin.getServer().broadcastMessage(plugin.util.formatMessage(unbanMsgBroadcast));
			return true;
			}else{
			String unbanMsgFailed = config.getString("messages.unbanMsgFailed", "%victim% is already unbanned!");
			unbanMsgFailed = unbanMsgFailed.replaceAll(plugin.regexAdmin, admin);
			unbanMsgFailed = unbanMsgFailed.replaceAll(plugin.regexVictim, plugin.getServer().getOfflinePlayer(p).getName());
			sender.sendMessage(plugin.util.formatMessage(unbanMsgFailed));
			return true;
			}
		}
	}
}
