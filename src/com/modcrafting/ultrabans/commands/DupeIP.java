package com.modcrafting.ultrabans.commands;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class DupeIP implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.checkip";
	public DupeIP(UltraBan ultraBan) {
		this.plugin = ultraBan;
	
	}
	public boolean onCommand(final CommandSender sender, Command command, String commandLabel, final String[] args) {
		boolean auth = false;
		Player player = null;
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission) || player.isOp()) auth = true;
		}else{
			auth = true;
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}else{
		if (args.length < 1) return false;
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,new Runnable(){

			@Override
			public void run() {
				String p = args[0];

				p = plugin.util.expandName(p); 
				String ip = plugin.db.getAddress(p);
				if(ip == null){
					sender.sendMessage(ChatColor.RED + "Unable to view ip for " + p + " !");
					return;
				}
				String sip = null;
				OfflinePlayer[] pl = plugin.getServer().getOfflinePlayers();
				sender.sendMessage(ChatColor.AQUA + "Scanning Current IP of " + p + ": " + ip + " !");
				for (int i=0; i<pl.length; i++){
					sip = plugin.db.getAddress(pl[i].getName());
			        if (sip != null && sip.equalsIgnoreCase(ip)){
			        	if (!pl[i].getName().equalsIgnoreCase(p)){
				        	sender.sendMessage(ChatColor.GRAY + "Player: " + pl[i].getName() + " duplicates player: " + p + "!");
			        	}
			        }
				}
				sender.sendMessage(ChatColor.GREEN + "Scanning Complete!");
			}
		});
		return true;
		}
	}
}
