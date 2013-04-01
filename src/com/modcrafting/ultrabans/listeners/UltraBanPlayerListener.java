/* COPYRIGHT (c) 2013 Deathmarine (Joshua McCurry)
 * This file is part of Ultrabans.
 * Ultrabans is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Ultrabans is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Ultrabans.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.modcrafting.ultrabans.listeners;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.Formatting;

public class UltraBanPlayerListener implements Listener{
	Ultrabans plugin;
	String spamcheck = null;
	int spamCount = 0;
	FileConfiguration config;
	String version;
	public UltraBanPlayerListener(Ultrabans ultraBans) {
		plugin = ultraBans;
		config = ultraBans.getConfig();

		String p2 = plugin.getServer().getClass().getPackage().getName();
        version = p2.substring(p2.lastIndexOf('.') + 1);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerLogin(PlayerLoginEvent event){
		final Player player = event.getPlayer();
		if(plugin.bannedPlayers.containsKey(player.getName().toLowerCase())){
			String reason = plugin.getUBDatabase().getBanReason(player.getName());
			String admin = plugin.getUBDatabase().getAdmin(player.getName());
			if(admin==null) admin = Ultrabans.DEFAULT_ADMIN;
			if(reason==null) reason = Ultrabans.DEFAULT_REASON;
			String bcmsg = config.getString("Messages.Ban.Login", "%admin% banned you from this server! Reason: %reason%!");
			if(bcmsg.contains(Ultrabans.ADMIN)) bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
			if(bcmsg.contains(Ultrabans.REASON)) bcmsg = bcmsg.replaceAll(Ultrabans.REASON, reason);
			//bcmsg=Formatting.formatMessage(bcmsg);
			event.disallow(PlayerLoginEvent.Result.KICK_BANNED, bcmsg);
		}
		if(plugin.bannedPlayers.get(player.getName().toLowerCase()) != null){
			String reason = plugin.getUBDatabase().getBanReason(player.getName());
			String admin = plugin.getUBDatabase().getAdmin(player.getName());
			if(admin==null) admin = Ultrabans.DEFAULT_ADMIN;
			if(reason==null) reason = Ultrabans.DEFAULT_REASON;
			long tempTime = plugin.bannedPlayers.get(player.getName().toLowerCase());
			long diff = tempTime - (System.currentTimeMillis()/1000);
			if(diff <= 0){
				String ip = plugin.getUBDatabase().getAddress(player.getName());
				if(plugin.bannedIPs.containsKey(ip)){
					plugin.bannedIPs.remove(ip);
					Bukkit.unbanIP(ip);
				}
				//plugin.tempBans.remove(player.getName().toLowerCase());
				plugin.bannedPlayers.remove(player.getName().toLowerCase());
				plugin.getUBDatabase().removeFromBanlist(player.getName().toLowerCase());
				plugin.getUBDatabase().addPlayer(player.getName(), "Untempbanned: " + reason, admin, 0, 5);
				return;
			}
			Date date = new Date();
			date.setTime(tempTime*1000);
			String dateStr = date.toString();
			String msgvic = config.getString("Messages.TempBan.Login", "You have been tempbanned by %admin% for %time%. Reason: %reason%!");
			if(msgvic.contains(Ultrabans.ADMIN)) msgvic = msgvic.replaceAll(Ultrabans.ADMIN, admin);
			if(msgvic.contains(Ultrabans.REASON)) msgvic = msgvic.replaceAll(Ultrabans.REASON, reason);
			if(msgvic.contains(Ultrabans.TIME)) msgvic = msgvic.replaceAll(Ultrabans.TIME, dateStr.substring(4, 19));
			//msgvic=Formatting.formatMessage(msgvic);
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, msgvic);
			return;
		}
		if(config.getBoolean("Lockdown", false)&&!player.hasPermission("ultraban.override.lockdown")){
			String lockMsgLogin = config.getString("Messages.Lockdown.LoginMsg", "Server is under a lockdown, Try again later!");
			//lockMsgLogin=Formatting.formatMessage(lockMsgLogin);
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, lockMsgLogin);
			plugin.getLogger().info(player.getName() + " attempted to join during lockdown.");
		}
		
		if(!player.hasPermission("ultraban.override.dupeip")&&config.getBoolean("Login.DupeCheck.Enable", true)){
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,new Runnable(){
				@Override
				public void run() {
					String ip = plugin.getUBDatabase().getAddress(player.getName());
					if(ip != null){
						List<String> list = plugin.getUBDatabase().listPlayers(ip);
						for(Player admin:plugin.getServer().getOnlinePlayers()){
							if(admin.hasPermission("ultraban.dupeip")){
								for(String name:list){
									if(!name.equalsIgnoreCase(player.getName())) admin.sendMessage(ChatColor.GRAY + "Player: " + name + " duplicates player: " + player.getName() + "!");
								}
							}
						}
					}
				}
			},20L);			
		}
	 	if(plugin.jailed.get(player.getName().toLowerCase()) != null)
	 		tempjailCheck(player);
	}
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAsyncLogin(AsyncPlayerPreLoginEvent event){
		//final Player player = event.getPlayer();
		final String ip = event.getAddress().getHostAddress();
		//TODO: from above plugin.getUBDatabase().setAddress(player.getName().toLowerCase(), ip);
		if(plugin.bannedIPs.containsKey(ip)){
			//event.setJoinMessage(null);
			String adminMsg = config.getString("Messages.IPBan.Login", "Your IP is banned!");
			//adminMsg=Formatting.formatMessage(adminMsg);
			//player.kickPlayer(adminMsg);
			event.disallow(Result.KICK_BANNED, ChatColor.translateAlternateColorCodes('&', adminMsg));
		}
		String player_name = plugin.getUBDatabase().getName(ip);
		
		/*
		//Shows 0 considering the first keepalive packet hasn't been sent.
		if(config.getBoolean("Login.PingCheck.Enable",true)){
			boolean p=false;
			
			int ping = 0;
            for(Method meth:player.getClass().getMethods()){
            	if(meth.getName().equals("getHandle")){
					try {
						Object obj = meth.invoke(player, (Object[]) null);
	            		for(Field field:obj.getClass().getFields()){
	            			if(field.getName().equals("ping")){
	            				ping = field.getInt(obj);
	            			}
	            		}
					} catch (Exception e) {
						e.printStackTrace();
					}
            	}
            }
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
		*/
	}
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
		Player player = event.getPlayer();
		String adminMsg = config.getString("Messages.Mute.Chat", "Your cry falls on deaf ears.");
		if(plugin.jailed.containsKey(player.getName().toLowerCase())&&config.getBoolean("Jail.Vannila", true)){
			String args[] = event.getMessage().split(" ");
			if(config.getStringList("Jail.AllowedCommands").contains(args[0])) return;
		 	if(plugin.jailed.get(player.getName().toLowerCase()) != null){
		 		if(tempjailCheck(player)) return;

		 	}
			//player.sendMessage(Formatting.formatMessage(adminMsg));
			event.setCancelled(true);
		 }	
		if(plugin.muted.contains(player.getName().toLowerCase())&&config.getBoolean("Muted.Vannila", true)){
			String args[] = event.getMessage().split(" ");
			if(config.getStringList("Mute.AllowedCommands").contains(args[0])) return;
			//player.sendMessage(Formatting.formatMessage(adminMsg));
			event.setCancelled(true);
		}
			
	}
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerChat(AsyncPlayerChatEvent event){
		Player player = event.getPlayer();
		String message = event.getMessage();
		String adminMsg = config.getString("Messages.Mute.Chat", "Your cry falls on deaf ears.");
		if(plugin.muted.contains(player.getName().toLowerCase())){;
			//player.sendMessage(Formatting.formatMessage(adminMsg));
			event.setCancelled(true);
		}
		if(plugin.jailed.containsKey(player.getName().toLowerCase())&&config.getBoolean("Jail.Mute",true)){
			if(plugin.jailed.get(player.getName().toLowerCase())!=null&&tempjailCheck(player)) return;
			//player.sendMessage(Formatting.formatMessage(adminMsg));
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
	/*
	private boolean checkPlayerPing(Player player,int ping){
		int pingout =config.getInt("Login.PingCheck.MaxPing",500);
		if(ping>pingout&&!player.hasPermission("ultraban.override.pingcheck")){
			String msgvic = config.getString("Messages.Kick.MsgToVictim", "You have been kicked by %admin%. Reason: %reason%");
			if(msgvic.contains(Ultrabans.ADMIN)) msgvic = msgvic.replaceAll(Ultrabans.ADMIN, "Ultrabans");
			if(msgvic.contains(Ultrabans.REASON)) msgvic = msgvic.replaceAll(Ultrabans.REASON, "High Ping Rate");
			msgvic=Formatting.formatMessage(msgvic);
			player.kickPlayer(msgvic);
			return true;
		}
		//pass
		return false;
	}
	*/
	private boolean tempjailCheck(Player player){
		long tempTime = plugin.jailed.get(player.getName().toLowerCase());
		long now = System.currentTimeMillis()/1000;
		long diff = tempTime - now;
		if(diff <= 0){
			plugin.jailed.remove(player.getName().toLowerCase());
			plugin.jailed.remove(player.getName().toLowerCase());
			plugin.getUBDatabase().removeFromJaillist(player.getName().toLowerCase());
			plugin.getUBDatabase().addPlayer(player.getName(), "Released From Jail", "Served Time", 0, 8);
			Location stlp = plugin.jail.getJail("release");
			player.teleport(stlp);
			String bcmsg = config.getString("Messages.Pardon.Msg","%victim% was released from jail by %admin%!");
			if(bcmsg.contains(Ultrabans.ADMIN)) bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, Ultrabans.DEFAULT_ADMIN);
			if(bcmsg.contains(Ultrabans.VICTIM)) bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, player.getName());
			//bcmsg=Formatting.formatMessage(bcmsg);
			player.sendMessage(bcmsg);
			return true;
		}
		Date date = new Date();
		date.setTime(tempTime*1000);
		String dateStr = date.toString();
		String reason = plugin.getUBDatabase().getjailReason(player.getName());
		if(reason==null) reason = Ultrabans.DEFAULT_REASON;
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
			if(Formatting.validIP(ipcheck[i].trim())){
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
			 if(Pattern.compile(Pattern.quote(string[i].trim()), Pattern.CASE_INSENSITIVE).matcher(mes).find()){
				 if(mode.equalsIgnoreCase("%scramble%")){
					 mes = mes.replaceAll(string[i].trim(), ChatColor.MAGIC + "AAAAA");
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
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event){
		if(plugin.jailed.containsKey(event.getPlayer().getName())){
			event.setRespawnLocation(plugin.jail.getJail("jail"));
		}
	}
}
