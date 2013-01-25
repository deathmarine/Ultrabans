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

public class Ipban implements CommandExecutor{
	Ultrabans plugin;
	public Ipban(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(final CommandSender sender, Command command, String label, final String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+Ultrabans.DEFAULT_DENY_MESSAGE);
			return true;
		}
		if (args.length < 1) return false;

		Bukkit.getScheduler().scheduleSyncDelayedTask(Ultrabans.getPlugin(),new Runnable(){
			@Override
			public void run() {
				boolean broadcast = true;
		    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
				Player player = null;
				String admin = Ultrabans.DEFAULT_ADMIN;
				String reason = Ultrabans.DEFAULT_REASON;
				if (sender instanceof Player){
					player = (Player)sender;
					admin = player.getName();
				}
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
				//IPv4 Check
				if(Formatting.validIP(p)){
					plugin.bannedIPs.add(p);
					String pname = plugin.getUBDatabase().getName(p);
					if (pname != null){
						plugin.getUBDatabase().addPlayer(pname, reason, admin, 0, 1);
						plugin.bannedPlayers.add(pname);
					}else{
						plugin.getUBDatabase().setAddress(p, p);
						plugin.getUBDatabase().addPlayer(p, reason, admin, 0, 1);
					}
					String bcmsg = config.getString("Messages.IPBan.MsgToBroadcast","%victim% was ipbanned by %admin%. Reason: %reason%!");
					if(bcmsg.contains(Ultrabans.ADMIN)) bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
					if(bcmsg.contains(Ultrabans.REASON)) bcmsg = bcmsg.replaceAll(Ultrabans.REASON, reason);
					if(bcmsg.contains(Ultrabans.VICTIM)) bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, p);
					bcmsg=Formatting.formatMessage(bcmsg);
					if(broadcast){
						plugin.getServer().broadcastMessage(bcmsg);
					}else{
						sender.sendMessage(ChatColor.ITALIC + "Silent: " + bcmsg);
					}
					plugin.getLogger().info(bcmsg);
					return;
				}
				p = Formatting.expandName(p);
				Player victim = plugin.getServer().getPlayer(p); 
				String victimip = null;
				if(victim == null){
					victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
					if(victim != null){
						if(victim.hasPermission("ultraban.override.ipban")&&!admin.equalsIgnoreCase(Ultrabans.DEFAULT_ADMIN)){
							String bcmsg = config.getString("Messages.IPBan.Denied","Your ipban has been denied!");
							bcmsg = Formatting.formatMessage(bcmsg);
							sender.sendMessage(bcmsg);
							return;
						}
					}else{
						victimip = plugin.getUBDatabase().getAddress(p);
						if(victimip == null){
							String failed = config.getString("Messages.IPBan.IPNotFound", "IP address not found. Processed as a normal ban for %victim%!");
							if(failed.contains(Ultrabans.VICTIM)) failed = failed.replaceAll(Ultrabans.VICTIM, p);
							failed = Formatting.formatMessage(failed);
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
							return;
						}
					}
				}else{
					if(victim.getName().equalsIgnoreCase(admin)){
						String bcmsg = config.getString("Messages.IPBan.Emo","You cannot ipban yourself!");
						bcmsg = Formatting.formatMessage(bcmsg);
						sender.sendMessage(bcmsg);
						return;
					}
					if(victim.hasPermission("ultraban.override.ipban")&&!admin.equalsIgnoreCase(Ultrabans.DEFAULT_ADMIN)){
						String bcmsg = config.getString("Messages.IPBan.Denied","Your ipban has been denied!");
						bcmsg = Formatting.formatMessage(bcmsg);
						sender.sendMessage(bcmsg);
						return;
					}	
					victimip = plugin.getUBDatabase().getAddress(victim.getName().toLowerCase());
				}
				if(plugin.bannedIPs.contains(victimip)){
					String failed = config.getString("Messages.IPBan.Failed", "%victim% is already ipbanned.");
					if(failed.contains(Ultrabans.VICTIM)) failed = failed.replaceAll(Ultrabans.VICTIM, p);
					failed = Formatting.formatMessage(failed);
					sender.sendMessage(failed);
					return;
				}
				if(victimip == null){
					String failed = config.getString("Messages.IPBan.IPNotFound", "IP address not found. Processed as a normal ban for %victim%!");
					if(failed.contains(Ultrabans.VICTIM)) failed = failed.replaceAll(Ultrabans.VICTIM, p);
					failed = Formatting.formatMessage(failed);
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
					return;
				}
				String msgvic = config.getString("Messages.IPBan.MsgToVictim", "You have been ipbanned by %admin%. Reason: %reason%!");
				if(msgvic.contains(Ultrabans.ADMIN)) msgvic = msgvic.replaceAll(Ultrabans.ADMIN, admin);
				if(msgvic.contains(Ultrabans.REASON)) msgvic = msgvic.replaceAll(Ultrabans.REASON, reason);
				msgvic=Formatting.formatMessage(msgvic);
				if(victim != null && victim.isOnline()) victim.kickPlayer(msgvic);
				
				String bcmsg = config.getString("Messages.IPBan.MsgToBroadcast","%victim% was ipbanned by %admin%. Reason: %reason%!");
				if(bcmsg.contains(Ultrabans.ADMIN)) bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
				if(bcmsg.contains(Ultrabans.REASON)) bcmsg = bcmsg.replaceAll(Ultrabans.REASON, reason);
				if(bcmsg.contains(Ultrabans.VICTIM)) bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, p);
				bcmsg=Formatting.formatMessage(bcmsg);
				if(broadcast){
					plugin.getServer().broadcastMessage(bcmsg);
				}else{
					sender.sendMessage(ChatColor.ITALIC + "Silent: " + bcmsg);
				}
				plugin.bannedPlayers.add(p.toLowerCase());
				plugin.bannedIPs.add(victimip);
				plugin.getUBDatabase().addPlayer(p.toLowerCase(), reason, admin, 0, BanType.IPBAN.getId());
				plugin.getLogger().info(bcmsg);
			}
		});
		return true;
	}	
	
	
}
