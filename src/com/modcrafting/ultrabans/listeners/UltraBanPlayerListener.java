/* COPYRIGHT (c) 2015 Deathmarine
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
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.InfoBan;
import com.modcrafting.ultrabans.util.BanType;
import com.modcrafting.ultrabans.util.Formatting;

public class UltraBanPlayerListener implements Listener {
	Ultrabans plugin;
	String spamcheck = null;
	int spamCount = 0;
	FileConfiguration config;
	YamlConfiguration lang;

	public UltraBanPlayerListener(Ultrabans ultraBans) {
		plugin = ultraBans;
		config = ultraBans.getConfig();
		lang = ultraBans.getLangConfig();
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerLogin(PlayerLoginEvent event) {
		// TODO Set IP...
		String reason = Ultrabans.DEFAULT_REASON;
		String admin = Ultrabans.DEFAULT_ADMIN;
		
		final Player player = event.getPlayer();
		String ip = event.getAddress().getHostAddress();

		plugin.getUBDatabase().setAddress(player.getUniqueId().toString(), ip);
		if (plugin.cacheIP.containsKey(ip)) {
			for (InfoBan info : plugin.cacheIP.get(player.getUniqueId()
					.toString())) {
				if (info.getType() == BanType.IPBAN.getId()
						|| info.getType() == BanType.TEMPIPBAN.getId()) {
					// TODO:TempCheck via Type

					if (admin == info.getAdmin())
						admin = info.getAdmin();
					if (reason == info.getReason())
						reason = info.getReason();
					String bcmsg = lang.getString("IPBan.Login");
					if (bcmsg.contains(Ultrabans.ADMIN))
						bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
					if (bcmsg.contains(Ultrabans.REASON))
						bcmsg = bcmsg.replaceAll(Ultrabans.REASON, reason);
					event.disallow(PlayerLoginEvent.Result.KICK_BANNED,
							ChatColor.translateAlternateColorCodes('&', bcmsg));
				}
			}
		}

		if (plugin.cache.containsKey(player.getUniqueId().toString())) {
			for (InfoBan info : plugin.cache.get(player.getUniqueId()
					.toString())) {
				if (info.getType() == BanType.BAN.getId()
						|| info.getType() == BanType.TEMPBAN.getId()) {

					// TODO:TempCheck via Type

					if (admin == info.getAdmin())
						admin = info.getAdmin();
					if (reason == info.getReason())
						reason = info.getReason();
					String bcmsg = lang.getString("Ban.Login");
					if (bcmsg.contains(Ultrabans.ADMIN))
						bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
					if (bcmsg.contains(Ultrabans.REASON))
						bcmsg = bcmsg.replaceAll(Ultrabans.REASON, reason);
					event.disallow(PlayerLoginEvent.Result.KICK_BANNED,
							ChatColor.translateAlternateColorCodes('&', bcmsg));
				}

			}
		}
		if (config.getBoolean("Lockdown", false)
				&& !player.hasPermission("ultraban.override.lockdown")) {
			String lockMsgLogin = lang.getString("Lockdown.LoginMsg",
					"Server is under a lockdown, Try again later!");
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
					ChatColor.translateAlternateColorCodes('&', lockMsgLogin));
			plugin.getLogger().info(
					player.getName() + " attempted to join during lockdown.");
		}

		if (!player.hasPermission("ultraban.override.dupeip")
				&& config.getBoolean("Login.DupeCheck.Enable", true)) {
			plugin.getServer().getScheduler()
					.scheduleSyncDelayedTask(plugin, new Runnable() {
						@Override
						public void run() {
							String ip = plugin.getUBDatabase().getAddress(
									player.getUniqueId().toString());
							if (ip != null) {
								List<String> list = plugin.getUBDatabase()
										.listPlayers(ip);
								for (Player admin : plugin.getServer()
										.getOnlinePlayers()) {
									if (admin.hasPermission("ultraban.dupeip")) {
										for (String name : list) {
											if (!name.equalsIgnoreCase(player
													.getName()))
												admin.sendMessage(ChatColor.GRAY
														+ "Player: "
														+ name
														+ " duplicates player: "
														+ player.getName()
														+ "!");
										}
									}
								}
							}
						}
					}, 20L);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onAsyncLogin(AsyncPlayerPreLoginEvent event) {
		String ip = event.getAddress().getHostAddress();
		String reason = Ultrabans.DEFAULT_ADMIN;
		String admin = Ultrabans.DEFAULT_REASON;
		if (plugin.cacheIP.containsKey(ip)) {
			for (InfoBan info : plugin.cacheIP.get(ip)) {
				if (info.getType() == BanType.IPBAN.getId()
						|| info.getType() == BanType.TEMPIPBAN.getId()) {
					// TODO:TempCheck via Type

					if (admin == info.getAdmin())
						admin = info.getAdmin();
					if (reason == info.getReason())
						reason = info.getReason();
					String bcmsg = lang.getString("IPBan.Login");
					if (bcmsg.contains(Ultrabans.ADMIN))
						bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
					if (bcmsg.contains(Ultrabans.REASON))
						bcmsg = bcmsg.replaceAll(Ultrabans.REASON, reason);
					event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
							ChatColor.translateAlternateColorCodes('&', bcmsg));
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String args[] = event.getMessage().split(" ");
		String adminMsg = config.getString("Messages.Mute.Chat",
				"Your cry falls on deaf ears.");

		if (config.getBoolean("Jail.Vannila", true)
				&& plugin.cache.containsKey(player.getUniqueId().toString())) {
			List<InfoBan> list = plugin.cache.get(player.getUniqueId()
					.toString());
			for (InfoBan info : list) {
				if (info.getType() == BanType.TEMPJAIL.getId()
						|| info.getType() == BanType.JAIL.getId()) {
					if (tempjailCheck(player, info)
							&& config.getStringList("Jail.AllowedCommands")
									.contains(args[0]))
						return;
					player.sendMessage(ChatColor.translateAlternateColorCodes(
							'&', adminMsg));
					event.setCancelled(true);
				}
			}
		}

		if (plugin.muted.contains(player.getUniqueId().toString())
				&& config.getBoolean("Muted.Vannila", true)) {
			if (config.getStringList("Mute.AllowedCommands").contains(args[0]))
				return;
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					adminMsg));
			event.setCancelled(true);
		}

	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String message = event.getMessage();
		String adminMsg = config.getString("Messages.Mute.Chat",
				"Your cry falls on deaf ears.");
		if (plugin.muted.contains(player.getUniqueId().toString())) {
			;
			player.sendMessage(adminMsg);
			event.setCancelled(true);
		}
		if (plugin.cache.containsKey(player.getUniqueId().toString())
				&& config.getBoolean("Jail.Mute", true)) {
			List<InfoBan> list = plugin.cache.get(player.getUniqueId()
					.toString());
			for (InfoBan info : list) {
				if (info.getType() == BanType.TEMPJAIL.getId()
						|| info.getType() == BanType.JAIL.getId()) {
					if (tempjailCheck(player, info))
						return;
					player.sendMessage(ChatColor.translateAlternateColorCodes(
							'&', adminMsg));
					event.setCancelled(true);
				}
			}
		}
		if (config.getBoolean("Chat.IPCheck.Enable", true)) {
			ipcheck(player, message, event);
		}
		if (config.getBoolean("Chat.SpamCheck.Enable", true)) {
			spamcheck(player, message, event);
		}
		if (config.getBoolean("Chat.SwearCensor.Enable", true)) {
			swearcheck(player, message, event);
		}
	}

	private boolean tempjailCheck(Player player, InfoBan info) {
		long tempTime = info.getEndTime();
		long now = System.currentTimeMillis() / 1000;
		long diff = tempTime - now;
		if (diff <= 0) {
			List<InfoBan> list = plugin.cache.get(player.getUniqueId()
					.toString());
			list.remove(info);
			plugin.cache.put(player.getUniqueId().toString(), list);
			plugin.getAPI().pardonPlayer(player.getName(), info.getAdmin());
			Location stlp = plugin.jail.getJail("release");
			player.teleport(stlp);
			String bcmsg = plugin.getConfig().getString("Messages.Pardon.Msg",
					"%victim% was released from jail by %admin%!");
			if (bcmsg.contains(Ultrabans.ADMIN))
				bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN,
						Ultrabans.DEFAULT_ADMIN);
			if (bcmsg.contains(Ultrabans.VICTIM))
				bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, player.getName());
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					bcmsg));
			return true;
		}
		Date date = new Date();
		date.setTime(tempTime * 1000);
		String dateStr = date.toString();
		player.sendMessage(ChatColor.GRAY + "You've been tempjailed for "
				+ info.getReason());
		player.sendMessage(ChatColor.GRAY + "Remaining: " + ChatColor.RED
				+ dateStr);
		return false;
	}

	private void ipcheck(Player player, String message,
			AsyncPlayerChatEvent event) {
		String mes = new String(message);
		String[] content = { "\\,", "\\-", "\\_", "\\=" };
		for (int ii = 0; ii < content.length; ii++) {
			if (mes.contains(content[ii]))
				mes = mes.replaceAll(content[ii], ".");
		}
		String[] ipcheck = mes.split(" ");
		String mode = config.getString("Chat.IPCheck.Blocking");
		if (mode == null)
			mode = "";
		boolean valid = false;
		for (int i = 0; i < ipcheck.length; i++) {
			if (Formatting.validIP(ipcheck[i].trim())) {
				if (mode.equalsIgnoreCase("%scramble%")) {
					event.setMessage(mes.replaceAll(ipcheck[i].trim(),
							ChatColor.MAGIC + "AAAAA"));
				} else if (mode.equalsIgnoreCase("%replace%")) {
					event.setMessage(mes.replaceAll(ipcheck[i].trim(), plugin
							.getServer().getIp()));
				} else {
					event.setMessage(mes.replaceAll(ipcheck[i].trim(), mode));
				}
				valid = true;
			}
		}
		String result = config.getString("Chat.IPCheck.Result", "ban");
		String reason = config.getString("Chat.IPCheck.Reason", "Advertising");
		if (valid && result != null) {
			if (result.equalsIgnoreCase("ban")
					|| result.equalsIgnoreCase("kick")
					|| result.equalsIgnoreCase("ipban")
					|| result.equalsIgnoreCase("jail")
					|| result.equalsIgnoreCase("warn")) {
				String fakecmd = null;
				if (config.getBoolean("Chat.IPCheck.Silent", false)) {
					fakecmd = result + " " + player.getName() + " " + "-s"
							+ " " + reason;
				} else {
					fakecmd = result + " " + player.getName() + " " + reason;
				}
				plugin.getServer().dispatchCommand(
						plugin.getServer().getConsoleSender(), fakecmd);
			}
		}
	}

	private void spamcheck(Player player, String message,
			AsyncPlayerChatEvent event) {
		String mes = new String(message);
		if (!mes.equalsIgnoreCase(spamcheck)) {
			spamcheck = event.getMessage();
			spamCount = 0;
		} else {
			event.setCancelled(true);
			spamCount++;
		}
		String result = config.getString("Chat.SpamCheck.Result", "kick");
		String reason = config.getString("Chat.SpamCheck.Reason", "Spam");
		if (config.getInt("Chat.SpamCheck.Counter") < spamCount
				&& result != null) {
			if (result.equalsIgnoreCase("ban")
					|| result.equalsIgnoreCase("kick")
					|| result.equalsIgnoreCase("ipban")
					|| result.equalsIgnoreCase("jail")
					|| result.equalsIgnoreCase("warn")) {
				String fakecmd = null;
				if (config.getBoolean("Chat.SpamCheck.Silent", false)) {
					fakecmd = result + " " + player.getName() + " " + "-s"
							+ " " + reason;
				} else {
					fakecmd = result + " " + player.getName() + " " + reason;
				}
				plugin.getServer().dispatchCommand(
						plugin.getServer().getConsoleSender(), fakecmd);
			}
		}

	}

	private void swearcheck(Player player, String message,
			AsyncPlayerChatEvent event) {
		String mes = new String(message);
		String[] string = config.getString("Chat.SwearCensor.Words").split(" ");
		String mode = config.getString("Chat.SwearCensor.Blocking");
		if (mode == null)
			mode = "";
		boolean valid = false;
		for (int i = 0; i < string.length; i++) {
			if (Pattern
					.compile(Pattern.quote(string[i].trim()),
							Pattern.CASE_INSENSITIVE).matcher(mes).find()) {
				if (mode.equalsIgnoreCase("%scramble%")) {
					mes = mes.replaceAll(string[i].trim(), ChatColor.MAGIC
							+ "AAAAA");
				} else {
					mes = mes.replaceAll(string[i].trim(), mode);
				}
				valid = true;
			}
		}
		event.setMessage(mes);
		String result = config.getString("Chat.SwearCensor.Result", "mute");
		String reason = config.getString("Chat.SwearCensor.Reason", "Language");
		if (valid && result != null) {
			if (result.equalsIgnoreCase("ban")
					|| result.equalsIgnoreCase("kick")
					|| result.equalsIgnoreCase("ipban")
					|| result.equalsIgnoreCase("jail")
					|| result.equalsIgnoreCase("warn")
					|| result.equalsIgnoreCase("mute")) {
				String fakecmd = null;
				if (config.getBoolean("Chat.SwearCensor.Silent", false)) {
					fakecmd = result + " " + player.getName() + " " + "-s"
							+ " " + reason;
				} else {
					fakecmd = result + " " + player.getName() + " " + reason;
				}
				plugin.getServer().dispatchCommand(
						plugin.getServer().getConsoleSender(), fakecmd);
			}
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if (plugin.cache.containsKey(player.getUniqueId().toString())) {
			List<InfoBan> list = plugin.cache.get(player.getUniqueId()
					.toString());
			for (InfoBan info : list) {
				if (info.getType() == BanType.TEMPJAIL.getId()
						|| info.getType() == BanType.JAIL.getId()) {
					if (tempjailCheck(player, info))
						return;
					event.setRespawnLocation(plugin.jail.getJail("jail"));
				}
			}
		}
	}
}
