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

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.Formatting;

public class DupeIP extends CommandHandler{
	public DupeIP(Ultrabans ultraBan) {
		super(ultraBan);
	}
	
	public String command(final CommandSender sender, Command command, final String[] args) {
		if (args.length < 1) 
			return lang.getString("DupeIP.Argument");
		String name = Formatting.expandName(args[0]);
		String ip = plugin.getUBDatabase().getAddress(name);
		if(ip == null){
			OfflinePlayer n = plugin.getServer().getOfflinePlayer(name);
			if(n!=null && n.isOnline()){
				ip = n.getPlayer().getAddress().getAddress().getHostAddress();
				plugin.getUBDatabase().setAddress(n.getName().toLowerCase(), ip);
			}else{
				return lang.getString("DupeIP.NoPlayer");
			}
		}
		List<String> list = plugin.getUBDatabase().listPlayers(ip);
		String msg = lang.getString("DupeIP.Header");
		if(msg.contains(Ultrabans.VICTIM)) 
			msg = msg.replace(Ultrabans.VICTIM, name);
		if(msg.contains("%ip%")) 
			msg = msg.replace("%ip%", ip);
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<(list.size()-1);i++){
			if(!list.get(i).equalsIgnoreCase(name)){
				sb.append(list.get(i)).append(" ");
			}
		}
		if(sb.toString().length()>0)
			sender.sendMessage(ChatColor.GRAY+sb.toString());
		return lang.getString("DupeIP.Completed");
	}
}
