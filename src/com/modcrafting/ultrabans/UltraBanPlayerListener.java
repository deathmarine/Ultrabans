
package com.modcrafting.ultrabans;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class UltraBanPlayerListener implements Listener{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	public UltraBanPlayerListener(UltraBan ultraBans) {
		this.plugin = ultraBans;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event){
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		Player player = event.getPlayer();
		if(plugin.bannedPlayers.contains(player.getName().toLowerCase())){
			System.out.println("banned player joined");
			String reason = plugin.db.getBanReason(player.getName());
			String admin = plugin.db.getAdmin(player.getName());
			String banMsgBroadcast = config.getString("messages.LoginBan", "%admin% banned you from this server! Reason: %reason%!");
			banMsgBroadcast = banMsgBroadcast.replaceAll("%admin%", admin);
			banMsgBroadcast = banMsgBroadcast.replaceAll("%reason%", reason);
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, banMsgBroadcast);
		}
		if(plugin.tempBans.get(player.getName().toLowerCase()) != null){
			long tempTime = plugin.tempBans.get(player.getName().toLowerCase());
			long now = System.currentTimeMillis()/1000;
			long diff = tempTime - now;
			if(diff <= 0){
				String ip = plugin.db.getAddress(player.getName());
				if(plugin.bannedIPs.contains(ip)){
					plugin.bannedIPs.remove(ip);
					Bukkit.unbanIP(ip);
					System.out.println("Also removed the IP ban!");
				}
				plugin.tempBans.remove(player.getName().toLowerCase());
				plugin.bannedPlayers.remove(player.getName().toLowerCase());
				plugin.db.removeFromBanlist(player.getName().toLowerCase());
				String admin = config.getString("defAdminName", "server");
				String reason = plugin.db.getBanReason(player.getName());
				plugin.db.addPlayer(player.getName(), "Untempbanned: " + reason, admin, 0, 5);
				return;
			}
			Date date = new Date();
			date.setTime(tempTime*1000);
			String dateStr = date.toString();
			String reason = plugin.db.getBanReason(player.getName());
			String adminMsg = "You've been tempbanned for " + reason + " Remaining:" + dateStr;
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, adminMsg);
			return;
		}
		if(plugin.tempJail.get(player.getName().toLowerCase()) != null){
			long tempTime = plugin.tempJail.get(player.getName().toLowerCase());
			long now = System.currentTimeMillis()/1000;
			long diff = tempTime - now;
			if(diff <= 0){
				plugin.tempJail.remove(player.getName().toLowerCase());
				plugin.jailed.remove(player.getName().toLowerCase());
				plugin.db.removeFromJaillist(player.getName().toLowerCase());
				plugin.db.addPlayer(player.getName(), "Released From Jail", "Served Time", 0, 8);
				String label = "release";
				Location setlp = new Location(
		                plugin.getServer().getWorld(config.getString(label+".world", plugin.getServer().getWorlds().get(0).getName())),
		                config.getInt(label+".x", 0),
		                config.getInt(label+".y", 0),
		                config.getInt(label+".z", 0));
				player.teleport(setlp);
				player.sendMessage(ChatColor.GREEN + "You've served your time.");
				return;
			}
			return;
		}
		boolean lock = config.getBoolean("lockdown", false);
		if(lock){
			boolean auth = false;
			String lockMsgLogin = config.getString("messages.lockMsgLogin", "Server is under a lockdown, Try again later!");
			if(player.hasPermission("ultraban.override.lockdown") || player.isOp()) auth = true;
			
			if (!auth) event.disallow(PlayerLoginEvent.Result.KICK_OTHER, lockMsgLogin);
			UltraBan.log.log(Level.INFO,"[UltraBan] " + player.getName() + " attempted to join during lockdown.");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event){
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		Player player = event.getPlayer();
		String ip = player.getAddress().getAddress().getHostAddress();
		plugin.db.setAddress(player.getName().toLowerCase(), ip);
		System.out.println("[UltraBan] Logged " + player.getName() + " connecting from ip:" + ip);
		
		
		if(plugin.bannedIPs.contains(ip)){
			System.out.println("[UltraBan] Banned player attempted Login!");
			event.setJoinMessage(null);
			String adminMsg = config.getString("messages.LoginIPBan", "Your IP is banned!");
			player.kickPlayer(adminMsg);
		}
		if(!plugin.db.matchAddress(player.getName(), ip)){
			plugin.db.updateAddress(player.getName(), ip);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		Player player = event.getPlayer();
			if(plugin.jailed.contains(player.getName().toLowerCase())){
			 	if(plugin.tempJail.get(player.getName().toLowerCase()) != null){
					long tempTime = plugin.tempJail.get(player.getName().toLowerCase());
					long now = System.currentTimeMillis()/1000;
					long diff = tempTime - now;
					if(diff <= 0){
						plugin.tempJail.remove(player.getName().toLowerCase());
						plugin.jailed.remove(player.getName().toLowerCase());
						plugin.db.removeFromJaillist(player.getName().toLowerCase());
						String label = "release";
						Location setlp = new Location(
				                plugin.getServer().getWorld(config.getString(label+".world", plugin.getServer().getWorlds().get(0).getName())),
				                config.getInt(label+".x", 0),
				                config.getInt(label+".y", 0),
				                config.getInt(label+".z", 0));
						player.teleport(setlp);
						player.sendMessage(ChatColor.GREEN + "You've served your time.");
						return;
					}
					Date date = new Date();
					date.setTime(tempTime*1000);
					String dateStr = date.toString();
					String reason = plugin.db.getjailReason(player.getName());
					player.sendMessage(ChatColor.GRAY + "You've been tempjailed for " + reason);
					player.sendMessage(ChatColor.GRAY + "Remaining: " + ChatColor.RED + dateStr);
				}
				String adminMsg = config.getString("messages.jailCmdMsg", "You cannot use commands while Jailed!");
				player.sendMessage(ChatColor.GRAY + adminMsg);
				event.setCancelled(true);
			 }
			if(plugin.muted.contains(player.getName().toLowerCase())){
				if(config.getBoolean("muteVanilla", true)){
					String adminMsg = config.getString("messages.muteChatMsg", "Your cry falls on deaf ears.");
		 			player.sendMessage(ChatColor.GRAY + adminMsg);
					event.setCancelled(true);
				}
			}
			
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(PlayerChatEvent event){
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		 Player player = event.getPlayer();
		 	if(plugin.muted.contains(player.getName().toLowerCase())){
				String adminMsg = config.getString("messages.muteChatMsg", "Your cry falls on deaf ears.");
		 		player.sendMessage(ChatColor.GRAY + adminMsg);
		 		event.setCancelled(true);
		 	}
		 	if(plugin.jailed.contains(player.getName().toLowerCase())){
			 	if(plugin.tempJail.get(player.getName().toLowerCase()) != null){
					long tempTime = plugin.tempJail.get(player.getName().toLowerCase());
					long now = System.currentTimeMillis()/1000;
					long diff = tempTime - now;
					if(diff <= 0){
						plugin.tempJail.remove(player.getName().toLowerCase());
						plugin.jailed.remove(player.getName().toLowerCase());
						plugin.db.removeFromJaillist(player.getName().toLowerCase());
						World wtlp = player.getWorld();
						Location tlp = wtlp.getSpawnLocation();
						player.teleport(tlp);
						player.sendMessage(ChatColor.GREEN + "You've served your time.");
						return;
					}
					Date date = new Date();
					date.setTime(tempTime*1000);
					String dateStr = date.toString();
					String reason = plugin.db.getjailReason(player.getName());
					player.sendMessage(ChatColor.GRAY + "You've been tempjailed for " + reason);
					player.sendMessage(ChatColor.GRAY + "Remaining: " + ChatColor.RED + dateStr);
				}
				String adminMsg = config.getString("messages.jailChatMsg", "Your cry falls on deaf ears.");
		 		player.sendMessage(ChatColor.GRAY + adminMsg);
		 		event.setCancelled(true);
		 	}
	}
		 
}
