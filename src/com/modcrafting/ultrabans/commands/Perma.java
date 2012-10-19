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

public class Perma implements CommandExecutor{
	Ultrabans plugin;
	public Perma(Ultrabans ultraBan) {
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
		Player player = null;
		String admin = plugin.admin;
		String reason = plugin.reason;
		if (sender instanceof Player){
			player = (Player)sender;
			admin = player.getName();
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
		if(victim == null){
			victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
			if(victim == null){
				String smvic = config.getString("Messages.PermaBan.Online","%victim% must be online.");
				if(smvic.contains(plugin.regexVictim))smvic=smvic.replaceAll(plugin.regexVictim, p);
				smvic=plugin.util.formatMessage(smvic);
				sender.sendMessage(ChatColor.GRAY + smvic);
				return true;
			}
		}
		if(victim.getName().equalsIgnoreCase(admin)){
			String bcmsg = config.getString("Messages.PermaBan.Emo","You cannot permaban yourself!");
			bcmsg = plugin.util.formatMessage(bcmsg);
			sender.sendMessage(bcmsg);
			return true;
		}
		if(victim.hasPermission( "ultraban.override.permaban")&&!admin.equalsIgnoreCase(plugin.admin)){
			String bcmsg = config.getString("Messages.PermaBan.Denied","Your permaban has been denied!");
			bcmsg = plugin.util.formatMessage(bcmsg);
			sender.sendMessage(bcmsg);
			return true;
		}
		if(plugin.bannedPlayers.contains(victim.getName().toLowerCase())){
			String failed = config.getString("Messages.PermaBan.Failed", "%victim% is already banned.");
			if(failed.contains(plugin.regexVictim)) failed = failed.replaceAll(plugin.regexVictim, victim.getName());
			failed = plugin.util.formatMessage(failed);
			sender.sendMessage(failed);
			return true;
		}
		String msgvic = config.getString("Messages.PermaBan.MsgToVictim", "You have been permabanned by %admin%. Reason: %reason%");
		if(msgvic.contains(plugin.regexAdmin)) msgvic = msgvic.replaceAll(plugin.regexAdmin, admin);
		if(msgvic.contains(plugin.regexReason)) msgvic = msgvic.replaceAll(plugin.regexReason, reason);
		msgvic=plugin.util.formatMessage(msgvic);
		victim.kickPlayer(msgvic);
		
		String bcmsg = config.getString("Messages.PermaBan.MsgToBroadcast","%victim% was permabanned by %admin%. Reason: %reason%!");
		if(bcmsg.contains(plugin.regexAdmin)) bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
		if(bcmsg.contains(plugin.regexReason)) bcmsg = bcmsg.replaceAll(plugin.regexReason, reason);
		if(bcmsg.contains(plugin.regexVictim)) bcmsg = bcmsg.replaceAll(plugin.regexVictim, victim.getName());
		bcmsg=plugin.util.formatMessage(bcmsg);
		if(broadcast){
			plugin.getServer().broadcastMessage(bcmsg);
		}else{
			sender.sendMessage(ChatColor.ITALIC + "Silent: " + bcmsg);
		}
		if(config.getBoolean("CleanOnBan",false)) plugin.data.deletePlyrdat(victim.getName());
		if(config.getBoolean("ClearWarnOnBan",false)) plugin.db.clearWarns(victim.getName());
		plugin.bannedPlayers.add(victim.getName().toLowerCase());
		plugin.db.addPlayer(victim.getName(), reason, admin, 0, 9);
		plugin.getLogger().info(bcmsg);
		return true;
	}
}
