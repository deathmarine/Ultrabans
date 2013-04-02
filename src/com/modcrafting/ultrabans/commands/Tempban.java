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
import com.modcrafting.ultrabans.util.BanInfo;
import com.modcrafting.ultrabans.util.BanType;
import com.modcrafting.ultrabans.util.Formatting;

public class Tempban extends CommandHandler {
	public Tempban(Ultrabans instance) {
		super(instance);
	}

	public String command(CommandSender sender, Command command, String[] args) {
		if (args.length < 3)
			return lang.getString("Tempban.Arguments");
		boolean broadcast = true;
		String admin = Ultrabans.DEFAULT_ADMIN;
		String reason = Ultrabans.DEFAULT_REASON;
		if (sender instanceof Player)
			admin = sender.getName();
		String name = args[0];
		name = Formatting.expandName(name);
		if(name.equalsIgnoreCase(admin))
			return lang.getString("Tempban.Emo");
		long tempTime = 0;
		String amt = new String();
		String mode = new String();
			if(args.length > 3){
				if(args[1].equalsIgnoreCase("-s")
						&&sender.hasPermission(command.getPermission()+".silent"))
					broadcast = false;
				if(args[1].equalsIgnoreCase("-a")
						&&sender.hasPermission(command.getPermission()+".anon"))
					admin = Ultrabans.DEFAULT_ADMIN;
				amt=args[2];
				mode=args[3];
				reason = Formatting.combineSplit(4, args);
				tempTime = Formatting.parseTimeSpec(amt,mode);
			}else if(args.length > 2){
				amt=args[1];
				mode=args[2];
				tempTime = Formatting.parseTimeSpec(amt, mode);
				reason = Formatting.combineSplit(3, args);
			}
		if(tempTime == 0) 
			return lang.getString("Tempban.TimeFail");
		long temp = System.currentTimeMillis()/1000+tempTime;

		if(plugin.cache.containsKey(name.toLowerCase())){
			for(BanInfo info: plugin.cache.get(name.toLowerCase())){
				if(info.getType() == BanType.TEMPBAN.getId() 
				|| info.getType() == BanType.BAN.getId()){
					String failed = lang.getString("Tempban.Failed");
					if(failed.contains(Ultrabans.VICTIM))
						failed = failed.replace(Ultrabans.VICTIM, name);
					return failed;
					
				}
			}
		}
		
		OfflinePlayer victim = plugin.getServer().getOfflinePlayer(name);
		if(victim != null){
			if(victim.isOnline()){
				if(victim.getPlayer().hasPermission("ultraban.override.tempban") &&
						!admin.equalsIgnoreCase(Ultrabans.ADMIN))
					return lang.getString("Tempban.Denied");
				String vicmsg = lang.getString("Tempban.MsgToVictim");
				if(vicmsg.contains(Ultrabans.ADMIN))
					vicmsg = vicmsg.replace(Ultrabans.ADMIN, admin);
				if(vicmsg.contains(Ultrabans.REASON)) 
					vicmsg = vicmsg.replace(Ultrabans.REASON, reason);
				victim.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&', vicmsg));
			}
			name = victim.getName();
		}
		plugin.getAPI().tempbanPlayer(name, reason, temp, admin);
		String bcmsg = ChatColor.translateAlternateColorCodes('&', lang.getString("Tempban.MsgToBroadcast"));
		if(bcmsg.contains(Ultrabans.ADMIN)) 
			bcmsg = bcmsg.replace(Ultrabans.ADMIN, admin);
		if(bcmsg.contains(Ultrabans.REASON)) 
			bcmsg = bcmsg.replace(Ultrabans.REASON, reason);
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
}
