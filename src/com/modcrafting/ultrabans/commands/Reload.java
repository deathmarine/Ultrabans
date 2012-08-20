/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.modcrafting.ultrabans.UltraBan;

public class Reload implements CommandExecutor{
	UltraBan plugin;
	public Reload(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender.hasPermission(command.getPermission())){
			plugin.reloadConfig();
			plugin.getServer().getPluginManager().disablePlugin(plugin);
			plugin.getServer().getPluginManager().enablePlugin(plugin);
			sender.sendMessage("§2[UltraBan] reloaded.");
			return true;
		}else{
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
	}
	


}
