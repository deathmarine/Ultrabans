package com.modcrafting.ultrabans.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.modcrafting.ultrabans.Ultrabans;

public class Clean implements CommandExecutor{
	Ultrabans plugin;
	public Clean(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
		if(sender.hasPermission(command.getPermission())){
			final long time = System.currentTimeMillis();

			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
				int count = 0;
				@Override
				public void run() {
					List<String> list = plugin.db.getBans();
					int size = list.size();
					for(String name:list){
						if(plugin.data.deletePlyrdat(name)) count++;
					}
					long dif = System.currentTimeMillis()-time;
					sender.sendMessage(ChatColor.AQUA+"Ultrabans found "+ChatColor.BLUE+String.valueOf(size)+ChatColor.AQUA+" Ban(s)");
					sender.sendMessage(ChatColor.AQUA+"Deleted "+ChatColor.BLUE+String.valueOf(count)+ChatColor.AQUA+" Player.dat files in "+ChatColor.BLUE+String.valueOf(dif)+"ms.");
					
				}
				
			});
		}
		return true;
	}

}
