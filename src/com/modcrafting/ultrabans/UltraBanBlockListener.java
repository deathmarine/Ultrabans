package com.modcrafting.ultrabans;

import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
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
						World wtlp = player.getWorld();
						Location tlp = wtlp.getSpawnLocation();
						player.teleport(tlp);
						return;
					}
					Date date = new Date();
					date.setTime(tempTime*1000);
					String dateStr = date.toString();
					String reason = plugin.db.getBanReason(player.getName());
					player.sendMessage("You've been tempjailed for " + reason + " Remaining:" + dateStr);
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
						World wtlp = player.getWorld();
						Location tlp = wtlp.getSpawnLocation();
						player.teleport(tlp);
						return;
					}
					Date date = new Date();
					date.setTime(tempTime*1000);
					String dateStr = date.toString();
					String reason = plugin.db.getBanReason(player.getName());
					player.sendMessage("You've been tempjailed for " + reason + " Remaining:" + dateStr);
				}
			 String adminMsg = config.getString("messages.jailBreakMsg", "You cannot break blocks while you are jailed!");
			 player.sendMessage(ChatColor.GRAY + adminMsg);
			 event.setCancelled(true);
		}
	}
		 
}
