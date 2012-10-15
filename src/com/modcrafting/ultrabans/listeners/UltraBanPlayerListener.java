/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.listeners;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import com.modcrafting.ultrabans.Ultrabans;

public class UltraBanPlayerListener implements Listener{
	Ultrabans plugin;
	String spamcheck = null;
	int spamCount = 0;
	FileConfiguration config;
	public UltraBanPlayerListener(Ultrabans ultraBans) {
		plugin = ultraBans;
		config = ultraBans.getConfig();
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerLogin(PlayerLoginEvent event){
		Player player = event.getPlayer();
		if(plugin.bannedPlayers.contains(player.getName().toLowerCase())){
			String reason = plugin.db.getBanReason(player.getName());
			String admin = plugin.db.getAdmin(player.getName());
			String bcmsg = config.getString("Messages.Ban.Login", "%admin% banned you from this server! Reason: %reason%!");
			if(bcmsg.contains(plugin.regexAdmin)) bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
			if(bcmsg.contains(plugin.regexReason)) bcmsg = bcmsg.replaceAll(plugin.regexReason, reason);
			bcmsg=plugin.util.formatMessage(bcmsg);
			event.disallow(PlayerLoginEvent.Result.KICK_BANNED, bcmsg);
		}
		if(plugin.tempBans.get(player.getName().toLowerCase()) != null){
			String reason = plugin.db.getBanReason(player.getName());
			String admin = plugin.db.getAdmin(player.getName());
			long tempTime = plugin.tempBans.get(player.getName().toLowerCase());
			long diff = tempTime - (System.currentTimeMillis()/1000);
			if(diff <= 0){
				String ip = plugin.db.getAddress(player.getName());
				if(plugin.bannedIPs.contains(ip)){
					plugin.bannedIPs.remove(ip);
					Bukkit.unbanIP(ip);
				}
				plugin.tempBans.remove(player.getName().toLowerCase());
				plugin.bannedPlayers.remove(player.getName().toLowerCase());
				plugin.db.removeFromBanlist(player.getName().toLowerCase());
				admin = plugin.admin;
				plugin.db.addPlayer(player.getName(), "Untempbanned: " + reason, admin, 0, 5);
				return;
			}
			Date date = new Date();
			date.setTime(tempTime*1000);
			String dateStr = date.toString();
			String msgvic = config.getString("Messages.TempBan.Login", "You have been tempbanned by %admin% for %time%. Reason: %reason%!");
			if(msgvic.contains(plugin.regexAdmin)) msgvic = msgvic.replaceAll(plugin.regexAdmin, admin);
			if(msgvic.contains(plugin.regexReason)) msgvic = msgvic.replaceAll(plugin.regexReason, reason);
			if(msgvic.contains("%time%")) msgvic = msgvic.replaceAll("%time%", dateStr.substring(4, 19));
			msgvic=plugin.util.formatMessage(msgvic);
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, msgvic);
			return;
		}
		if(config.getBoolean("Lockdown", false)&&!player.hasPermission("ultraban.override.lockdown")){
			String lockMsgLogin = config.getString("Messages.Lockdown.LoginMsg", "Server is under a lockdown, Try again later!");
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, lockMsgLogin);
			plugin.getLogger().info(player.getName() + " attempted to join during lockdown.");
		}
	}
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(final PlayerJoinEvent event){
		final Player player = event.getPlayer();
		final String ip = player.getAddress().getAddress().getHostAddress();
		plugin.db.setAddress(player.getName().toLowerCase(), ip);
		if(plugin.bannedIPs.contains(ip)){
			event.setJoinMessage(null);
			String adminMsg = config.getString("Messages.IPBan.Login", "Your IP is banned!");
			player.kickPlayer(adminMsg);
		}
		if(!plugin.db.matchAddress(player.getName(), ip)){
			plugin.db.updateAddress(player.getName(), ip);
		}
		if(!player.hasPermission("ultraban.override.dupeip")&&config.getBoolean("Login.DupeCheck.Enable", true)){
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,new Runnable(){
				@Override
				public void run() {
					String ip = plugin.db.getAddress(player.getName());
					List<String> list = plugin.db.listPlayers(ip);
					for(Player admin:plugin.getServer().getOnlinePlayers()){
						if(admin.hasPermission("ultraban.dupeip")){
							if(ip == null){
								admin.sendMessage(ChatColor.RED + "Unable to view ip for " + player.getName() + " !");
								return;
							}
							for(String name:list){
								if(!name.equalsIgnoreCase(player.getName())) admin.sendMessage(ChatColor.GRAY + "Player: " + name + " duplicates player: " + player.getName() + "!");
							}
						}
					}
				}
			},20L);			
		}
		if(config.getBoolean("Login.PingCheck.Enable",true)){
			boolean p=false;
			int ping = ((CraftPlayer) player).getHandle().ping;
			p = checkPlayerPing(player, ping);
			for(Player admin:plugin.getServer().getOnlinePlayers()){
				if(admin.hasPermission("ultraban.ping")){
					if(p){
						admin.sendMessage(ChatColor.GRAY + "Player: " + player.getName() + " was kicked for High Ping!");
					}else{
						admin.sendMessage(ChatColor.GRAY + "Player: " + player.getName() + " Ping: "+String.valueOf(ping)+"ms");						
					}
					
				}
			}
		}
		if(config.getBoolean("Login.ProxyPingBack.Enable",true)){ //TODO UnderConstruction
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
				@Override
				public void run() {
					try {
						int to = config.getInt("Login.ProxyPingBack.Timeout",10000);
						InetAddress tip = InetAddress.getByName(ip);
					 	if(!tip.isReachable(to)){
					 		event.getPlayer().kickPlayer("");
					 	}
					} catch (UnknownHostException e) {
				 		event.getPlayer().kickPlayer("");
					} catch (IOException e) {
				 		event.getPlayer().kickPlayer("");
					}
					
				}
				
			});
		}
	 	if(plugin.tempJail.get(player.getName().toLowerCase()) != null)tempjailCheck(player);
		plugin.getLogger().info("Logged " + player.getName() + " connecting from ip:" + ip);
	}
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
		Player player = event.getPlayer();
		String adminMsg = config.getString("Messages.Mute.Chat", "Your cry falls on deaf ears.");
		if(plugin.jailed.contains(player.getName().toLowerCase())&&config.getBoolean("Jail.Vannila", true)){
			String args[] = event.getMessage().split(" ");
			if(config.getStringList("Jail.AllowedCommands").contains(args[0])) return;
		 	if(plugin.tempJail.get(player.getName().toLowerCase()) != null){
		 		if(tempjailCheck(player)) return;

		 	}
			player.sendMessage(plugin.util.formatMessage(adminMsg));
			event.setCancelled(true);
		 }	
		if(plugin.muted.contains(player.getName().toLowerCase())&&config.getBoolean("Muted.Vannila", true)){
			String args[] = event.getMessage().split(" ");
			if(config.getStringList("Mute.AllowedCommands").contains(args[0])) return;
			player.sendMessage(plugin.util.formatMessage(adminMsg));
			event.setCancelled(true);
		}
			
	}
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerChat(AsyncPlayerChatEvent event){
		Player player = event.getPlayer();
		String message = event.getMessage();
		String adminMsg = config.getString("Messages.Mute.Chat", "Your cry falls on deaf ears.");
		if(plugin.muted.contains(player.getName().toLowerCase())){;
			player.sendMessage(plugin.util.formatMessage(adminMsg));
			event.setCancelled(true);
		}
		if(plugin.jailed.contains(player.getName().toLowerCase())&&config.getBoolean("Jail.Mute",true)){
			if(plugin.tempJail.get(player.getName().toLowerCase())!=null&&tempjailCheck(player)) return;
			player.sendMessage(plugin.util.formatMessage(adminMsg));
			event.setCancelled(true);
		}
		if(config.getBoolean("Chat.IPCheck.Enable", true)){
			ipcheck(player, message, event);
		}
		if(config.getBoolean("Chat.SpamCheck.Enable", true)){
			spamcheck(player, message, event);
		}
		if(config.getBoolean("Chat.SwearCensor.Enable", true)){
			swearcheck(player, message, event);
		}
	}
	private boolean checkPlayerPing(Player player,int ping){
		int pingout =config.getInt("MaxPing",200);
		if(ping>pingout&&!player.hasPermission("ultraban.override.pingcheck")){
			String msgvic = config.getString("Messages.Kick.MsgToVictim", "You have been kicked by %admin%. Reason: %reason%");
			if(msgvic.contains(plugin.regexAdmin)) msgvic = msgvic.replaceAll(plugin.regexAdmin, "Ultrabans");
			if(msgvic.contains(plugin.regexReason)) msgvic = msgvic.replaceAll(plugin.regexReason, "High Ping Rate");
			msgvic=plugin.util.formatMessage(msgvic);
			player.kickPlayer(msgvic);
			return true;
		}
		//pass
		return false;
	}
	private boolean tempjailCheck(Player player){
		long tempTime = plugin.tempJail.get(player.getName().toLowerCase());
		long now = System.currentTimeMillis()/1000;
		long diff = tempTime - now;
		if(diff <= 0){
			plugin.tempJail.remove(player.getName().toLowerCase());
			plugin.jailed.remove(player.getName().toLowerCase());
			plugin.db.removeFromJaillist(player.getName().toLowerCase());
			plugin.db.addPlayer(player.getName(), "Released From Jail", "Served Time", 0, 8);
			Location stlp = plugin.jail.getJail("release");
			player.teleport(stlp);
			String bcmsg = config.getString("Messages.Pardon.Msg","%victim% was released from jail by %admin%!");
			if(bcmsg.contains(plugin.regexAdmin)) bcmsg = bcmsg.replaceAll(plugin.regexAdmin, plugin.admin);
			if(bcmsg.contains(plugin.regexVictim)) bcmsg = bcmsg.replaceAll(plugin.regexVictim, player.getName());
			bcmsg=plugin.util.formatMessage(bcmsg);
			player.sendMessage(bcmsg);
			return true;
		}
		Date date = new Date();
		date.setTime(tempTime*1000);
		String dateStr = date.toString();
		String reason = plugin.db.getjailReason(player.getName());
		player.sendMessage(ChatColor.GRAY + "You've been tempjailed for " + reason);
		player.sendMessage(ChatColor.GRAY + "Remaining: " + ChatColor.RED + dateStr);
		return false;
	}
	private void ipcheck(Player player, String message,AsyncPlayerChatEvent event){
		String mes = new String(message);
		String[] content = {"\\,","\\-","\\_","\\="};
		for (int ii=0;ii<content.length;ii++){
			if(mes.contains(content[ii])) mes = mes.replaceAll(content[ii], "."); 									
		}
		String[] ipcheck = mes.split(" ");
		String mode = config.getString("Chat.IPCheck.Blocking");
		if(mode == null) mode = "";
		boolean valid = false;
		for (int i=0; i<ipcheck.length; i++){
			if(plugin.util.validIP(ipcheck[i].trim())){
				if(mode.equalsIgnoreCase("%scramble%")){
					event.setMessage(mes.replaceAll(ipcheck[i].trim(), ChatColor.MAGIC + "AAAAA"));
				 }else if(mode.equalsIgnoreCase("%replace%")){
					event.setMessage(mes.replaceAll(ipcheck[i].trim(), plugin.getServer().getIp()));
				 }else{
					 event.setMessage(mes.replaceAll(ipcheck[i].trim(), mode));
				 }
				 valid = true;
			}
		}
		String result = config.getString("Chat.IPCheck.Result","ban");
		String reason = config.getString("Chat.IPCheck.Reason","Advertising");
		if(valid && result != null){
			if(result.equalsIgnoreCase("ban") || result.equalsIgnoreCase("kick") || result.equalsIgnoreCase("ipban") || result.equalsIgnoreCase("jail") || result.equalsIgnoreCase("warn")){
				String fakecmd = null;
				if(config.getBoolean("Chat.IPCheck.Silent", false)){
					fakecmd = result + " " + player.getName() + " " + "-s" + " " + reason;
				}else{
					fakecmd = result + " " + player.getName() + " " + reason;
				}
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), fakecmd);
			}
		}
	}
	private void spamcheck(Player player, String message, AsyncPlayerChatEvent event){
		 String mes = new String(message);
		 if(!mes.equalsIgnoreCase(spamcheck)){
			 spamcheck = event.getMessage();
			 spamCount = 0;
		 }else{
			 event.setCancelled(true);
			 spamCount++;
		 }
		String result = config.getString("Chat.SpamCheck.Result","kick");
		String reason = config.getString("Chat.SpamCheck.Reason","Spam");
		if(config.getInt("Chat.SpamCheck.Counter") < spamCount  && result != null){
			if(result.equalsIgnoreCase("ban") || result.equalsIgnoreCase("kick") || result.equalsIgnoreCase("ipban") || result.equalsIgnoreCase("jail") || result.equalsIgnoreCase("warn")){
				String fakecmd = null;
				if(config.getBoolean("Chat.SpamCheck.Silent", false)){
					fakecmd = result + " " + player.getName() + " " + "-s" + " " + reason;
				}else{
					fakecmd = result + " " + player.getName() + " " + reason;
				}
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), fakecmd);
			}
		}
		
	}
	private void swearcheck(Player player, String message, AsyncPlayerChatEvent event){
		 String mes = new String(message);
		 String[] string = config.getString("Chat.SwearCensor.Words").split(" ");
		 String mode = config.getString("Chat.SwearCensor.Blocking");
		 if(mode == null) mode = "";
		 boolean valid = false;
		 for (int i=0; i<string.length; i++){
			 if(mes.contains(string[i].trim())){
				 if(mode.equalsIgnoreCase("%scramble%")){
					 mes = mes.replaceAll(string[i].trim(), ChatColor.MAGIC + "AAAAA");
				 }else if(mode.equalsIgnoreCase("%replace%")){
					 mes = mes.replaceAll(string[i].trim(), plugin.getServer().getIp());
				 }else{
					 mes = mes.replaceAll(string[i].trim(), mode);
				 }
				 valid = true;
			 }
		 }
		 event.setMessage(mes);
		 String result = config.getString("Chat.SwearCensor.Result","mute");
		 String reason = config.getString("Chat.SwearCensor.Reason","Language");
		 if(valid && result != null){
			 if(result.equalsIgnoreCase("ban") || result.equalsIgnoreCase("kick") || result.equalsIgnoreCase("ipban") || result.equalsIgnoreCase("jail") || result.equalsIgnoreCase("warn") ||result.equalsIgnoreCase("mute")){
				 String fakecmd = null;
				 if(config.getBoolean("Chat.SwearCensor.Silent", false)){
					 fakecmd = result + " " + player.getName() + " " + "-s" + " " + reason;
				 }else{
					 fakecmd = result + " " + player.getName() + " " + reason;
				 }
				 plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), fakecmd);
			 }
		 }		
	}
}
