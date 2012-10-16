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
import com.modcrafting.ultrabans.tracker.Track;

public class Ban implements CommandExecutor{
	Ultrabans plugin;
	public Ban(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Track.track(command.getName());
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+plugin.perms);
			return true;
		}
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean broadcast = true;
		String admin = plugin.admin;
		String reason = plugin.reason;
		if (sender instanceof Player){
			admin = sender.getName();
		}
		if (args.length < 1) return false;
		
		String p = args[0];
		p = plugin.util.expandName(p);
		Player victim = plugin.getServer().getPlayer(p);
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
	
		if(plugin.bannedPlayers.contains(p.toLowerCase())){
			String failed = config.getString("Messages.Ban.Failed", "%victim% is already banned.");
			if(failed.contains(plugin.regexVictim)){
				if(victim == null){
					failed = failed.replaceAll(plugin.regexVictim, p);
				}else{
					failed = failed.replaceAll(plugin.regexVictim, victim.getName());
				}	
			}
			failed = plugin.util.formatMessage(failed);
			sender.sendMessage(failed);
			return true;
		}
		
		if(victim == null){
			victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
			if(victim != null){
				if(victim.hasPermission("ultraban.override.ban")&&!admin.equalsIgnoreCase(plugin.admin)){
					sender.sendMessage(ChatColor.RED + "Your ban has been denied!");
					return true;
				}
			}else{
				String bcmsg = config.getString("Messages.Ban.MsgToBroadcast", "%victim% was banned by %admin%. Reason: %reason%");
				if(bcmsg.contains(plugin.regexAdmin)) bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
				if(bcmsg.contains(plugin.regexReason)) bcmsg = bcmsg.replaceAll(plugin.regexReason, reason);
				if(bcmsg.contains(plugin.regexVictim)) bcmsg = bcmsg.replaceAll(plugin.regexVictim, p.toLowerCase());
				plugin.bannedPlayers.add(p.toLowerCase());
				if(config.getBoolean("CleanOnBan")) plugin.data.deletePlyrdat(p);
				plugin.db.addPlayer(p, reason, admin, 0, 0);
				plugin.getLogger().info(bcmsg);
				if(broadcast){
					plugin.getServer().broadcastMessage(plugin.util.formatMessage(bcmsg));
				}else{
					sender.sendMessage(ChatColor.ITALIC + "Silent: " + plugin.util.formatMessage(bcmsg));
				}
				return true;	
				
			}		
		}

		if(victim.getName().equalsIgnoreCase(admin)){
			String bcmsg = config.getString("Messages.Ban.Emo","You cannot ban yourself!");
			bcmsg = plugin.util.formatMessage(bcmsg);
			sender.sendMessage(bcmsg);
			return true;
		}
		if(victim.hasPermission("ultraban.override.ban")&&!admin.equalsIgnoreCase(plugin.admin)){
			String bcmsg = config.getString("Messages.Ban.Denied","Your ban has been denied!");
			bcmsg = plugin.util.formatMessage(bcmsg);
			sender.sendMessage(bcmsg);
			return true;
		}
		
		//Lowercase Cache / Normal Case Storage
		plugin.bannedPlayers.add(victim.getName().toLowerCase());
		plugin.db.addPlayer(victim.getName(), reason, admin, 0, 0);
		
		//Build Messages
		String vicmsg = config.getString("Messages.Ban.MsgToVictim", "You have been banned by %admin%. Reason: %reason%");
		if(vicmsg.contains(plugin.regexAdmin)) vicmsg = vicmsg.replaceAll(plugin.regexAdmin, admin);
		if(vicmsg.contains(plugin.regexReason)) vicmsg = vicmsg.replaceAll(plugin.regexReason, reason);
		victim.kickPlayer(plugin.util.formatMessage(vicmsg));
		
		String bcmsg = config.getString("Messages.Ban.MsgToBroadcast", "%victim% was banned by %admin%. Reason: %reason%");
		if(bcmsg.contains(plugin.regexAdmin)) bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
		if(bcmsg.contains(plugin.regexReason)) bcmsg = bcmsg.replaceAll(plugin.regexReason, reason);
		if(bcmsg.contains(plugin.regexVictim)) bcmsg = bcmsg.replaceAll(plugin.regexVictim, p.toLowerCase());
		if(config.getBoolean("CleanOnBan",false)) plugin.data.deletePlyrdat(victim.getName());
		if(broadcast){
			plugin.getServer().broadcastMessage(plugin.util.formatMessage(bcmsg));
		}else{
			sender.sendMessage(ChatColor.ITALIC + "Silent: " + plugin.util.formatMessage(bcmsg));
		}
		plugin.getLogger().info(admin + " banned player " + victim.getName() + ".");
		return true;
	}
}
