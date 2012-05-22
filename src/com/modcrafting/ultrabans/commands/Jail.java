package com.modcrafting.ultrabans.commands;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Jail implements CommandExecutor{

	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.jail";
	public World world;
	public String jworld;
    public double x;
    public int y;
    public double z;
    public int yaw;
    public int pitch;
    public Location setlp;
    public Jail(UltraBan ultraBan) {
			this.plugin = ultraBan;
		}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean auth = false;
		Player player = null;
		String admin = config.getString("defAdminName", "server");
		String reason = config.getString("defReason", "not sure");
		boolean broadcast = true;
		boolean anon = false;
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission) || player.isOp()) auth = true;
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if(auth){
			if (args.length < 1) return false;
			if(args[0].equalsIgnoreCase("setjail")){
				this.setlp = player.getLocation();
				setJail(setlp, "jail");
				sender.sendMessage(ChatColor.GRAY + "Jail has been set!");
				return true;
			}
			if(args[0].equalsIgnoreCase("setrelease")){
				this.setlp = player.getLocation();
				setJail(setlp, "release");
				sender.sendMessage(ChatColor.GRAY + "Release has been set!");
				return true;
			}
			if(args[0].equalsIgnoreCase("pardon")){
					String jaile = args[1];
					if(plugin.autoComplete) jaile = plugin.util.expandName(jaile);
					plugin.jailed.remove(jaile.toLowerCase());
					Player jailee = plugin.getServer().getPlayer(jaile);
					plugin.db.removeFromJaillist(jailee.getName());
					plugin.db.addPlayer(jailee.getName(), "Released From Jail", admin, 0, 8);
					Location stlp = getJail("release");
					if(stlp != null){
						jailee.teleport(stlp);
					}else{
						jailee.teleport(jailee.getWorld().getSpawnLocation());
					}
					if(plugin.tempJail.containsKey(jaile.toLowerCase())){
						plugin.tempJail.remove(jaile.toLowerCase());
					}
					String jailMsgRelease = config.getString("messages.jailMsgRelease", "%victim% was released from jail by %admin%!");
					jailMsgRelease = jailMsgRelease.replaceAll("%admin%", admin);
					jailMsgRelease = jailMsgRelease.replaceAll("%victim%", jaile);
					jailee.sendMessage(plugin.util.formatMessage(jailMsgRelease));
					sender.sendMessage(plugin.util.formatMessage(jailMsgRelease));
					return true;
			}
			String p = args[0];
			Player victim = plugin.getServer().getPlayer(p);
			if(plugin.autoComplete) p = plugin.util.expandName(p);
			if(args.length > 1){
				if(args[1].equalsIgnoreCase("-s")){
					broadcast = false;
					reason = plugin.util.combineSplit(2, args, " ");
				}else{
					if(args[1].equalsIgnoreCase("-a")){
						anon = true;
						reason = plugin.util.combineSplit(2, args, " ");
					}else{
					reason = plugin.util.combineSplit(1, args, " ");
					}
				}
			}
			if (anon){
				admin = config.getString("defAdminName", "server");
			}
			if(victim == null){
				if(plugin.jailed.contains(p)){
					sender.sendMessage(ChatColor.GRAY + p + " is already in jail.");
					return true;
				}else{
					sender.sendMessage(ChatColor.GRAY + p + " was not found to be jailed.");
				}
				sender.sendMessage(ChatColor.GRAY + "Player Must be online to be jailed.");
				return true;
			}else{
				if(victim.getName() == admin){
					sender.sendMessage(ChatColor.RED + "You cannot emojail yourself!");
					return true;
				}
				if(victim.hasPermission( "ultraban.override.jail")){
					sender.sendMessage(ChatColor.RED + "Your jail attempt has been denied! Player Notified!");
					victim.sendMessage(ChatColor.RED + "Player:" + admin + " Attempted to jail you!");
					return true;
				}
				if(plugin.jailed.contains(victim.getName().toLowerCase())){
					sender.sendMessage(ChatColor.GRAY + victim.getName() + " is already in jail.");
					return true;
				}else{
					sender.sendMessage(ChatColor.GRAY + victim.getName() + " was not found to be jailed.");
				}
					if(broadcast){
					String adminMsgAll = config.getString("messages.jailMsgBroadcast", "%victim% was jailed by %admin%. Reason: %reason%");
					adminMsgAll = adminMsgAll.replaceAll("%admin%", admin);
					adminMsgAll = adminMsgAll.replaceAll("%reason%", reason);
					adminMsgAll = adminMsgAll.replaceAll("%victim%", p);
					plugin.getServer().broadcastMessage(plugin.util.formatMessage(adminMsgAll));
				}else{
					String jailMsgVictim = config.getString("messages.jailMsgVictim", "You have been jailed by %admin%. Reason: %reason%!");
					jailMsgVictim = jailMsgVictim.replaceAll("%admin%", admin);
					jailMsgVictim = jailMsgVictim.replaceAll("%victim%", p);
					victim.sendMessage(plugin.util.formatMessage(jailMsgVictim));
					String adminMsgAll = config.getString("messages.jailMsgBroadcast", "%victim% was jailed by %admin%. Reason: %reason%");
					adminMsgAll = adminMsgAll.replaceAll("%admin%", admin);
					adminMsgAll = adminMsgAll.replaceAll("%reason%", reason);
					adminMsgAll = adminMsgAll.replaceAll("%victim%", p);
					sender.sendMessage(plugin.util.formatMessage(":S:" + adminMsgAll));
				}
				plugin.db.addPlayer(p, reason, admin, 0, 6);
				plugin.jailed.add(p.toLowerCase());
				Location stlp = getJail("jail");
				victim.teleport(stlp);
				return true;
			}
		}
		return false;
	}

	public void setJail(Location location, String label) {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
        config.set(label+".x", (int) setlp.getX());
        config.set(label+".y", (int) setlp.getY());
        config.set(label+".z", (int) setlp.getZ());
        config.set(label+".world", setlp.getWorld().getName());
        plugin.saveConfig();

    }
    public Location getJail(String label){
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
        Location setlp = new Location(
                plugin.getServer().getWorld(config.getString("jail.world", plugin.getServer().getWorlds().get(0).getName())),
                config.getInt(label+".x", 0),
                config.getInt(label+".y", 0),
                config.getInt(label+".z", 0));
        	return setlp;
    }
}

        
