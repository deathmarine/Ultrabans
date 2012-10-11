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

import com.modcrafting.ultrabans.Ultrabans;

public class Ipban implements CommandExecutor{
	Ultrabans plugin;
	public Ipban(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+plugin.perms);
			return true;
		}
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean broadcast = true;
		Player player = null;
		String admin = plugin.admin;
		String reason = plugin.reason;
		if (sender instanceof Player){
			player = (Player)sender;
			admin = player.getName();
		}
		if (args.length < 1) return false;

		String p = args[0];
		if(args.length > 1){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = plugin.util.combineSplit(2, args, " ");
			}else{
				if(args[1].equalsIgnoreCase("-a")){
					admin = plugin.admin;
					reason = plugin.util.combineSplit(2, args, " ");
				}else{
					reason = plugin.util.combineSplit(1, args, " ");
				}
			}
		}
		//IPv4 Check
		if(plugin.util.validIP(p)){
			plugin.bannedIPs.add(p);
			String pname = plugin.db.getName(p);
			if (pname != null){
				plugin.db.addPlayer(pname, reason, admin, 0, 1);
				plugin.bannedPlayers.add(pname);
			}else{
				plugin.db.setAddress(p, p);
				plugin.db.addPlayer(p, reason, admin, 0, 1);
			}
			String bcmsg = config.getString("Messages.IPBan.MsgToBroadcast","%victim% was ipbanned by %admin%. Reason: %reason%!");
			if(bcmsg.contains(plugin.regexAdmin)) bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
			if(bcmsg.contains(plugin.regexReason)) bcmsg = bcmsg.replaceAll(plugin.regexReason, reason);
			if(bcmsg.contains(plugin.regexVictim)) bcmsg = bcmsg.replaceAll(plugin.regexVictim, p);
			bcmsg=plugin.util.formatMessage(bcmsg);
			if(broadcast){
				plugin.getServer().broadcastMessage(bcmsg);
			}else{
				sender.sendMessage(ChatColor.ITALIC + "Silent: " + bcmsg);
			}
			plugin.getLogger().info(bcmsg);
			return true;
		}
		p = plugin.util.expandName(p);
		Player victim = plugin.getServer().getPlayer(p); 
		String victimip = null;
		if(victim == null){
			victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
			if(victim != null){
				if(victim.hasPermission("ultraban.override.ipban")&&!admin.equalsIgnoreCase(plugin.admin)){
					String bcmsg = config.getString("Messages.IPBan.Denied","Your ipban has been denied!");
					bcmsg = plugin.util.formatMessage(bcmsg);
					sender.sendMessage(bcmsg);
					return true;
				}
			}else{
				victimip = plugin.db.getAddress(p);
				if(victimip == null){
					String failed = config.getString("Messages.IPBan.IPNotFound", "IP address not found. Processed as a normal ban for %victim%!");
					if(failed.contains(plugin.regexVictim)) failed = failed.replaceAll(plugin.regexVictim, p);
					failed = plugin.util.formatMessage(failed);
					sender.sendMessage(failed);
					StringBuilder sb = new StringBuilder();
					sb.append("ban");
					sb.append(" ");
					sb.append(p);
					sb.append(" ");
					if(!broadcast){
						sb.append("-s");
						sb.append(" ");
					}
					sb.append(reason);
					if(player != null){
						player.getPlayer().performCommand(sb.toString());
					}else{
						plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), sb.toString());
					}
					return true;
				}
			}
		}else{
			if(victim.getName().equalsIgnoreCase(admin)){
				String bcmsg = config.getString("Messages.IPBan.Emo","You cannot ipban yourself!");
				bcmsg = plugin.util.formatMessage(bcmsg);
				sender.sendMessage(bcmsg);
				return true;
			}
			if(victim.hasPermission("ultraban.override.ipban")&&!admin.equalsIgnoreCase(plugin.admin)){
				String bcmsg = config.getString("Messages.IPBan.Denied","Your ipban has been denied!");
				bcmsg = plugin.util.formatMessage(bcmsg);
				sender.sendMessage(bcmsg);
				return true;
			}	
			victimip = plugin.db.getAddress(victim.getName().toLowerCase());
		}
		if(plugin.bannedIPs.contains(victimip)){
			String failed = config.getString("Messages.IPBan.Failed", "%victim% is already ipbanned.");
			if(failed.contains(plugin.regexVictim)) failed = failed.replaceAll(plugin.regexVictim, p);
			failed = plugin.util.formatMessage(failed);
			sender.sendMessage(failed);
			return true;
		}
		if(victimip == null){
			String failed = config.getString("Messages.IPBan.IPNotFound", "IP address not found. Processed as a normal ban for %victim%!");
			if(failed.contains(plugin.regexVictim)) failed = failed.replaceAll(plugin.regexVictim, p);
			failed = plugin.util.formatMessage(failed);
			sender.sendMessage(failed);
			StringBuilder sb = new StringBuilder();
			sb.append("ban");
			sb.append(" ");
			sb.append(p);
			sb.append(" ");
			if(!broadcast){
				sb.append("-s");
				sb.append(" ");
			}
			sb.append(reason);
			if(player != null){
				player.getPlayer().performCommand(sb.toString());
			}else{
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), sb.toString());
			}
			return true;
		}
		String msgvic = config.getString("Messages.IPBan.MsgToVictim", "You have been ipbanned by %admin%. Reason: %reason%!");
		if(msgvic.contains(plugin.regexAdmin)) msgvic = msgvic.replaceAll(plugin.regexAdmin, admin);
		if(msgvic.contains(plugin.regexReason)) msgvic = msgvic.replaceAll(plugin.regexReason, reason);
		msgvic=plugin.util.formatMessage(msgvic);
		if(victim != null && victim.isOnline()) victim.kickPlayer(msgvic);
		
		String bcmsg = config.getString("Messages.IPBan.MsgToBroadcast","%victim% was ipbanned by %admin%. Reason: %reason%!");
		if(bcmsg.contains(plugin.regexAdmin)) bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
		if(bcmsg.contains(plugin.regexReason)) bcmsg = bcmsg.replaceAll(plugin.regexReason, reason);
		if(bcmsg.contains(plugin.regexVictim)) bcmsg = bcmsg.replaceAll(plugin.regexVictim, p);
		bcmsg=plugin.util.formatMessage(bcmsg);
		if(broadcast){
			plugin.getServer().broadcastMessage(bcmsg);
		}else{
			sender.sendMessage(ChatColor.ITALIC + "Silent: " + bcmsg);
		}
		plugin.bannedPlayers.add(p.toLowerCase());
		plugin.bannedIPs.add(victimip);
		plugin.db.addPlayer(p.toLowerCase(), reason, admin, 0, 1);
		plugin.getLogger().info(bcmsg);
		return true;
	}	
	
	
}
