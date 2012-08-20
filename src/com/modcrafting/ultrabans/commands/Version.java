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
import org.bukkit.plugin.PluginDescriptionFile;

import com.modcrafting.ultrabans.UltraBan;

public class Version implements CommandExecutor{
	UltraBan plugin;
	public Version(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		PluginDescriptionFile pdfFile = plugin.getDescription();
		sender.sendMessage(ChatColor.BLUE + "Thank you " + sender.getName() + " for using:");
		sender.sendMessage(ChatColor.GRAY + pdfFile.getName() + " version " + pdfFile.getVersion() + " by " + pdfFile.getAuthors());
		return true;
	}
	
}

