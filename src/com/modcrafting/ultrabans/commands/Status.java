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
import org.bukkit.command.CommandSender;
import com.modcrafting.ultrabans.Ultrabans;

public class Status extends CommandHandler{
	public Status(Ultrabans instance) {
		super(instance);
	}

	public String command(final CommandSender sender, Command command, String[] args) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getString("Status.CacheHeader")));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getString("Status.CacheBans")
				.replace(Ultrabans.AMOUNT,String.valueOf(plugin.bannedPlayers.size()))));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getString("Status.CacheIPBans")
				.replace(Ultrabans.AMOUNT,String.valueOf(plugin.bannedIPs.size()))));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getString("Status.CacheJail")
				.replace(Ultrabans.AMOUNT,String.valueOf(plugin.jailed.size()))));
		int counter=0;
		counter = counter+plugin.bannedPlayers.toString().getBytes().length;
		counter = counter+plugin.bannedIPs.toString().getBytes().length;
		counter = counter+plugin.jailed.toString().getBytes().length;
		return lang.getString("Status.Usage").replace(Ultrabans.AMOUNT, String.valueOf(counter));
	}
}
