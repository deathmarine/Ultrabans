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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.BanType;
import com.modcrafting.ultrabans.util.Formatting;

public class Ban implements CommandExecutor{
	Ultrabans plugin;
	public Ban(Ultrabans instance){
		plugin=instance;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+Ultrabans.DEFAULT_DENY_MESSAGE);
			return true;
		}
    	FileConfiguration config = Ultrabans.getPlugin().getConfig();
		boolean broadcast = true;
		String admin = Ultrabans.DEFAULT_ADMIN;
		String reason = Ultrabans.DEFAULT_REASON;
		if (sender instanceof Player){
			admin = sender.getName();
		}
		if (args.length < 1) return false;
		String p = args[0];
		p = Formatting.expandName(p);
		Player victim = Bukkit.getPlayer(p);
		if(args.length > 1){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = Formatting.combineSplit(2, args, " ");
			}else{
				if(args[1].equalsIgnoreCase("-a")
						&&sender.hasPermission(command.getPermission()+".anon")){
					admin = Ultrabans.DEFAULT_ADMIN;
					reason = Formatting.combineSplit(2, args, " ");
				}else{
					reason = Formatting.combineSplit(1, args, " ");
				}
			}
		}
		if(plugin.bannedPlayers.contains(p.toLowerCase())){
			String failed = config.getString("Messages.Ban.Failed", "%victim% is already banned.");
			if(failed.contains(Ultrabans.VICTIM)){
				if(victim == null){
					failed = failed.replaceAll(Ultrabans.VICTIM, p);
				}else{
					failed = failed.replaceAll(Ultrabans.VICTIM, victim.getName());
				}	
			}
			failed = Formatting.formatMessage(failed);
			sender.sendMessage(failed);
			return true;
		}
		if(victim == null){
			victim = Bukkit.getOfflinePlayer(p).getPlayer();
			if(victim != null){
				if(victim.hasPermission("ultraban.override.ban")
						&&!admin.equalsIgnoreCase(Ultrabans.ADMIN)){
					sender.sendMessage(ChatColor.RED + "Your ban has been denied!");
					return true;
				}
			}else{
				String bcmsg = config.getString("Messages.Ban.MsgToBroadcast", "%victim% was banned by %admin%. Reason: %reason%");
				if(bcmsg.contains(Ultrabans.ADMIN)) bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
				if(bcmsg.contains(Ultrabans.REASON)) bcmsg = bcmsg.replaceAll(Ultrabans.REASON, reason);
				if(bcmsg.contains(Ultrabans.VICTIM)) bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, p.toLowerCase());
				plugin.bannedPlayers.add(p.toLowerCase());
				if(config.getBoolean("CleanOnBan")) plugin.data.deletePlyrdat(p);
				final String fname = p;
				final String freason = reason;
				final String fadmin = admin;
				Bukkit.getScheduler().runTaskAsynchronously(Ultrabans.getPlugin(),new Runnable(){
					@Override
					public void run() {
						Ultrabans.getPlugin().getUBDatabase().addPlayer(fname, freason, fadmin, 0, BanType.BAN.getId());
					}	
				});
				if(plugin.getLog())
					plugin.getLogger().info(bcmsg);
				if(broadcast){
					Bukkit.broadcastMessage(Formatting.formatMessage(bcmsg));
				}else{
					sender.sendMessage(ChatColor.ITALIC + "Silent: " + Formatting.formatMessage(bcmsg));
				}
				return true;	
				
			}		
		}
		if(victim.getName().equalsIgnoreCase(admin)){
			String bcmsg = config.getString("Messages.Ban.Emo","You cannot ban yourself!");
			bcmsg = Formatting.formatMessage(bcmsg);
			sender.sendMessage(bcmsg);
			return true;
		}
		if(victim.hasPermission("ultraban.override.ban")&&!admin.equalsIgnoreCase(Ultrabans.DEFAULT_ADMIN)){
			String bcmsg = config.getString("Messages.Ban.Denied","Your ban has been denied!");
			bcmsg = Formatting.formatMessage(bcmsg);
			sender.sendMessage(bcmsg);
			return true;
		}
		plugin.bannedPlayers.add(victim.getName().toLowerCase());
		final String fname = victim.getName();
		final String freason = reason;
		final String fadmin = admin;
		Bukkit.getScheduler().runTaskAsynchronously(Ultrabans.getPlugin(),new Runnable(){
			@Override
			public void run() {
				Ultrabans.getPlugin().getUBDatabase().addPlayer(fname, freason, fadmin, 0, BanType.BAN.getId());
			}	
		});
		String vicmsg = config.getString("Messages.Ban.MsgToVictim", "You have been banned by %admin%. Reason: %reason%");
		if(vicmsg.contains(Ultrabans.ADMIN)) vicmsg = vicmsg.replaceAll(Ultrabans.ADMIN, admin);
		if(vicmsg.contains(Ultrabans.REASON)) vicmsg = vicmsg.replaceAll(Ultrabans.REASON, reason);
		victim.kickPlayer(Formatting.formatMessage(vicmsg));
		String bcmsg = config.getString("Messages.Ban.MsgToBroadcast", "%victim% was banned by %admin%. Reason: %reason%");
		if(bcmsg.contains(Ultrabans.ADMIN)) bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
		if(bcmsg.contains(Ultrabans.REASON)) bcmsg = bcmsg.replaceAll(Ultrabans.REASON, reason);
		if(bcmsg.contains(Ultrabans.VICTIM)) bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, p.toLowerCase());
		if(config.getBoolean("CleanOnBan",false)) plugin.data.deletePlyrdat(fname);
		if(config.getBoolean("ClearWarnOnBan",false)) plugin.getUBDatabase().clearWarns(fname);
		if(broadcast){
			Bukkit.broadcastMessage(Formatting.formatMessage(bcmsg));
		}else{
			sender.sendMessage(ChatColor.ITALIC + "Silent: " + Formatting.formatMessage(bcmsg));
		}
		if(plugin.getLog())
			plugin.getLogger().info(admin + " banned player " + fname + ".");
		return true;
	}
}
