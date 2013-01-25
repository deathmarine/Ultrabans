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
import com.modcrafting.ultrabans.util.Formatting;
public class Tempipban implements CommandExecutor{
	Ultrabans plugin;
	public Tempipban(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(final CommandSender sender, Command command, String label, final String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+Ultrabans.DEFAULT_DENY_MESSAGE);
			return true;
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(Ultrabans.getPlugin(),new Runnable(){
			@Override
			public void run() {
		    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
				boolean broadcast = true;
				Player player = null;
				String admin = Ultrabans.DEFAULT_ADMIN;
				String reason = Ultrabans.DEFAULT_REASON;
				if (sender instanceof Player){
					player = (Player)sender;
					admin = player.getName();
				}
				if (args.length < 3) return;
				String p = args[0];
				p = Formatting.expandName(p);
				Player victim = plugin.getServer().getPlayer(p);
				long tempTime = 0;
				String amt="";
				String mode="";
				if(args.length > 3){
					if(args[1].equalsIgnoreCase("-s")){
						broadcast = false;
						amt=args[2];
						mode=args[3];
						reason = Formatting.combineSplit(4, args, " ");
						tempTime = Formatting.parseTimeSpec(amt,mode);
					}else if(args[1].equalsIgnoreCase("-a")){
						admin = Ultrabans.DEFAULT_ADMIN;
						amt=args[2];
						mode=args[3];
						reason = Formatting.combineSplit(4, args, " ");
						tempTime = Formatting.parseTimeSpec(amt,mode);
					}else{
						amt=args[1];
						mode=args[2];
						tempTime = Formatting.parseTimeSpec(amt,mode);
						reason = Formatting.combineSplit(3, args, " ");
					}
				}
				if(tempTime == 0) return;
				long temp = System.currentTimeMillis()/1000+tempTime;
				
				if(victim != null){
					if(victim.getName().equalsIgnoreCase(admin)){
						String bcmsg = config.getString("Messages.TempIpBan.Emo","You cannot tempipban yourself!");
						bcmsg = Formatting.formatMessage(bcmsg);
						sender.sendMessage(bcmsg);
						return;
					}
					if(victim.hasPermission("ultraban.override.tempipban")&&!admin.equalsIgnoreCase(Ultrabans.DEFAULT_ADMIN)){
						String bcmsg = config.getString("Messages.TempIpBan.Denied","Your tempipban has been denied!");
						bcmsg = Formatting.formatMessage(bcmsg);
						sender.sendMessage(bcmsg);
						return;
					}	
					if(plugin.bannedPlayers.contains(victim.getName().toLowerCase())){
						String failed = config.getString("Messages.TempIpBan.Failed", "%victim% is already tempipbanned!");
						if(failed.contains(Ultrabans.VICTIM)) failed = failed.replaceAll(Ultrabans.VICTIM, p);
						failed = Formatting.formatMessage(failed);
						sender.sendMessage(failed);
						return;
					}
					String offlineip = plugin.getUBDatabase().getAddress(p.toLowerCase());
					if(offlineip != null){
						plugin.bannedIPs.add(offlineip);
					}else{
						String failed = config.getString("Messages.TempIpBan.IPNotFound", "IP address not found. Processed as a normal tempban for %victim%!");
						if(failed.contains(Ultrabans.VICTIM)) failed = failed.replaceAll(Ultrabans.VICTIM, p);
						failed = Formatting.formatMessage(failed);
						sender.sendMessage(failed);
						StringBuilder sb = new StringBuilder();
						sb.append("tempban");
						sb.append(" ");
						sb.append(p);
						sb.append(" ");
						if(!broadcast){
							sb.append("-s");
							sb.append(" ");
						}
						sb.append(amt);
						sb.append(" ");
						sb.append(mode);
						sb.append(" ");
						sb.append(reason);
						if(player != null){
							player.getPlayer().performCommand(sb.toString());
						}else{
							plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), sb.toString());
						}
						return;
					}
					String msgvic = config.getString("Messages.TempIpBan.MsgToVictim", "You have been tempipbanned by %admin% for %amt% %mode%s. Reason: %reason%!");
					if(msgvic.contains(Ultrabans.ADMIN)) msgvic = msgvic.replaceAll(Ultrabans.ADMIN, admin);
					if(msgvic.contains(Ultrabans.REASON)) msgvic = msgvic.replaceAll(Ultrabans.REASON, reason);
					if(msgvic.contains(Ultrabans.AMOUNT)) msgvic = msgvic.replaceAll(Ultrabans.AMOUNT, amt);
					if(msgvic.contains(Ultrabans.MODE)) msgvic = msgvic.replaceAll(Ultrabans.MODE, mode);
					msgvic=Formatting.formatMessage(msgvic);
					victim.kickPlayer(msgvic);
					
					String bcmsg = config.getString("Messages.TempIpBan.MsgToBroadcast","%victim% was tempipbanned by %admin% for %amt% %mode%s. Reason: %reason%!");
					if(bcmsg.contains(Ultrabans.ADMIN)) bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
					if(bcmsg.contains(Ultrabans.REASON)) bcmsg = bcmsg.replaceAll(Ultrabans.REASON, reason);
					if(bcmsg.contains(Ultrabans.VICTIM)) bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, p);
					if(bcmsg.contains(Ultrabans.AMOUNT)) bcmsg = bcmsg.replaceAll(Ultrabans.AMOUNT, amt);
					if(bcmsg.contains(Ultrabans.MODE)) bcmsg = bcmsg.replaceAll(Ultrabans.MODE, mode);
					bcmsg = Formatting.formatMessage(bcmsg);
					if(broadcast){
						plugin.getServer().broadcastMessage(bcmsg);
					}else{
						sender.sendMessage(ChatColor.ITALIC + "Silent: " + bcmsg);
					}
					plugin.tempBans.put(victim.getName().toLowerCase(), temp);
					plugin.getUBDatabase().addPlayer(victim.getName(), reason, admin, temp, 1);
					if(plugin.getLog())
						plugin.getLogger().info(bcmsg);
					return;
				}else{
					victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
					if(victim != null){
						if(victim.hasPermission("ultraban.override.tempipban")&&!admin.equalsIgnoreCase(Ultrabans.DEFAULT_ADMIN)){
							String bcmsg = config.getString("Messages.TempIpBan.Denied","Your tempipban has been denied!");
							bcmsg = Formatting.formatMessage(bcmsg);
							sender.sendMessage(bcmsg);
							return;
						}
					}
					if(plugin.bannedPlayers.contains(p.toLowerCase())){
						String failed = config.getString("Messages.TempIpBan.Failed", "%victim% is already tempipbanned!");
						if(failed.contains(Ultrabans.VICTIM)) failed = failed.replaceAll(Ultrabans.VICTIM, p);
						failed = Formatting.formatMessage(failed);
						sender.sendMessage(failed);
						return;
					}
					String offlineip = plugin.getUBDatabase().getAddress(p.toLowerCase());
					if(offlineip != null){
						plugin.bannedIPs.add(offlineip);
					}
					String bcmsg = config.getString("Messages.TempIpBan.MsgToBroadcast","%victim% was tempipbanned by %admin% for %amt% %mode%s. Reason: %reason%!");
					if(bcmsg.contains(Ultrabans.ADMIN)) bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
					if(bcmsg.contains(Ultrabans.REASON)) bcmsg = bcmsg.replaceAll(Ultrabans.REASON, reason);
					if(bcmsg.contains(Ultrabans.VICTIM)) bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, p);
					if(bcmsg.contains(Ultrabans.AMOUNT)) bcmsg = bcmsg.replaceAll(Ultrabans.AMOUNT, amt);
					if(bcmsg.contains(Ultrabans.MODE)) bcmsg = bcmsg.replaceAll(Ultrabans.MODE, mode);
					bcmsg = Formatting.formatMessage(bcmsg);
					if(broadcast){
						plugin.getServer().broadcastMessage(bcmsg);
					}else{
						sender.sendMessage(ChatColor.ITALIC + "Silent: " + bcmsg);
					}
					plugin.tempBans.put(p.toLowerCase(), temp);
					plugin.getUBDatabase().addPlayer(p, reason, admin, temp, 1);
					if(plugin.getLog())
						plugin.getLogger().info(bcmsg);
					return;
				}
			}
		});
		return true;
	}
}
