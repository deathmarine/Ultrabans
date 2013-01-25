package com.modcrafting.ultrabans.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.Formatting;

public class Clean implements CommandExecutor{
	Ultrabans plugin;
	public Clean(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+Ultrabans.DEFAULT_DENY_MESSAGE);
			return true;
		}
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			int count = 0;
			@Override
			public void run() {
				List<String> list = plugin.getUBDatabase().getBans();
				for(String name:list){
					if(plugin.data.deletePlyrdat(name)) count++;
				}
				String msg = plugin.getConfig().getString("Messages.Clean.Complete","Deleted %amt% player.dat files.");
				if(msg.contains(Ultrabans.AMOUNT)) msg=msg.replaceAll(Ultrabans.AMOUNT, String.valueOf(count));
				msg=Formatting.formatMessage(msg);
				sender.sendMessage(msg);
			}
		});
		return true;
	}

}
