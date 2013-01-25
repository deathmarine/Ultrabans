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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.Formatting;

public class Unban implements CommandExecutor{
	Ultrabans plugin;
	public Unban(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(final CommandSender sender, final Command command, String label, final String[] args) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Ultrabans.getPlugin(),new Runnable(){
			@Override
			public void run() {
		    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		    	String admin=Ultrabans.DEFAULT_ADMIN;
				Player player = null;
				if (sender instanceof Player){
					player = (Player)sender;
					admin = player.getName();
				}
				if(!sender.hasPermission(command.getPermission())){
					sender.sendMessage(ChatColor.RED+Ultrabans.DEFAULT_DENY_MESSAGE);
					return;
				}
				if (args.length < 1) return;
				String p = args[0];
				OfflinePlayer of = plugin.getServer().getOfflinePlayer(p);
				if(of!=null){
					if(of.isBanned()) of.setBanned(false);
					p=of.getName();
				}
				//unban IPv4
				if(Formatting.validIP(p)){
					plugin.bannedIPs.remove(p);
					String pname = plugin.getUBDatabase().getName(p);
					if (pname != null){
						of = plugin.getServer().getOfflinePlayer(pname);
						p=of.getName();						
						String reason = plugin.getUBDatabase().getBanReason(p);
						plugin.getUBDatabase().removeFromBanlist(p);
						plugin.getUBDatabase().addPlayer(p, "Unbanned: " + reason, admin, 0, 5);
						plugin.getLogger().info(admin + " unbanned player " + p + ".");				
					}else{
						plugin.getUBDatabase().removeFromBanlist(pname);			
					}
					String bcmsg = config.getString("Messages.Unban.MsgToBroadcast", "%victim% was unbanned by %admin%!");
					bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
					bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, p);
					plugin.getServer().broadcastMessage(Formatting.formatMessage(bcmsg));
					return;
				}
				
				if(plugin.getUBDatabase().permaBan(p.toLowerCase())){
					String perma = config.getString("Messages.Unban.PermaBanned", "%victim% was unbanned by %admin%!");
					perma = perma.replaceAll(Ultrabans.VICTIM, p);
					perma = Formatting.formatMessage(perma);
					sender.sendMessage(perma);
					if(plugin.getLog())
						plugin.getLogger().info(perma);
					return;
				}

				if(plugin.bannedPlayers.contains(p.toLowerCase())){
					plugin.bannedPlayers.remove(p.toLowerCase());
					plugin.getUBDatabase().removeFromBanlist(p);
					Bukkit.getOfflinePlayer(p).setBanned(false);
					String ip = plugin.getUBDatabase().getAddress(p);
					if(plugin.bannedIPs.contains(ip)){
						plugin.bannedIPs.remove(ip);
						if(plugin.getLog())
							plugin.getLogger().info("Also removed the IP ban!");
					}
					if(config.getBoolean("UnbansLog.Enable",true)){
						String reason = plugin.getUBDatabase().getBanReason(p);
						if(config.getBoolean("UnbansLog.LogReason",true)&&reason!=null){
							plugin.getUBDatabase().addPlayer(p, "Unbanned: "+reason, admin, 0, 5);
						}else{
							plugin.getUBDatabase().addPlayer(p, "Unbanned", admin, 0, 5);
						}
					}
					plugin.getLogger().info(admin + " unbanned player " + p + ".");
					String bcmsg = config.getString("Messages.Unban.MsgToBroadcast", "%victim% was unbanned by %admin%!");
					bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
					bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, p);
					plugin.getServer().broadcastMessage(Formatting.formatMessage(bcmsg));
					return;
				}else{
					if(plugin.tempBans.containsKey(p.toLowerCase())){
						plugin.tempBans.remove(p.toLowerCase());
						plugin.getUBDatabase().removeFromBanlist(p);
						String ip = plugin.getUBDatabase().getAddress(p);
						if(plugin.bannedIPs.contains(ip)){
							plugin.bannedIPs.remove(ip);
							if(plugin.getLog())
								plugin.getLogger().info("Also removed the IP ban!");
						}
						if(config.getBoolean("UnbansLog.Enable",true)){
							String reason = plugin.getUBDatabase().getBanReason(p);
							if(config.getBoolean("UnbansLog.LogReason",true)&&reason!=null){
								plugin.getUBDatabase().addPlayer(p, "Unbanned: "+reason, admin, 0, 5);
							}else{
								plugin.getUBDatabase().addPlayer(p, "Unbanned", admin, 0, 5);
							}
						}
						String bcmsg = config.getString("Messages.Unban.MsgToBroadcast", "%victim% was unbanned by %admin%!");
						bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
						bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, p);
						bcmsg = Formatting.formatMessage(bcmsg);
						plugin.getServer().broadcastMessage(bcmsg);
						if(plugin.getLog())
							plugin.getLogger().info(bcmsg);
					}else{
						if(of!=null){
							String bcmsg = config.getString("Messages.Unban.MsgToBroadcast", "%victim% was unbanned by %admin%!");
							bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
							bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, p);
							bcmsg = Formatting.formatMessage(bcmsg);
							plugin.getServer().broadcastMessage(bcmsg);
							if(plugin.getLog())
								plugin.getLogger().info(bcmsg);
							return;
						}
						String failed = config.getString("Messages.Unban.Failed", "%victim% is already unbanned!");
						failed = failed.replaceAll(Ultrabans.VICTIM, p);
						failed = Formatting.formatMessage(failed);
						sender.sendMessage(failed);
					}
					return;
				}
			}
		});
		return true;
	}
}
