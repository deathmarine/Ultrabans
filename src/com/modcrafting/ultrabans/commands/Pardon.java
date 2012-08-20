/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Pardon implements CommandExecutor{
	UltraBan plugin;
    public Pardon(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		Player player = null;
		String admin = config.getString("defAdminName", "server");
		if (sender instanceof Player){
			player = (Player)sender;
			admin = player.getName();
		}
		if(!sender.hasPermission((String) plugin.getDescription().getCommands().get(label.toLowerCase()).get("permission"))){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		if (args.length < 1) return false;
		
		String jaile = args[0];
		if(plugin.autoComplete) jaile = plugin.util.expandName(jaile);
		plugin.jailed.remove(jaile.toLowerCase());
		
		Player jailee = plugin.getServer().getPlayer(jaile);
		if(jailee == null){
			jailee = plugin.getServer().getOfflinePlayer(jaile).getPlayer();
			if(jailee == null){
				sender.sendMessage(ChatColor.RED + "Unable to find Jailed Player");
				return true;
			}
		}
		plugin.db.removeFromJaillist(jailee.getName());
		plugin.db.addPlayer(jailee.getName(), "Released From Jail", admin, 0, 8);
		
		Location stlp = plugin.jail.getJail("release");
		
		if(stlp != null){
			jailee.teleport(stlp);
		}else{
			jailee.teleport(jailee.getWorld().getSpawnLocation());
		}
		
		if(plugin.tempJail.containsKey(jaile.toLowerCase())){
			plugin.tempJail.remove(jaile.toLowerCase());
		}
		
		String jailMsgRelease = config.getString("messages.jailMsgRelease");
		if(jailMsgRelease.contains(plugin.regexAdmin)) jailMsgRelease = jailMsgRelease.replaceAll(plugin.regexAdmin, admin);
		if(jailMsgRelease.contains(plugin.regexVictim)) jailMsgRelease = jailMsgRelease.replaceAll(plugin.regexVictim, jaile);
		if(jailMsgRelease != null){
			jailee.sendMessage(plugin.util.formatMessage(jailMsgRelease));
			sender.sendMessage(plugin.util.formatMessage(jailMsgRelease));
		}
		return true;
	}
}
