/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */

package com.modcrafting.ultrabans.commands;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.Ultrabans;

public class CheckIP implements CommandExecutor{
	Ultrabans plugin;
	public CheckIP(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(final CommandSender sender, Command command, String label, final String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+Ultrabans.DEFAULT_DENY_MESSAGE);
			return true;
		}
		if (args.length < 1) return false;
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,new Runnable(){

			@Override
			public void run() {
				String p = args[0];
				String ip = plugin.getUBDatabase().getAddress(p.toLowerCase());
				InetAddress InetP;
				if(ip == null){
					Player n = plugin.getServer().getPlayer(p);
					if(n!=null){
						plugin.getUBDatabase().setAddress(n.getName().toLowerCase(),plugin.getServer().getPlayer(p).getAddress().getAddress().getHostAddress());
					}else{
						sender.sendMessage(ChatColor.GRAY + "Player not found!");
						return;
					}
				}
				try {
					InetP = InetAddress.getByName(ip);
					sender.sendMessage(ChatColor.YELLOW + "IP Address: " + ip);
					sender.sendMessage(ChatColor.YELLOW + "Host Address: " + InetP.getHostAddress());
					sender.sendMessage(ChatColor.YELLOW + "Host Name: " + InetP.getHostName());
					sender.sendMessage(ChatColor.YELLOW + "Connection: " + InetP.getCanonicalHostName());
			
					try {
						boolean ping = InetP.isReachable(1800);
						if (ping){
							sender.sendMessage(ChatColor.GREEN + "Ping Test Passed.");
						}else{
							sender.sendMessage(ChatColor.RED + "Ping Test Failed.");
						}
					} catch (IOException e) {
						sender.sendMessage(ChatColor.RED + "Ping Test Failed.");
					}
				} catch (UnknownHostException e) {
					sender.sendMessage(ChatColor.RED + "Gathering Information Failed: Recommend Kick!");
				}
			}
		});
		return true;
		
	}
}
