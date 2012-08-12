/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class UltraBanPlayerListener implements Listener{
	UltraBan plugin;
	String spamcheck = null;
	int spamCount = 0;
	public UltraBanPlayerListener(UltraBan ultraBans) {
		this.plugin = ultraBans;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerLogin(PlayerLoginEvent event){
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		Player player = event.getPlayer();
		if(plugin.bannedPlayers.contains(player.getName().toLowerCase())){
			System.out.println("banned player joined");
			String reason = plugin.db.getBanReason(player.getName());
			String admin = plugin.db.getAdmin(player.getName());
			String banMsgBroadcast = config.getString("messages.LoginBan", "%admin% banned you from this server! Reason: %reason%!");
			banMsgBroadcast = banMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
			banMsgBroadcast = banMsgBroadcast.replaceAll(plugin.regexReason, reason);
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
			plugin.log.log(Level.INFO,"[UltraBan] " + player.getName() + " attempted to join during lockdown.");
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event){
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		final Player player = event.getPlayer();
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
		if(player.hasPermission("ultraban.override.dupeip")||!config.getBoolean("enableLoginDupeCheck", true)) return;
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,new Runnable(){

			@Override
			public void run() {
				for(Player admin:plugin.getServer().getOnlinePlayers()){
					if(!admin.hasPermission("ultraban.dupeip")) return;
					String ip = plugin.db.getAddress(player.getName());
					if(ip == null){
						admin.sendMessage(ChatColor.RED + "Unable to view ip for " + player.getName() + " !");
						return;
					}
					String sip = null;
					OfflinePlayer[] pl = plugin.getServer().getOfflinePlayers();
					for (int i=0; i<pl.length; i++){
						sip = plugin.db.getAddress(pl[i].getName());
				        if (sip != null && sip.equalsIgnoreCase(ip)){
				        	if (!pl[i].getName().equalsIgnoreCase(player.getName())){
				        		admin.sendMessage(ChatColor.GRAY + "Player: " + pl[i].getName() + " duplicates player: " + player.getName() + "!");
				        	}
				        }
					}
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.LOW)
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
				player.sendMessage(plugin.util.formatMessage(adminMsg));
				event.setCancelled(true);
			 }
			if(plugin.muted.contains(player.getName().toLowerCase())){
				if(config.getBoolean("muteVanilla", true)){
					String adminMsg = config.getString("messages.muteChatMsg", "Your cry falls on deaf ears.");
		 			player.sendMessage(plugin.util.formatMessage(adminMsg));
					event.setCancelled(true);
				}
			}
			
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerChat(AsyncPlayerChatEvent event){
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		 Player player = event.getPlayer();
		 String message = event.getMessage();
		 //Mute Check
		 if(plugin.muted.contains(player.getName().toLowerCase())){
			String adminMsg = config.getString("messages.muteChatMsg", "Your cry falls on deaf ears.");
			player.sendMessage(plugin.util.formatMessage(adminMsg));
			event.setCancelled(true);
		 }
		 
		 //Jail Check
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
			player.sendMessage(plugin.util.formatMessage(adminMsg));
			event.setCancelled(true);
		}
		 //Block IP
		 if(config.getBoolean("Chat.IPCheck.Enable", true)){
			//127.0.0.1
			String mes = message;
			String[] content = {",","-","_","+","="};
			for (int ii=0;ii<content.length;ii++){
				if(message.contains(content[ii])) mes = message.replaceAll(content[ii], "."); 									
			}
			String[] ipcheck = mes.split(" ");
			String mode = config.getString("Chat.IPCheck.Blocking");
			if(mode == null) mode = "";
			boolean valid = false;
			for (int i=0; i<ipcheck.length; i++){
				if(plugin.util.validIP(ipcheck[i].trim())){
					if(mode.equalsIgnoreCase("%scramble%")){
						event.setMessage(message.replaceAll(ipcheck[i].trim(), ChatColor.MAGIC + "AAAAA"));
					 }else if(mode.equalsIgnoreCase("%replace%")){
						event.setMessage(message.replaceAll(ipcheck[i].trim(), plugin.getServer().getIp()));
					 }else{
						 event.setMessage(message.replaceAll(ipcheck[i].trim(), mode));
					 }
					 valid = true;
				}
			}
			String result = config.getString("Chat.IPCheck.Result");
			if(valid && result != null){
				if(result.equalsIgnoreCase("ban") || result.equalsIgnoreCase("kick") || result.equalsIgnoreCase("ipban") || result.equalsIgnoreCase("jail") || result.equalsIgnoreCase("warn")){
					String fakecmd = null;
					if(config.getBoolean("Chat.IPCheck.Silent", false)){
						fakecmd = result + " " + player.getName() + " " + "-s" + " " + " Ultrabans AutoMated: Advertising";
					}else{
						fakecmd = result + " " + player.getName() + " " + " Ultrabans AutoMated: Advertising";
					}
					plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), fakecmd);
				}
			}
		 }
		 
		 //Block Spam
		 if(config.getBoolean("Chat.SpamCheck.Enable", true)){
			 if(!message.equalsIgnoreCase(spamcheck)){
				 spamcheck = event.getMessage();
				 spamCount = 0;
			 }else{
				 event.setCancelled(true);
				 spamCount++;
			 }
			String result = config.getString("Chat.SpamCheck.Result");
			if(config.getInt("Chat.SpamCheck.Counter") < spamCount  && result != null){
				if(result.equalsIgnoreCase("ban") || result.equalsIgnoreCase("kick") || result.equalsIgnoreCase("ipban") || result.equalsIgnoreCase("jail") || result.equalsIgnoreCase("warn")){
					String fakecmd = null;
					if(config.getBoolean("Chat.SpamCheck.Silent", false)){
						fakecmd = result + " " + player.getName() + " " + "-s" + " " + " Ultrabans AutoMated: Spam";
					}else{
						fakecmd = result + " " + player.getName() + " " + " Ultrabans AutoMated: Spam";
					}
					plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), fakecmd);
				}
			}
		 }
		 
		 //Block Swearing
		 if(config.getBoolean("Chat.SwearCensor.Enable", true)){
			String[] string = config.getString("Chat.SwearCensor.Words").split(" ");
			String mode = config.getString("Chat.SwearCensor.Blocking");
			if(mode == null) mode = "";
			boolean valid = false;
			for (int i=0; i<string.length; i++){
				if(message.contains(string[i].trim())){
					 if(mode.equalsIgnoreCase("%scramble%")){
						message = message.replaceAll(string[i].trim(), ChatColor.MAGIC + "AAAAA");
					 }else if(mode.equalsIgnoreCase("%replace%")){
						 message = message.replaceAll(string[i].trim(), plugin.getServer().getIp());
					 }else{
						 message = message.replaceAll(string[i].trim(), mode);
					 }
					 valid = true;
				}
			}
			event.setMessage(message);
			String result = config.getString("Chat.SwearCensor.Result");
			if(valid && result != null){
				if(result.equalsIgnoreCase("ban") || result.equalsIgnoreCase("kick") || result.equalsIgnoreCase("ipban") || result.equalsIgnoreCase("jail") || result.equalsIgnoreCase("warn")){
					String fakecmd = null;
					if(config.getBoolean("Chat.SwearCensor.Silent", false)){
						fakecmd = result + " " + player.getName() + " " + "-s" + " " + " Ultrabans AutoMated: Language";
					}else{
						fakecmd = result + " " + player.getName() + " " + " Ultrabans AutoMated: Language";
					}
					plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), fakecmd);
				}
			}
			
		 }
		 
	}
		 
}
