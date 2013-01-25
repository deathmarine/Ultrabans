/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.modcrafting.ultrabans.Ultrabans;

public class Help implements CommandExecutor{
	Ultrabans plugin;
	public Help(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	@SuppressWarnings("unused")
	private static String unused = "Best Help System Evhar!";
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		int page = 1;
		if(args.length>0){
			try{
				page = Integer.parseInt(args[0]);
			}catch(NumberFormatException nfe){
				page = 1;
			}
		}
		plugin.getServer().dispatchCommand(sender, "help Ultrabans "+page);
		return true;
	}

}
