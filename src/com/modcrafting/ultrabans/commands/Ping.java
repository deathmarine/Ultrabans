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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.Ultrabans;

public class Ping extends CommandHandler{
	public Ping(Ultrabans instance) {
		super(instance);
	}

	public String command(final CommandSender sender, Command command, String[] args) {
		if(args.length>0){
			Player p = plugin.getServer().getPlayer(args[0]);
			if(p!=null){
				String ping = null;
	            for(Method meth:p.getClass().getMethods()){
	            	if(meth.getName().equals("getHandle")){
						try {
							Object obj = meth.invoke(p, (Object[]) null);
		            		for(Field field:obj.getClass().getFields())
		            			if(field.getName().equals("ping"))
		            				ping = String.valueOf(field.getInt(obj));
						} catch (Exception e) {
							e.printStackTrace();
						}
	            	}
	            }
	            String msg = lang.getString("Ping.Personal");
	            if(msg.contains(Ultrabans.VICTIM))
	            	msg.replace(Ultrabans.VICTIM, p.getName());
	            if(msg.contains(Ultrabans.AMOUNT))
	            	msg.replace(Ultrabans.AMOUNT, String.valueOf(ping));
				return msg;
			}
			return lang.getString("Ping.Failed");
		}
		if(sender instanceof Player){
			Player p = (Player) sender;
			int ping = 0;
            for(Method meth:p.getClass().getMethods()){
            	if(meth.getName().equals("getHandle")){
					try {
						Object obj = meth.invoke(p, (Object[]) null);
	            		for(Field field:obj.getClass().getFields())
	            			if(field.getName().equals("ping"))
	            				ping = field.getInt(obj);
					} catch (Exception e) {
						e.printStackTrace();
					}
            	}    	
            }
            String msg = lang.getString("Ping.Personal");
            if(msg.contains(Ultrabans.AMOUNT))
            	msg.replace(Ultrabans.AMOUNT, String.valueOf(ping));
			return msg;
		}
		return lang.getString("Ping.Failed");
	}
}
