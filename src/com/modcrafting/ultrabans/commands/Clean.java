package com.modcrafting.ultrabans.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.tracker.Track;

public class Clean implements CommandExecutor{
	Ultrabans plugin;
	public Clean(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
		Track.track(command.getName());
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+plugin.perms);
			return true;
		}
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
			int count = 0;
			@Override
			public void run() {
				List<String> list = plugin.db.getBans();
				for(String name:list){
					if(plugin.data.deletePlyrdat(name)) count++;
				}
				String msg = plugin.getConfig().getString("Messages.Clean.Complete","Deleted %amt% player.dat files.");
				if(msg.contains(plugin.regexAmt)) msg=msg.replaceAll(plugin.regexAmt, String.valueOf(count));
				msg=plugin.util.formatMessage(msg);
				sender.sendMessage(msg);
			}
		});
		return true;
	}

}
