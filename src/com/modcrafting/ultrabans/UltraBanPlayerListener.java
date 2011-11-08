
package com.modcrafting.ultrabans;

import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.util.Util;

public class UltraBanPlayerListener extends PlayerListener {
	UltraBan plugin;

	public UltraBanPlayerListener(UltraBan instance) {
		this.plugin = instance;
	}
	@SuppressWarnings("deprecation")
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
				plugin.properties.load();
				String adminMsg = Util.formatMessage(plugin.properties.getNode("messages").getString("LoginTempban"));
				adminMsg = adminMsg.replaceAll("%time%", date.toString());
				adminMsg = adminMsg.replaceAll("%reason%", plugin.db.getBanReason(player.getName()));
				event.disallow(PlayerLoginEvent.Result.KICK_OTHER, adminMsg);
				return;
			}
			plugin.properties.load();
			String adminMsg = Util.formatMessage(plugin.properties.getNode("messages").getString("LoginBan"));
			adminMsg = adminMsg.replaceAll("%reason%", plugin.db.getBanReason(player.getName()));
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, adminMsg);
		}
	}
	@SuppressWarnings("deprecation")
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		String ip = player.getAddress().getAddress().getHostAddress();
		System.out.println("connect from ip " + ip);
		//Personalized copy
		player.sendMessage(ChatColor.GRAY + "Server is secured by" + ChatColor.GOLD + " Death's UltraBans");
		if(plugin.bannedIPs.contains(ip)){
			System.out.println("ip is banned");
			event.setJoinMessage(null);
			plugin.properties.load();
			String adminMsg = Util.formatMessage(plugin.properties.getNode("messages").getString("LoginIPBan"));
			player.kickPlayer(adminMsg);
		}
		if(!plugin.db.matchAddress(player.getName(), ip)){
			plugin.db.updateAddress(player.getName(), ip);
		}
	}
}
