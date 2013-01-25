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

import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.Formatting;

public class Lockdown implements CommandExecutor {
	Ultrabans plugin;
	public Lockdown(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+Ultrabans.DEFAULT_DENY_MESSAGE);
			return true;
		}
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		Player player = null;
		String admin = Ultrabans.DEFAULT_ADMIN;
		if (sender instanceof Player){
			player = (Player)sender;
			admin = player.getName();
		}
		boolean lock = config.getBoolean("Lockdown", false);
		if (args.length < 1) return false;
		String toggle = args[0];
		if (toggle.equalsIgnoreCase("on")){ 
			if(!lock){ 
				lockdownOn();
				sender.sendMessage(ChatColor.GRAY + Formatting.formatMessage(config.getString("Messages.Lockdown.Start","Lockdown initiated. PlayerLogin disabled.")));
				plugin.getLogger().info(admin + " initiated lockdown.");
			}else{
				sender.sendMessage(ChatColor.GRAY + Formatting.formatMessage(config.getString("Messages.Lockdown.LoginMsg","Server is under a lockdown, Try again later! Sorry.")));
			}
		}
		if (toggle.equalsIgnoreCase("off")){
			if(lock){
				lockdownEnd();
				sender.sendMessage(ChatColor.GRAY + Formatting.formatMessage(config.getString("Messages.Lockdown.End","Lockdown ended.PlayerLogin reenabled.")));
				plugin.getLogger().info(admin + " disabled lockdown.");
			}else{
				sender.sendMessage(ChatColor.GRAY + Formatting.formatMessage(config.getString("Messages.Lockdown.Status","Lockdown is disabled.")));
			}
		}
		if (toggle.equalsIgnoreCase("status")){
			if(lock){
				sender.sendMessage(ChatColor.GRAY + Formatting.formatMessage(config.getString("Messages.Lockdown.LoginMsg","Server is under a lockdown, Try again later! Sorry.")));
			}else{
				sender.sendMessage(ChatColor.GRAY + Formatting.formatMessage(config.getString("Messages.Lockdown.Status","Lockdown is disabled.")));
			}
		}
		
		return true;
	}
	private void lockdownOn(){
		plugin.getConfig().set("Lockdown",(boolean) true);
        plugin.saveConfig();
    }
	private void lockdownEnd(){
		plugin.getConfig().set("Lockdown",(boolean) false);
        plugin.saveConfig();
    }

}
