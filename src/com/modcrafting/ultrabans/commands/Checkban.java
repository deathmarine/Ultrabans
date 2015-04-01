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

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.InfoBan;
import com.modcrafting.ultrabans.util.BanType;
import com.modcrafting.ultrabans.util.Formatting;

public class Checkban extends CommandHandler {
	public Checkban(Ultrabans ultraBan) {
		super(ultraBan);
	}

	public String command(final CommandSender sender, Command command,
			String[] args) {
		if (args.length < 1)
			return lang.getString("CheckBan.Arguments");
		String name = args[0];
		name = Formatting.expandName(name);
		OfflinePlayer check = plugin.getServer().getOfflinePlayer(name);
		if (check != null)
			name = check.getName();
		String uuid = check.getUniqueId().toString();

		List<InfoBan> bans = plugin.getUBDatabase().listRecords(uuid);
		if (bans.isEmpty()) {
			String msg = lang.getString("CheckBan.None");
			if (msg.contains(Ultrabans.AMOUNT))
				msg = msg
						.replace(Ultrabans.AMOUNT, String.valueOf(bans.size()));
			if (msg.contains(Ultrabans.VICTIM))
				msg = msg.replace(Ultrabans.VICTIM, name);
			return msg;
		}
		String msg = lang.getString("CheckBan.Header");
		if (msg.contains(Ultrabans.AMOUNT))
			msg = msg.replace(Ultrabans.AMOUNT, String.valueOf(bans.size()));
		if (msg.contains(Ultrabans.VICTIM))
			msg = msg.replace(Ultrabans.VICTIM, bans.get(0).getName());
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
		for (InfoBan ban : bans) {
			sender.sendMessage(ChatColor.RED + BanType.toCode(ban.getType())
					+ ": " + ChatColor.GREEN + ban.getReason() + ChatColor.AQUA
					+ " by " + ban.getAdmin());
		}
		return null;
	}

}
