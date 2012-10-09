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

import com.modcrafting.ultrabans.Ultrabans;

public class Jail implements CommandExecutor{
	Ultrabans plugin;
    public Jail(Ultrabans ultraBan) {
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
		if(args[0].equalsIgnoreCase("setjail")){
			plugin.jail.setJail(player.getLocation(), "jail");
			String msg = config.getString("Messages.Jail.SetJail","Jail has been set!");
			msg=plugin.util.formatMessage(msg);
			sender.sendMessage(ChatColor.GRAY + msg);
			return true;
		}
		if(args[0].equalsIgnoreCase("setrelease")){
			plugin.jail.setJail(player.getLocation(), "release");
			String msg = config.getString("Messages.Jail.SetRelease","Release has been set!");
			msg=plugin.util.formatMessage(msg);
			sender.sendMessage(ChatColor.GRAY + msg);
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
					admin = plugin.admin;
					reason = plugin.util.combineSplit(2, args, " ");
				}else{
				reason = plugin.util.combineSplit(1, args, " ");
				}
			}
		}
		Player victim = plugin.getServer().getPlayer(p);
		if(victim == null){
			if(plugin.jailed.contains(p)){
				String msg = config.getString("Messages.Jail.Failed","%victim% is already in jail.");
				if(msg.contains(plugin.regexVictim)) msg = msg.replaceAll(plugin.regexVictim, p);
				msg=plugin.util.formatMessage(msg);
				sender.sendMessage(ChatColor.GRAY+msg);
				return true;
			}
			String msg = config.getString("Messages.Jail.Online","%victim% must be online to be jailed.");
			if(msg.contains(plugin.regexVictim)) msg = msg.replaceAll(plugin.regexVictim, p);
			msg=plugin.util.formatMessage(msg);
			sender.sendMessage(ChatColor.GRAY+msg);
			return true;
		}else{
			if(victim.getName().equalsIgnoreCase(admin)){
				String bcmsg = config.getString("Messages.Jail.Emo","You cannot jail yourself!");
				bcmsg = plugin.util.formatMessage(bcmsg);
				sender.sendMessage(bcmsg);
				return true;
			}
			if(victim.hasPermission( "ultraban.override.jail")){
				String bcmsg = config.getString("Messages.Jail.Denied","Your jail attempt has been denied!");
				bcmsg = plugin.util.formatMessage(bcmsg);
				sender.sendMessage(bcmsg);
				return true;
			}
			if(plugin.jailed.contains(victim.getName().toLowerCase())){
				String msg = config.getString("Messages.Jail.Failed","%victim% is already in jail.");
				if(msg.contains(plugin.regexVictim)) msg = msg.replaceAll(plugin.regexVictim, victim.getName());
				msg=plugin.util.formatMessage(msg);
				sender.sendMessage(ChatColor.GRAY+msg);
				return true;
			}
			String msgvic = config.getString("Messages.Jail.MsgToVictim", "You have been jailed by %admin%. Reason: %reason%");
			if(msgvic.contains(plugin.regexAdmin)) msgvic = msgvic.replaceAll(plugin.regexAdmin, admin);
			if(msgvic.contains(plugin.regexReason)) msgvic = msgvic.replaceAll(plugin.regexReason, reason);
			msgvic=plugin.util.formatMessage(msgvic);
			victim.sendMessage(msgvic);
			
			String bcmsg = config.getString("Messages.Jail.MsgToBroadcast","%victim% was jailed by %admin%. Reason: %reason%!");
			if(bcmsg.contains(plugin.regexAdmin)) bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
			if(bcmsg.contains(plugin.regexReason)) bcmsg = bcmsg.replaceAll(plugin.regexReason, reason);
			if(bcmsg.contains(plugin.regexVictim)) bcmsg = bcmsg.replaceAll(plugin.regexVictim, victim.getName());
			bcmsg=plugin.util.formatMessage(bcmsg);

			if(broadcast){
				plugin.getServer().broadcastMessage(bcmsg);
			}else{
				sender.sendMessage(ChatColor.ITALIC + "Silent: " + bcmsg);
			}
			plugin.db.addPlayer(p, reason, admin, 0, 6);
			plugin.jailed.add(p.toLowerCase());
			Location stlp = plugin.jail.getJail("jail");
			victim.teleport(stlp);
		}	
		return true;
	}

}

        
