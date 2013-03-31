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

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.Formatting;

public class Pardon extends CommandHandler{
	public Pardon(Ultrabans instance) {
		super(instance);
	}
	
	public String command(CommandSender sender, Command command, String[] args) {
		if (args.length < 1) 
			return lang.getString("Pardon.Arguments");
		String admin = Ultrabans.DEFAULT_ADMIN;
		if (sender instanceof Player)
			admin = sender.getName();
		String name = Formatting.expandName(args[0]);
		if(plugin.jailed.containsKey(name.toLowerCase())){
			if(plugin.jailed.containsKey(name.toLowerCase()))
				plugin.jailed.remove(name.toLowerCase());
			String bcmsg = lang.getString("Pardon.Msg");
			if(bcmsg.contains(Ultrabans.ADMIN)) 
				bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
			if(bcmsg.contains(Ultrabans.VICTIM)) 
				bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, name);
			plugin.getAPI().pardonPlayer(name, admin);
			Player victim = plugin.getServer().getPlayer(name);
			if(victim != null){
				Location stlp = plugin.jail.getJail("release");
				if(stlp != null){
					victim.teleport(stlp);
				}else{
					victim.teleport(victim.getBedSpawnLocation());
				}
				victim.sendMessage(bcmsg);
			}
			return bcmsg;
		}
		return lang.getString("Pardon.Failed");
	}
}
