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

public class Kick implements CommandExecutor{
	Ultrabans plugin;
	public Kick(Ultrabans ultraBan) {
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
		if(args[0].equals("*")&&sender.hasPermission("ultrabans.kick.all")){
				String adminMsg = config.getString("Messages.Kick.MsgToAll", "Everyone has been kicked by %admin%. Reason: %reason%");
				if(adminMsg.contains(plugin.regexAdmin)) adminMsg = adminMsg.replaceAll(plugin.regexAdmin, admin);
				if(adminMsg.contains(plugin.regexReason)) adminMsg = adminMsg.replaceAll(plugin.regexReason, reason);
				adminMsg=plugin.util.formatMessage(adminMsg);
				for (Player players:plugin.getServer().getOnlinePlayers()){
					if (!players.hasPermission("ultraban.override.kick.all")){
						players.kickPlayer(adminMsg);
					}
				}
				plugin.getServer().broadcastMessage(adminMsg);
				plugin.getLogger().info(adminMsg);
			
			return true;
		}
		p = plugin.util.expandName(p);
		Player victim = plugin.getServer().getPlayer(p);
		if(victim == null){
			String smvic = config.getString("Messages.Kick.Online","%victim% must be online.");
			if(smvic.contains(plugin.regexVictim))smvic=smvic.replaceAll(plugin.regexVictim, p);
			smvic=plugin.util.formatMessage(smvic);
			sender.sendMessage(ChatColor.GRAY + smvic);
			return true;
		}
		if(victim.getName().equalsIgnoreCase(admin)){
			String bcmsg = config.getString("Messages.Kick.Emo","You cannot kick yourself!");
			bcmsg = plugin.util.formatMessage(bcmsg);
			sender.sendMessage(bcmsg);
			return true;
		}
		if(victim.hasPermission( "ultraban.override.kick")&&!admin.equalsIgnoreCase(plugin.admin)){
			String bcmsg = config.getString("Messages.Kick.Denied","Your kick has been denied!");
			bcmsg = plugin.util.formatMessage(bcmsg);
			sender.sendMessage(bcmsg);
			return true;
		}
		String msgvic = config.getString("Messages.Kick.MsgToVictim", "You have been kicked by %admin%. Reason: %reason%");
		if(msgvic.contains(plugin.regexAdmin)) msgvic = msgvic.replaceAll(plugin.regexAdmin, admin);
		if(msgvic.contains(plugin.regexReason)) msgvic = msgvic.replaceAll(plugin.regexReason, reason);
		msgvic=plugin.util.formatMessage(msgvic);
		victim.kickPlayer(msgvic);
		
		String bcmsg = config.getString("Messages.Kick.MsgToBroadcast","%victim% was kicked by %admin%. Reason: %reason%!");
		if(bcmsg.contains(plugin.regexAdmin)) bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
		if(bcmsg.contains(plugin.regexReason)) bcmsg = bcmsg.replaceAll(plugin.regexReason, reason);
		if(bcmsg.contains(plugin.regexVictim)) bcmsg = bcmsg.replaceAll(plugin.regexVictim, victim.getName());
		bcmsg=plugin.util.formatMessage(bcmsg);
		if(broadcast){
			plugin.getServer().broadcastMessage(bcmsg);
		}else{
			sender.sendMessage(ChatColor.ITALIC + "Silent: " + bcmsg);
		}
		plugin.db.addPlayer(victim.getName(), reason, admin, 0, 3);
		plugin.getLogger().info(bcmsg);
		return true;
	}
}
