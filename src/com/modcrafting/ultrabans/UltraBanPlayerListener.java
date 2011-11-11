
package com.modcrafting.ultrabans;

import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;

public class UltraBanPlayerListener extends PlayerListener {
	UltraBan plugin;
	public UltraBanPlayerListener(UltraBan ultraBans) {
		this.plugin = ultraBans;
	}
	public void onPlayerLogin(PlayerLoginEvent event){
		Player player = event.getPlayer();
		if(plugin.bannedPlayers.contains(player.getName().toLowerCase())){
			System.out.println("banned player joined");
			if(plugin.tempBans.get(player.getName().toLowerCase()) != null){
				long tempTime = plugin.tempBans.get(player.getName().toLowerCase());
				long now = System.currentTimeMillis()/1000;
				long diff = tempTime - now;
				if(diff <= 0){
					plugin.bannedPlayers.remove(player.getName().toLowerCase());
					plugin.tempBans.remove(player.getName().toLowerCase());
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
			String reason = plugin.db.getBanReason(player.getName());
			String adminMsg = "You've been banned you for: " + reason;
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, adminMsg);
		}
	}
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		String ip = player.getAddress().getAddress().getHostAddress();
		plugin.db.setAddress(player.getName().toLowerCase(), ip);
		System.out.println(player.getName() + "connect from ip " + ip);
		//Personalized copy
		//player.sendMessage(ChatColor.GRAY + "Server is secured by" + ChatColor.GOLD + " Death's UltraBans");
		if(plugin.bannedIPs.contains(ip)){
			System.out.println("ip is banned");
			event.setJoinMessage(null);
			String adminMsg = "You're ip address is banned and has been logged!";
			player.kickPlayer(adminMsg);
		}
		if(!plugin.db.matchAddress(player.getName(), ip)){
			plugin.db.updateAddress(player.getName(), ip);
		}
	}
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
		Player player = event.getPlayer();
			if(plugin.jailed.contains(player.getName().toLowerCase())){
				player.sendMessage(ChatColor.GRAY + " You cannot use commands while Jailed!");
				event.setCancelled(true);
			 }
	}
	public void onPlayerChat(PlayerChatEvent event){
		 Player player = event.getPlayer();
		 	if(plugin.jailed.contains(player.getName().toLowerCase())){
		 		player.sendMessage(ChatColor.GRAY + " Your cry falls on deaf ears.");
		 		event.setCancelled(true);
		 	}
	}
		 
}
