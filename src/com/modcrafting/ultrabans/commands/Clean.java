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

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.Formatting;

public class Clean extends CommandHandler{
	public Clean(Ultrabans ultraBan) {
		super(ultraBan);
	}
	
	public String command(final CommandSender sender, Command command, String[] args) {
		int count = 0;
		for(String name:plugin.cache.keySet()){
			OfflinePlayer of = plugin.getServer().getOfflinePlayer(name);
			if(of!=null && Formatting.deletePlyrdat(of.getName())) 
				count++;
		}
		String msg = lang.getString("Clean.Complete");
		if(msg.contains(Ultrabans.AMOUNT)) 
			msg = msg.replace(Ultrabans.AMOUNT, String.valueOf(count));
		return msg;
	}

}
