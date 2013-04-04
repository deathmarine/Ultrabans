/* COPYRIGHT (c) 2013 Deathmarine (Joshua McCurry)
 * This file is part of Ultrabans.
 * Ultrabans is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Ultrabans is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Ultrabans.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.modcrafting.ultrabans.Ultrabans;

public abstract class CommandHandler implements CommandExecutor{
	Ultrabans plugin;
	FileConfiguration config;
	YamlConfiguration lang;
	public CommandHandler(Ultrabans instance){
		plugin=instance;
		config=instance.getConfig();
		lang=instance.getLangConfig();
	}

	public boolean onCommand(final CommandSender sender, final Command command, String label, final String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(Ultrabans.DEFAULT_DENY_MESSAGE);
			return true;
		}

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			@Override
			public void run() {
				String message = command(sender, command, args);
				if(message!=null)
					message = ChatColor.translateAlternateColorCodes('&', message);
					if(message.contains("%n%")){
						for(String m : message.split("%n%"))
							sender.sendMessage(m);
					}else{
						sender.sendMessage(message);
					}
			}
		});
		return true;
		
	}
	
	public abstract String command(CommandSender sender,Command command, String[] args);

}
