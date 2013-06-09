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

import java.util.Date;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.BanInfo;
import com.modcrafting.ultrabans.util.BanType;

public class History extends CommandHandler{
	public History(Ultrabans instance) {
		super(instance);
	}

	public String command(CommandSender sender, Command command, String[] args) {
		String size = "5";
		if (args.length > 0) 
			size = args[0];
		List<BanInfo> bans = plugin.getUBDatabase().listRecent(size);
		if(bans.size() < 1)
			return lang.getString("History.Failed");
		String msg = lang.getString("History.Header");
		if(msg.contains(Ultrabans.AMOUNT)) 
			msg = msg.replace(Ultrabans.AMOUNT, args[0]);
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
		for(BanInfo ban : bans){
			Date date = new Date();
			date.setTime(ban.getEndTime()*1000);
			String dateStr = date.toString();
			switch(BanType.fromID(ban.getType())){
				case TEMPBAN:
				case TEMPIPBAN:
				case TEMPJAIL:{
					sender.sendMessage(ChatColor.RED + BanType.toCode(ban.getType()) + ": " + ban.getName() + ChatColor.GRAY + " by " + ban.getAdmin() + " till " + dateStr.substring(4, 19) + " for " + ban.getReason());
					break;
				}
				default:{
					sender.sendMessage(ChatColor.RED + BanType.toCode(ban.getType()) + ": " + ban.getName() + ChatColor.GRAY + " by " + ban.getAdmin() + " for " + ban.getReason());
					break;	
				}
			}
		}
		return null;
	}
}