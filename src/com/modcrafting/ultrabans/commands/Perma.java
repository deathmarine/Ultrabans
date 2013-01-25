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

public class Perma implements CommandExecutor{
	Ultrabans plugin;
	public Perma(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
		p = Formatting.expandName(p);
		Player victim = plugin.getServer().getPlayer(p);
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
		if(victim == null){
			victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
			if(victim == null){
				String smvic = config.getString("Messages.PermaBan.Online","%victim% not found.");
				if(smvic.contains(Ultrabans.VICTIM))smvic=smvic.replaceAll(Ultrabans.VICTIM, p);
				smvic=Formatting.formatMessage(smvic);
				sender.sendMessage(ChatColor.GRAY + smvic);
				return true;
			}
		}
		if(victim.getName().equalsIgnoreCase(admin)){
			String bcmsg = config.getString("Messages.PermaBan.Emo","You cannot permaban yourself!");
			bcmsg = Formatting.formatMessage(bcmsg);
			sender.sendMessage(bcmsg);
			return true;
		}
		if(victim.hasPermission( "ultraban.override.permaban")&&!admin.equalsIgnoreCase(Ultrabans.DEFAULT_ADMIN)){
			String bcmsg = config.getString("Messages.PermaBan.Denied","Your permaban has been denied!");
			bcmsg = Formatting.formatMessage(bcmsg);
			sender.sendMessage(bcmsg);
			return true;
		}
		if(plugin.bannedPlayers.contains(victim.getName().toLowerCase())){
			String failed = config.getString("Messages.PermaBan.Failed", "%victim% is already banned.");
			if(failed.contains(Ultrabans.VICTIM)) failed = failed.replaceAll(Ultrabans.VICTIM, victim.getName());
			failed = Formatting.formatMessage(failed);
			sender.sendMessage(failed);
			return true;
		}
		String msgvic = config.getString("Messages.PermaBan.MsgToVictim", "You have been permabanned by %admin%. Reason: %reason%");
		if(msgvic.contains(Ultrabans.ADMIN)) msgvic = msgvic.replaceAll(Ultrabans.ADMIN, admin);
		if(msgvic.contains(Ultrabans.REASON)) msgvic = msgvic.replaceAll(Ultrabans.REASON, reason);
		msgvic=Formatting.formatMessage(msgvic);
		victim.kickPlayer(msgvic);
		
		String bcmsg = config.getString("Messages.PermaBan.MsgToBroadcast","%victim% was permabanned by %admin%. Reason: %reason%!");
		if(bcmsg.contains(Ultrabans.ADMIN)) bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
		if(bcmsg.contains(Ultrabans.REASON)) bcmsg = bcmsg.replaceAll(Ultrabans.REASON, reason);
		if(bcmsg.contains(Ultrabans.VICTIM)) bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, victim.getName());
		bcmsg=Formatting.formatMessage(bcmsg);
		if(broadcast){
			plugin.getServer().broadcastMessage(bcmsg);
		}else{
			sender.sendMessage(ChatColor.ITALIC + "Silent: " + bcmsg);
		}
		if(config.getBoolean("CleanOnBan",false)) plugin.data.deletePlyrdat(victim.getName());
		if(config.getBoolean("ClearWarnOnBan",false)) plugin.getUBDatabase().clearWarns(victim.getName());
		plugin.bannedPlayers.add(victim.getName().toLowerCase());
		final String fname = victim.getName();
		final String freason = reason;
		final String fadmin = admin;
		Bukkit.getScheduler().runTaskAsynchronously(Ultrabans.getPlugin(),new Runnable(){
			@Override
			public void run() {
				Ultrabans.getPlugin().getUBDatabase().addPlayer(fname, freason, fadmin, 0, BanType.PERMA.getId());
			}	
		});
		if(plugin.getLog())
			plugin.getLogger().info(bcmsg);
		return true;
	}
}
