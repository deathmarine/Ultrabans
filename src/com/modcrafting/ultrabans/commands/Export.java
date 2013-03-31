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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.modcrafting.ultrabans.Ultrabans;

public class Export extends CommandHandler{
	public Export(Ultrabans ultraBan) {
		super(ultraBan);
	}
	
	public String command(final CommandSender sender, Command command, String[] args) {
		try{
			BufferedWriter banlist = new BufferedWriter(new FileWriter("banned-players.txt",true));
			for(String p : plugin.bannedPlayers.keySet()){
				banlist.newLine();
				banlist.write(g(p));
			}
			banlist.close();
			BufferedWriter iplist = new BufferedWriter(new FileWriter("banned-ips.txt",true));
			for(String p : plugin.bannedIPs.keySet()){
				iplist.newLine();
				iplist.write(g(p));
			}
			iplist.close();
		}catch(IOException e){
			String msg = ChatColor.translateAlternateColorCodes('&', lang.getString("Export.Failed"));
			sender.sendMessage(msg);
			if(plugin.getLog())
				plugin.getLogger().severe(ChatColor.stripColor(msg));
		}
		String msg = ChatColor.translateAlternateColorCodes('&', lang.getString("Export.Completed"));
		if(plugin.getLog())
			plugin.getLogger().severe(ChatColor.stripColor(msg));
		return msg;
	}
	
	public String g(String player) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
		Date now = new Date();
		now.setTime(System.currentTimeMillis());
	    StringBuilder localStringBuilder = new StringBuilder();
	    localStringBuilder.append(player);
	    localStringBuilder.append("|");
	    localStringBuilder.append(format.format(now));
	    localStringBuilder.append("|");
	    String admin = plugin.getUBDatabase().getAdmin(player);
	    if(admin==null||admin.equalsIgnoreCase("")) 
	    	admin = "Ultrabans";
	    localStringBuilder.append(admin);
	    localStringBuilder.append("|");
	    localStringBuilder.append("Forever");
	    localStringBuilder.append("|");
	    String reason = plugin.getUBDatabase().getBanReason(player);
	    if(reason.equalsIgnoreCase("")) 
	    	reason = "Exported from Ultrabans";
	    localStringBuilder.append(reason);
	    return localStringBuilder.toString();
	}
}
