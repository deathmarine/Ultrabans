package com.modcrafting.ultrabans;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class UltraBanBlockListener extends BlockListener {
	public static UltraBan plugin;
	public UltraBanBlockListener(UltraBan instance) {
	plugin = instance;
	}
	public void onBlockPlace(BlockPlaceEvent event){
		 Player player = event.getPlayer();
		 if(plugin.jailed.contains(player.getName().toLowerCase())){
		 player.sendMessage(ChatColor.GRAY + " You cannot place blocks while you are jailed!");
		 event.setCancelled(true);
		 }
	 }
	public void onBlockBreak(BlockBreakEvent event){
		 Player player = event.getPlayer();
		 if(plugin.jailed.contains(player.getName().toLowerCase())){
		 player.sendMessage(ChatColor.GRAY + " You cannot break blocks while you are jailed!");
		 event.setCancelled(true);
		 }
	}
		 
}
