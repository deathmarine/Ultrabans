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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.Formatting;

public class Unban extends CommandHandler {
	public Unban(Ultrabans instance) {
		super(instance);
	}

	public String command(CommandSender sender, Command command, String[] args) {
		if (args.length < 1)
			return lang.getString("Ban.Arguments");
		boolean broadcast = true;
		String admin = Ultrabans.DEFAULT_ADMIN;
		String reason;
		if (sender instanceof Player)
			admin = sender.getName();
		String name = Formatting.expandName(args[0]);
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("-s")){
				if(	sender.hasPermission(command.getPermission() + ".silent"))
					broadcast = false;
				reason = Formatting.combineSplit(2, args);	
			} else if (args[1].equalsIgnoreCase("-a")){
				if(sender.hasPermission(command.getPermission() + ".anon"))
					admin = Ultrabans.DEFAULT_ADMIN;
				reason = Formatting.combineSplit(2, args);	
			} else {
				reason = Formatting.combineSplit(1, args);
			}
		}
		OfflinePlayer of = plugin.getServer().getOfflinePlayer(name);
		if(of!=null){
			if(of.isBanned()) of.setBanned(false);
			name = of.getName();
		}
		//unban IPv4
		if(Formatting.validIP(name)){
			if(plugin.bannedIPs.containsKey(name)){
				plugin.bannedIPs.remove(name);
			}
			String pname = plugin.getUBDatabase().getName(name);
			if (pname != null){
				of = plugin.getServer().getOfflinePlayer(pname);
				name = of.getName();						
				reason = plugin.getUBDatabase().getBanReason(name);
				
				plugin.getUBDatabase().removeFromBanlist(name);
				plugin.getUBDatabase().addPlayer(name, "Unbanned: " + reason, admin, 0, 5);
				plugin.getLogger().info(admin + " unbanned player " + name + ".");				
			}else{
				plugin.getUBDatabase().removeFromBanlist(pname);			
			}
			String bcmsg = lang.getString("Unban.MsgToBroadcast", "%victim% was unbanned by %admin%!");
			bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
			bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, name);
			plugin.getServer().broadcastMessage(bcmsg);
			return null;
		}
				
		if(plugin.getUBDatabase().permaBan(name.toLowerCase())){
			String perma = config.getString("Messages.Unban.PermaBanned");
			perma = perma.replaceAll(Ultrabans.VICTIM, name);
			if(plugin.getLog())
				plugin.getLogger().info(perma);
			return perma;
		}
		if(plugin.bannedPlayers.containsKey(name.toLowerCase())){
			if(config.getBoolean("UnbansLog.Enable", true)){
				reason = plugin.getUBDatabase().getBanReason(name);
				if(config.getBoolean("UnbansLog.LogReason",true) && reason != null){
					plugin.getUBDatabase().addPlayer(name, "Unbanned: "+reason, admin, 0, 5);
				}else{
					plugin.getUBDatabase().addPlayer(name, "Unbanned", admin, 0, 5);
				}
			}
			plugin.bannedPlayers.remove(name.toLowerCase());
			plugin.getUBDatabase().removeFromBanlist(name);
			String ip = plugin.getUBDatabase().getAddress(name);
			if(ip != null && plugin.bannedIPs.containsKey(ip)){
				plugin.bannedIPs.remove(ip);
				if(plugin.getLog())
					plugin.getLogger().info("Also removed the IP ban!");
			}
			
			String bcmsg = ChatColor.translateAlternateColorCodes('&', lang.getString("Unban.MsgToBroadcast"));
			if(bcmsg.contains(Ultrabans.ADMIN)) 
				bcmsg = bcmsg.replace(Ultrabans.ADMIN, admin);
			if(bcmsg.contains(Ultrabans.VICTIM)) 
				bcmsg = bcmsg.replace(Ultrabans.VICTIM, name);
			if(broadcast){
				plugin.getServer().broadcastMessage(bcmsg);
			}else{
				sender.sendMessage(ChatColor.ITALIC + "Silent: " + bcmsg);
			}
			if(plugin.getLog())
				plugin.getLogger().info(ChatColor.stripColor(bcmsg));
			return null;
		}
		String failed = config.getString("Messages.Unban.Failed", "%victim% is already unbanned!");
		failed = failed.replaceAll(Ultrabans.VICTIM, name);
		return failed;
	}
}
