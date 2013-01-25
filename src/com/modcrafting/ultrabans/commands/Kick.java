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
import com.modcrafting.ultrabans.util.BanType;
import com.modcrafting.ultrabans.util.Formatting;

public class Kick implements CommandExecutor{
	Ultrabans plugin;
	public Kick(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(final CommandSender sender, Command command, String label, final String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+Ultrabans.DEFAULT_DENY_MESSAGE);
			return true;
		}
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean broadcast = true;
		Player player = null;
		String admin = Ultrabans.DEFAULT_ADMIN;
		String reason = Ultrabans.DEFAULT_REASON;
		if (sender instanceof Player){
			player = (Player)sender;
			admin = player.getName();
		}
		if (args.length < 1) return false;

		String p = args[0];
		if(args.length > 1){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = Formatting.combineSplit(2, args, " ");
			}else{
				if(args[1].equalsIgnoreCase("-a")){
					admin = Ultrabans.DEFAULT_ADMIN;
					reason = Formatting.combineSplit(2, args, " ");
				}else{
				reason = Formatting.combineSplit(1, args, " ");
				}
			}
		}
		if(args[0].equals("*")&&sender.hasPermission("ultrabans.kick.all")){
				String adminMsg = config.getString("Messages.Kick.MsgToAll", "Everyone has been kicked by %admin%. Reason: %reason%");
				if(adminMsg.contains(Ultrabans.ADMIN)) adminMsg = adminMsg.replaceAll(Ultrabans.ADMIN, admin);
				if(adminMsg.contains(Ultrabans.REASON)) adminMsg = adminMsg.replaceAll(Ultrabans.REASON, reason);
				adminMsg=Formatting.formatMessage(adminMsg);
				for (Player players:plugin.getServer().getOnlinePlayers()){
					if (!players.hasPermission("ultraban.override.kick.all")){
						players.kickPlayer(adminMsg);
					}
				}
				plugin.getServer().broadcastMessage(adminMsg);
				plugin.getLogger().info(adminMsg);
			
			return true;
		}
		p = Formatting.expandName(p);
		Player victim = plugin.getServer().getPlayer(p);
		if(victim == null){
			String smvic = config.getString("Messages.Kick.Online","%victim% must be online.");
			if(smvic.contains(Ultrabans.VICTIM))smvic=smvic.replaceAll(Ultrabans.VICTIM, p);
			smvic=Formatting.formatMessage(smvic);
			sender.sendMessage(ChatColor.GRAY + smvic);
			return true;
		}
		if(victim.getName().equalsIgnoreCase(admin)){
			String bcmsg = config.getString("Messages.Kick.Emo","You cannot kick yourself!");
			bcmsg = Formatting.formatMessage(bcmsg);
			sender.sendMessage(bcmsg);
			return true;
		}
		if(victim.hasPermission( "ultraban.override.kick")&&!admin.equalsIgnoreCase(Ultrabans.DEFAULT_ADMIN)){
			String bcmsg = config.getString("Messages.Kick.Denied","Your kick has been denied!");
			bcmsg = Formatting.formatMessage(bcmsg);
			sender.sendMessage(bcmsg);
			return true;
		}
		String msgvic = config.getString("Messages.Kick.MsgToVictim", "You have been kicked by %admin%. Reason: %reason%");
		if(msgvic.contains(Ultrabans.ADMIN)) msgvic = msgvic.replaceAll(Ultrabans.ADMIN, admin);
		if(msgvic.contains(Ultrabans.REASON)) msgvic = msgvic.replaceAll(Ultrabans.REASON, reason);
		msgvic=Formatting.formatMessage(msgvic);
		victim.kickPlayer(msgvic);
		
		String bcmsg = config.getString("Messages.Kick.MsgToBroadcast","%victim% was kicked by %admin%. Reason: %reason%!");
		if(bcmsg.contains(Ultrabans.ADMIN)) bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
		if(bcmsg.contains(Ultrabans.REASON)) bcmsg = bcmsg.replaceAll(Ultrabans.REASON, reason);
		if(bcmsg.contains(Ultrabans.VICTIM)) bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, victim.getName());
		bcmsg=Formatting.formatMessage(bcmsg);
		if(broadcast){
			plugin.getServer().broadcastMessage(bcmsg);
		}else{
			sender.sendMessage(ChatColor.ITALIC + "Silent: " + bcmsg);
		}
		final String fname = victim.getName();
		final String freason = reason;
		final String fadmin = admin;
		Bukkit.getScheduler().scheduleSyncDelayedTask(Ultrabans.getPlugin(),new Runnable(){
			@Override
			public void run() {
				Ultrabans.getPlugin().getUBDatabase().addPlayer(fname, freason, fadmin, 0, BanType.KICK.getId());
			}	
		});
		plugin.getLogger().info(bcmsg);
		return true;
	}
}
