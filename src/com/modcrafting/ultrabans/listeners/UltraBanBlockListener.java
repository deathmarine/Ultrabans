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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.BanInfo;
import com.modcrafting.ultrabans.util.BanType;

public class UltraBanBlockListener implements Listener {
	Ultrabans plugin;

	public UltraBanBlockListener(Ultrabans instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (plugin.cache.containsKey(player.getUniqueId().toString())) {
			for (BanInfo info : plugin.cache.get(player.getUniqueId()
					.toString())) {
				if (info.getType() == BanType.TEMPJAIL.getId()
						|| info.getType() == BanType.JAIL.getId()) {
					if (info.getType() == BanType.TEMPJAIL.getId()
							&& tempjailCheck(player, info))
						return;
					String adminMsg = plugin.getConfig().getString(
							"Messages.Jail.PlaceMsg",
							"You cannot place blocks while you are jailed!");
					player.sendMessage(ChatColor.GRAY + adminMsg);
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (plugin.cache.containsKey(player.getUniqueId().toString())) {
			List<BanInfo> list = plugin.cache.get(player.getUniqueId()
					.toString());
			for (BanInfo info : list) {
				if (info.getType() == BanType.TEMPJAIL.getId()
						|| info.getType() == BanType.JAIL.getId()) {
					if (info.getType() == BanType.TEMPJAIL.getId()
							&& tempjailCheck(player, info))
						return;
					String adminMsg = plugin.getConfig().getString(
							"Messages.Jail.BreakMsg",
							"You cannot break blocks while you are jailed!");
					player.sendMessage(ChatColor.GRAY + adminMsg);
					event.setCancelled(true);
				}
			}
		}
	}

	private boolean tempjailCheck(Player player, BanInfo info) {
		long tempTime = info.getEndTime();
		long now = System.currentTimeMillis() / 1000;
		long diff = tempTime - now;
		if (diff <= 0) {
			List<BanInfo> list = plugin.cache.get(player.getUniqueId()
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
}
