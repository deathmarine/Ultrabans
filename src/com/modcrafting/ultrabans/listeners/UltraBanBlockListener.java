/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.listeners;

import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
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
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		 Player player = event.getPlayer();
		 if(plugin.jailed.contains(player.getName().toLowerCase())){
			 if(plugin.tempJail.get(player.getName().toLowerCase()) != null){
				 if(tempjailCheck(player)) return;
			 }
			 String adminMsg = config.getString("Messages.Jail.PlaceMsg", "You cannot place blocks while you are jailed!");
			 player.sendMessage(ChatColor.GRAY + adminMsg);
			 event.setCancelled(true);
		 }
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event){
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		 Player player = event.getPlayer();
		 if(plugin.jailed.contains(player.getName().toLowerCase())){
			 if(plugin.tempJail.get(player.getName().toLowerCase()) != null){
				 if(tempjailCheck(player)) return;
			 }
			 String adminMsg = config.getString("Messages.Jail.BreakMsg", "You cannot break blocks while you are jailed!");
			 player.sendMessage(ChatColor.GRAY + adminMsg);
			 event.setCancelled(true);
		}
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
			String bcmsg = plugin.getConfig().getString("Messages.Pardon.Msg","%victim% was released from jail by %admin%!");
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
}
