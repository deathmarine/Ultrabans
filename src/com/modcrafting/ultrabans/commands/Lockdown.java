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
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Lockdown implements CommandExecutor {
	UltraBan plugin;
	public Lockdown(UltraBan ultraBan) {
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
		// Using the config considering the server may restart.
		boolean lock = config.getBoolean("lockdown", false);
		if (args.length < 1) return false;
		String toggle = args[0];
		if (toggle.equalsIgnoreCase("on")){ 
			if(!lock){ 
				lockdownOn();
				sender.sendMessage(ChatColor.GRAY + "Lockdown initiated.PlayerLogin disabled.");
				plugin.getLogger().info(admin + " initiated lockdown.");
			}
			if(lock) sender.sendMessage(ChatColor.GRAY + "Lockdown already initiated.PlayerLogin disabled.");
			return true;
		}
		if (toggle.equalsIgnoreCase("off")){
			if(lock){
				lockdownEnd();
				sender.sendMessage(ChatColor.GRAY + "Lockdown ended.PlayerLogin reenabled.");
				plugin.getLogger().info(admin + " disabled lockdown.");
			}
			if(!lock) sender.sendMessage(ChatColor.GRAY + "Lockdown already ended / never initiated.");
			return true;
		}
		if (toggle.equalsIgnoreCase("status")){
			if(lock) sender.sendMessage(ChatColor.GRAY + "Lockdown in progress.PlayerLogin disabled.");
			if(!lock) sender. sendMessage(ChatColor.GRAY + "Lockdown is not in progress.");
			return true;
		}
		
		return false;
	}
	public void lockdownOn(){
		plugin.getConfig().set("lockdown", (boolean) true);
        plugin.saveConfig();

    }
	public void lockdownEnd(){
		plugin.getConfig().set("lockdown",false);
        plugin.saveConfig();

    }

}
