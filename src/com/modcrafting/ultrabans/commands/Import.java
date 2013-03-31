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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.modcrafting.ultrabans.Ultrabans;

public class Import extends CommandHandler{
	public Import(Ultrabans instance) {
		super(instance);
	}

	public String command(final CommandSender sender, Command command, String[] args) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getString("Import.Loading")));
		try {
			BufferedReader banlist = new BufferedReader(new FileReader("banned-players.txt"));
			String p;
			while ((p = banlist.readLine()) != null){
				if(!p.contains("#")&&p.length()>0){
					g(p);
				}
			}
			banlist.close();
			BufferedReader bannedIP = new BufferedReader(new FileReader("banned-ips.txt"));
			String ip;
			while ((ip = bannedIP.readLine()) != null){
				if(ip!=null&&!ip.contains("#")&&ip.length()>0){
				    String[] args1 = ip.split("\\|");
				    String name = args1[0].trim();
					if(!plugin.bannedIPs.containsKey(name)) 
						plugin.bannedIPs.put(name, Long.MIN_VALUE);
					String cknullIP = plugin.getUBDatabase().getName(name);
					if (cknullIP != null){
						plugin.getUBDatabase().addPlayer(plugin.getUBDatabase().getName(name), "imported", args1[2].trim(), 0, 1);
					}else{
						plugin.getUBDatabase().setAddress("import", name);
						plugin.getUBDatabase().addPlayer("import", "imported", args1[2].trim(), 0, 1);
					}
				}
			}
			bannedIP.close();
		}catch(IOException e){
			String msg = ChatColor.translateAlternateColorCodes('&', lang.getString("Import.Failed"));
			sender.sendMessage(msg);
			if(plugin.getLog())
				plugin.getLogger().severe(ChatColor.stripColor(msg));
		}
		String msg = ChatColor.translateAlternateColorCodes('&', lang.getString("Import.Completed"));
		if(plugin.getLog())
			plugin.getLogger().severe(ChatColor.stripColor(msg));
		return msg;
	}

	public void g(String line) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
	    String[] args = line.split("\\|");
	    if(args.length<4) return;
	    String name = args[0].trim();
	    long temp = 0;
	    int type = 0;
	    try {
		    if(!args[3].trim().equalsIgnoreCase("Forever")){
		    	temp=format.parse(args[3].trim()).getTime();
		    }
		} catch (ParseException e) {
			e.printStackTrace();
		}
	    String admin = args[2].trim();
	    String reason = args[4];
	    plugin.bannedPlayers.put(name, Long.MIN_VALUE);
		plugin.getUBDatabase().addPlayer(name, reason, admin, temp, type);
	}
}
