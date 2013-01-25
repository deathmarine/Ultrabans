package com.modcrafting.ultrabans.commands;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.Ultrabans;

public class Ping implements CommandExecutor{
	Ultrabans plugin;
	public Ping(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+Ultrabans.DEFAULT_DENY_MESSAGE);
			return true;
		}
		if(args.length>0){
			Player p = plugin.getServer().getPlayer(args[0]);
			if(p!=null){
				String ping = null;
	            for(Method meth:p.getClass().getMethods()){
	            	if(meth.getName().equals("getHandle")){
						try {
							Object obj = meth.invoke(p, (Object[]) null);
		            		for(Field field:obj.getClass().getFields()){
		            			if(field.getName().equals("ping")){
		            				ping = String.valueOf(field.getInt(obj));
		            			}
		            		}
						} catch (Exception e) {
							e.printStackTrace();
						}
	            	}
	            	
	            }
				sender.sendMessage(ChatColor.GRAY+p.getName()+"'s ping is: "+ChatColor.GOLD+ping+"ms");
			}else{
				sender.sendMessage(ChatColor.RED+"Player not found.");
			}
			return true;
		}
		if(sender instanceof Player){
			Player p = (Player) sender;
			String ping = null;
            for(Method meth:p.getClass().getMethods()){
            	if(meth.getName().equals("getHandle")){
					try {
						Object obj = meth.invoke(p, (Object[]) null);
	            		for(Field field:obj.getClass().getFields()){
	            			if(field.getName().equals("ping")){
	            				ping = String.valueOf(field.getInt(obj));
	            			}
	            		}
					} catch (Exception e) {
						e.printStackTrace();
					}
            	}
            	
            }
			sender.sendMessage(ChatColor.GRAY+"Your ping is: "+ChatColor.GOLD+ping+"ms");
		}else{
			sender.sendMessage(ChatColor.GRAY+"Your ping is: "+ChatColor.GOLD+"0ms");
		}
		return true;
	}
}
