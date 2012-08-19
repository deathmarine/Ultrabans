/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.modcrafting.ultrabans.UltraBan;

public class Help implements CommandExecutor{
	UltraBan plugin;
	final int constant = 5;
	public Help(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		int page = 1;
		if(args.length>0){
			try{
				page = Integer.parseInt(args[0]);
			}catch(NumberFormatException nfe){
				page = 1;
			}
		}
		sender.sendMessage(ChatColor.GRAY + "Ultrabans " + ChatColor.BLUE + "Required Info {}" + ChatColor.GREEN + " Optional ()" + ChatColor.RED + " Silent -s");
		for(int i=0;i<constant;i++){
			if(plugin.getDescription().getCommands().size()>i+(page*constant)){
				String cmd = plugin.getDescription().getCommands().keySet().toArray()[i+(page*constant)].toString();
				String usage = (String) plugin.getDescription().getCommands().get(cmd).get("usage");
				if(usage.contains("<command>")) usage = usage.replace("<command>", cmd);
				if(sender.hasPermission("ultraban."+cmd)){
					sender.sendMessage(ChatColor.GOLD+usage);
					sender.sendMessage(ChatColor.GRAY+(String) plugin.getDescription().getCommands().get(cmd).get("description"));
				}
			}
		}
		sender.sendMessage(ChatColor.GRAY+"----"+ChatColor.GOLD+"Page "+String.valueOf(page)+" of "+String.valueOf(Math.round(plugin.getDescription().getCommands().size()/constant))+ChatColor.GRAY+"----");
		return true;
	}

}
