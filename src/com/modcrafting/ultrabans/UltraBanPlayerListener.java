
package com.modcrafting.ultrabans;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;

public class UltraBanPlayerListener extends PlayerListener {
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	public UltraBanPlayerListener(UltraBan ultraBans) {
		this.plugin = ultraBans;
	}
	public void onPlayerLogin(PlayerLoginEvent event){
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		Player player = event.getPlayer();
		if(plugin.bannedPlayers.contains(player.getName().toLowerCase())){
			System.out.println("banned player joined");
			String reason = plugin.db.getBanReason(player.getName().toLowerCase());
			String adminMsg = "You've been banned for: " + reason;
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, adminMsg);
		}
		if(plugin.tempBans.get(player.getName().toLowerCase()) != null){
			long tempTime = plugin.tempBans.get(player.getName().toLowerCase());
			long now = System.currentTimeMillis()/1000;
			long diff = tempTime - now;
			if(diff <= 0){
				plugin.tempBans.remove(player.getName().toLowerCase());
				plugin.bannedPlayers.remove(player.getName().toLowerCase());
				plugin.db.removeFromBanlist(player.getName().toLowerCase());
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
		boolean lock = config.getBoolean("lockdown", false);
		if(lock){
			boolean auth = false;
			String lockMsgLogin = config.getString("messages.lockMsgLogin", "Server is under a lockdown, Try again later!");
			if (plugin.setupPermissions()){
				if (plugin.permission.has(player, "ultraban.lockdown.override")) auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no vault doesn't take or node
			}
			
			if (!auth) event.disallow(PlayerLoginEvent.Result.KICK_OTHER, lockMsgLogin);
			UltraBan.log.log(Level.INFO,"[UltraBan] " + player.getName() + " attempted to join during lockdown.");
		}
	}
	public void onPlayerJoin(PlayerJoinEvent event){
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		Player player = event.getPlayer();
		String ip = player.getAddress().getAddress().getHostAddress();
		String dubName = plugin.db.getName(ip);
		if (dubName != player.getName()){
			if (plugin.setupPermissions()){
				Player[] parg = plugin.getServer().getOnlinePlayers();
				for (int i=0; i<parg.length; i++){
					if (plugin.permission.has(parg[i], "ultraban.info")){
						parg[i].sendMessage(ChatColor.RED + "ATTN: " + dubName + " and " + player.getName() + " share IP: " + ip);
					}
				}
			}
		}
		plugin.db.setAddress(player.getName().toLowerCase(), ip);
		System.out.println("[UltraBan] Logged " + player.getName() + " connecting from ip:" + ip);
		
		
		//Personalized copy
		//player.sendMessage(ChatColor.GRAY + "Server is secured by" + ChatColor.GOLD + " Death's UltraBans");
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
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		Player player = event.getPlayer();
			if(plugin.jailed.contains(player.getName().toLowerCase())){
				String adminMsg = config.getString("messages.jailCmdMsg", "You cannot use commands while Jailed!");
				player.sendMessage(ChatColor.GRAY + adminMsg);
				event.setCancelled(true);
			 }
	}
	public void onPlayerChat(PlayerChatEvent event){
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		 Player player = event.getPlayer();
		 	if(plugin.muted.contains(player.getName().toLowerCase())){
				String adminMsg = config.getString("messages.muteChatMsg", "Your cry falls on deaf ears.");
		 		player.sendMessage(ChatColor.GRAY + adminMsg);
		 		event.setCancelled(true);
		 	}
		 	if(plugin.jailed.contains(player.getName().toLowerCase())){
				String adminMsg = config.getString("messages.jailChatMsg", "Your cry falls on deaf ears.");
		 		player.sendMessage(ChatColor.GRAY + adminMsg);
		 		event.setCancelled(true);
		 	}
	}
		 
}
