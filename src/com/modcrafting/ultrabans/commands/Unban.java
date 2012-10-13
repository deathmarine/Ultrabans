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

import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.tracker.Track;

public class Unban implements CommandExecutor{
	Ultrabans plugin;
	public Unban(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Track.track(command.getName());
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
    	String admin=plugin.admin;
		Player player = null;
		if (sender instanceof Player){
			player = (Player)sender;
			admin = player.getName();
		}
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+plugin.perms);
			return true;
		}
		if (args.length < 1) return false;
		String p = args[0];
		
		//unban IPv4
		if(plugin.util.validIP(p)){
			plugin.bannedIPs.remove(p);
			String pname = plugin.db.getName(p);
			if (pname != null){
				String reason = plugin.db.getBanReason(plugin.getServer().getOfflinePlayer(pname).getName());
				plugin.db.removeFromBanlist(plugin.getServer().getOfflinePlayer(pname).getName());
				plugin.db.addPlayer(plugin.getServer().getOfflinePlayer(p).getName(), "Unbanned: " + reason, admin, 0, 5);
				plugin.getLogger().info(admin + " unbanned player " + plugin.getServer().getOfflinePlayer(p).getName() + ".");				
			}else{
				plugin.db.removeFromBanlist(pname);			
			}
			String bcmsg = config.getString("Messages.Unban.MsgToBroadcast", "%victim% was unbanned by %admin%!");
			bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
			bcmsg = bcmsg.replaceAll(plugin.regexVictim, p);
			plugin.getServer().broadcastMessage(plugin.util.formatMessage(bcmsg));
			return true;
		}
		
		if(plugin.db.permaBan(p.toLowerCase())){
			String perma = config.getString("Messages.Unban.PermaBanned", "%victim% was unbanned by %admin%!");
			perma = perma.replaceAll(plugin.regexVictim, p);
			perma = plugin.util.formatMessage(perma);
			sender.sendMessage(perma);
			plugin.getLogger().info(perma);
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
				plugin.getLogger().info("Also removed the IP ban!");
			}
			plugin.db.addPlayer(plugin.getServer().getOfflinePlayer(p).getName(), "Unbanned: " + reason, admin, 0, 5);
			plugin.getLogger().info(admin + " unbanned player " + plugin.getServer().getOfflinePlayer(p).getName() + ".");
			String bcmsg = config.getString("Messages.Unban.MsgToBroadcast", "%victim% was unbanned by %admin%!");
			bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
			bcmsg = bcmsg.replaceAll(plugin.regexVictim, plugin.getServer().getOfflinePlayer(p).getName());
			plugin.getServer().broadcastMessage(plugin.util.formatMessage(bcmsg));
			return true;
		}else{
			if(plugin.tempBans.containsKey(p.toLowerCase())){
				plugin.tempBans.remove(p.toLowerCase());
				plugin.db.removeFromBanlist(plugin.getServer().getOfflinePlayer(p).getName());
				String ip = plugin.db.getAddress(plugin.getServer().getOfflinePlayer(p).getName());
				if(plugin.bannedIPs.contains(ip)){
					plugin.bannedIPs.remove(ip);
					System.out.println("Also removed the IP ban!");
				}
				plugin.db.addPlayer(p, "Unbanned", admin, 0, 5);
				String bcmsg = config.getString("Messages.Unban.MsgToBroadcast", "%victim% was unbanned by %admin%!");
				bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
				bcmsg = bcmsg.replaceAll(plugin.regexVictim, plugin.getServer().getOfflinePlayer(p).getName());
				bcmsg = plugin.util.formatMessage(bcmsg);
				plugin.getServer().broadcastMessage(bcmsg);
				plugin.getLogger().info(bcmsg);
				
			}else{
				String failed = config.getString("Messages.Unban.Failed", "%victim% is already unbanned!");
				failed = failed.replaceAll(plugin.regexVictim, plugin.getServer().getOfflinePlayer(p).getName());
				failed = plugin.util.formatMessage(failed);
				sender.sendMessage(failed);
			}
			return true;
		}
	}
}
