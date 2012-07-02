/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans;

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

public class UltraBanBlockListener implements Listener {
	public static UltraBan plugin;
	public UltraBanBlockListener(UltraBan instance) {
	plugin = instance;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event){
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
			String adminMsg = config.getString("messages.jailPlaceMsg", "You cannot place blocks while you are jailed!");
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
			 String adminMsg = config.getString("messages.jailBreakMsg", "You cannot break blocks while you are jailed!");
			 player.sendMessage(ChatColor.GRAY + adminMsg);
			 event.setCancelled(true);
		}
	}
		 
}
