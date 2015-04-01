/* COPYRIGHT (c) 2015 Deathmarine
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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.Ultrabans;

public class Inventory extends CommandHandler{
	public Inventory(Ultrabans instance) {
		super(instance);
	}
	
	public String command(CommandSender sender, Command command, String[] args) {
		if(sender instanceof Player){
			if (args.length < 1) 
				return lang.getString("InvOf.Arguments");
			OfflinePlayer victim = plugin.getServer().getOfflinePlayer(args[0]);
			if(victim==null||!victim.isOnline()){
				return ChatColor.translateAlternateColorCodes('&', lang.getString("InvOf.Failed"));
			}
			((Player) sender).openInventory(victim.getPlayer().getInventory());
			return null;
		}
		return ChatColor.translateAlternateColorCodes('&', lang.getString("InvOf.Console"));
	}
}
