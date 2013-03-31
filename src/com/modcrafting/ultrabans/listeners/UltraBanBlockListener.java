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
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import com.modcrafting.ultrabans.Ultrabans;

public class UltraBanBlockListener implements Listener {
	Ultrabans plugin;
	public UltraBanBlockListener(Ultrabans instance) {
		plugin = instance;
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event){
		 Player player = event.getPlayer();
		 if(plugin.jailed.containsKey(player.getName().toLowerCase())){
			 if(plugin.jailed.get(player.getName().toLowerCase())>Long.MIN_VALUE){
				 if(tempjailCheck(player)) return;
			 }
			 String adminMsg = plugin.getConfig().getString("Messages.Jail.PlaceMsg", "You cannot place blocks while you are jailed!");
			 player.sendMessage(ChatColor.GRAY + adminMsg);
			 event.setCancelled(true);
		 }
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event){
		 Player player = event.getPlayer();
		 if(plugin.jailed.containsKey(player.getName().toLowerCase())){
			 if(plugin.jailed.get(player.getName().toLowerCase())>Long.MIN_VALUE){
				 if(tempjailCheck(player)) return;
			 }
			 String adminMsg = plugin.getConfig().getString("Messages.Jail.BreakMsg", "You cannot break blocks while you are jailed!");
			 player.sendMessage(ChatColor.GRAY + adminMsg);
			 event.setCancelled(true);
		}
	}
	private boolean tempjailCheck(Player player){
		long tempTime = plugin.jailed.get(player.getName().toLowerCase());
		if(tempTime == Long.MIN_VALUE)
			return false;
		long now = System.currentTimeMillis()/1000;
		long diff = tempTime - now;
		if(diff <= 0){
			//TODO: ADD API
			plugin.jailed.remove(player.getName().toLowerCase());
			plugin.getUBDatabase().removeFromJaillist(player.getName().toLowerCase());
			plugin.getUBDatabase().addPlayer(player.getName(), "Released From Jail", "Served Time", 0, 8);
			Location stlp = plugin.jail.getJail("release");
			player.teleport(stlp);
			String bcmsg = plugin.getConfig().getString("Messages.Pardon.Msg","%victim% was released from jail by %admin%!");
			if(bcmsg.contains(Ultrabans.ADMIN)) bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, Ultrabans.DEFAULT_ADMIN);
			if(bcmsg.contains(Ultrabans.VICTIM)) bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, player.getName());
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', bcmsg));
			return true;
		}
		Date date = new Date();
		date.setTime(tempTime*1000);
		String dateStr = date.toString();
		String reason = plugin.getUBDatabase().getjailReason(player.getName());
		player.sendMessage(ChatColor.GRAY + "You've been tempjailed for " + reason);
		player.sendMessage(ChatColor.GRAY + "Remaining: " + ChatColor.RED + dateStr);
		return false;
	}	 
}
