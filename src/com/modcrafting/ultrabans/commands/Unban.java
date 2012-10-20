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
import org.bukkit.OfflinePlayer;
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
		OfflinePlayer of = plugin.getServer().getOfflinePlayer(p);
		if(of!=null){
			if(of.isBanned()) of.setBanned(false);
			p=of.getName();
		}
		//unban IPv4
		if(plugin.util.validIP(p)){
			plugin.bannedIPs.remove(p);
			String pname = plugin.db.getName(p);
			if (pname != null){
				of = plugin.getServer().getOfflinePlayer(pname);
				p=of.getName();						
				String reason = plugin.db.getBanReason(p);
				plugin.db.removeFromBanlist(p);
				plugin.db.addPlayer(p, "Unbanned: " + reason, admin, 0, 5);
				plugin.getLogger().info(admin + " unbanned player " + p + ".");				
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
			plugin.db.removeFromBanlist(p);
			Bukkit.getOfflinePlayer(p).setBanned(false);
			String ip = plugin.db.getAddress(p);
			if(plugin.bannedIPs.contains(ip)){
				plugin.bannedIPs.remove(ip);
				plugin.getLogger().info("Also removed the IP ban!");
			}
			if(config.getBoolean("UnbansLog.Enable",true)){
				String reason = plugin.db.getBanReason(p);
				if(config.getBoolean("UnbansLog.LogReason",true)&&reason!=null){
					plugin.db.addPlayer(p, "Unbanned: "+reason, admin, 0, 5);
				}else{
					plugin.db.addPlayer(p, "Unbanned", admin, 0, 5);
				}
			}
			plugin.getLogger().info(admin + " unbanned player " + p + ".");
			String bcmsg = config.getString("Messages.Unban.MsgToBroadcast", "%victim% was unbanned by %admin%!");
			bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
			bcmsg = bcmsg.replaceAll(plugin.regexVictim, p);
			plugin.getServer().broadcastMessage(plugin.util.formatMessage(bcmsg));
			return true;
		}else{
			if(plugin.tempBans.containsKey(p.toLowerCase())){
				plugin.tempBans.remove(p.toLowerCase());
				plugin.db.removeFromBanlist(p);
				String ip = plugin.db.getAddress(p);
				if(plugin.bannedIPs.contains(ip)){
					plugin.bannedIPs.remove(ip);
					System.out.println("Also removed the IP ban!");
				}
				if(config.getBoolean("UnbansLog.Enable",true)){
					String reason = plugin.db.getBanReason(p);
					if(config.getBoolean("UnbansLog.LogReason",true)&&reason!=null){
						plugin.db.addPlayer(p, "Unbanned: "+reason, admin, 0, 5);
					}else{
						plugin.db.addPlayer(p, "Unbanned", admin, 0, 5);
					}
				}
				String bcmsg = config.getString("Messages.Unban.MsgToBroadcast", "%victim% was unbanned by %admin%!");
				bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
				bcmsg = bcmsg.replaceAll(plugin.regexVictim, p);
				bcmsg = plugin.util.formatMessage(bcmsg);
				plugin.getServer().broadcastMessage(bcmsg);
				plugin.getLogger().info(bcmsg);
			}else{
				if(of!=null){
					String bcmsg = config.getString("Messages.Unban.MsgToBroadcast", "%victim% was unbanned by %admin%!");
					bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
					bcmsg = bcmsg.replaceAll(plugin.regexVictim, p);
					bcmsg = plugin.util.formatMessage(bcmsg);
					plugin.getServer().broadcastMessage(bcmsg);
					plugin.getLogger().info(bcmsg);
					return true;
				}
				String failed = config.getString("Messages.Unban.Failed", "%victim% is already unbanned!");
				failed = failed.replaceAll(plugin.regexVictim, p);
				failed = plugin.util.formatMessage(failed);
				sender.sendMessage(failed);
			}
			return true;
		}
	}
}
