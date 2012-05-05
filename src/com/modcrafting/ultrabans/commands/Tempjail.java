package com.modcrafting.ultrabans.commands;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Tempjail implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.tempjail";
	public Tempjail(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean autoComplete;
	public String expandName(String p) {
		int m = 0;
		String Result = "";
		for (int n = 0; n < plugin.getServer().getOnlinePlayers().length; n++) {
			String str = plugin.getServer().getOnlinePlayers()[n].getName();
			if (str.matches("(?i).*" + p + ".*")) {
				m++;
				Result = str;
				if(m==2) {
					return null;
				}
			}
			if (str.equalsIgnoreCase(p))
				return str;
		}
		if (m == 1)
			return Result;
		if (m > 1) {
			return null;
		}
		if (m < 1) {
			return p;
		}
		return p;
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean auth = false;
		boolean anon = false;
		Player player = null;
		String admin = config.getString("defAdminName", "server");
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission) || player.isOp()) auth = true;
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		if (args.length < 3) return false;

		String p = args[0]; // Get the victim's potential name
		
		if(autoComplete)
			p = expandName(p);
		Player victim = plugin.getServer().getPlayer(p);
		
		//Figured this out after the fact...... Ugh
		//Neglect to study bukkit. 
		//Screw it if it works... go with it.
		//victim = Bukkit.getOfflinePlayer(p).getPlayer();

		
		String reason = config.getString("defReason", "not sure");
		boolean broadcast = true;
		if(args.length > 3){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = combineSplit(4, args, " ");
			}else{
				if(args[1].equalsIgnoreCase("-a")){
					anon = true;
					reason = combineSplit(4, args, " ");
				}else{
				reason = combineSplit(3, args, " ");
				}
			}
		}

		if (anon){
			admin = config.getString("defAdminName", "server");
		}

		long tempTime = parseTimeSpec(args[1],args[2]);
		if(tempTime == 0)
			return false;
		long temp = System.currentTimeMillis()/1000+tempTime; //epoch time
		//Separate for Online-Offline
		if(victim != null){
			if(plugin.jailed.contains(victim.getName().toLowerCase())){
				sender.sendMessage(ChatColor.BLUE + victim.getName() +  ChatColor.GRAY + " is already jailed for " + reason);
				return true;
			}
			plugin.tempJail.put(victim.getName().toLowerCase(), temp);
			plugin.db.addPlayer(victim.getName(), reason, admin, temp, 6);
			plugin.jailed.add(p.toLowerCase());
			Location stlp = getJail();
			victim.teleport(stlp);
			log.log(Level.INFO, "[UltraBan] " + admin + " tempjailned player " + victim.getName() + ".");
			String tempjailMsgVictim = config.getString("messages.tempjailMsgVictim", "You have been temp. jailed by %admin%. Reason: %reason%!");
			tempjailMsgVictim = tempjailMsgVictim.replaceAll("%admin%", admin);
			tempjailMsgVictim = tempjailMsgVictim.replaceAll("%reason%", reason);
			if(broadcast){
				String tempjailMsgBroadcast = config.getString("messages.tempjailMsgBroadcast", "%victim% was temp. jailed by %admin%. Reason: %reason%!");
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll("%admin%", admin);
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll("%reason%", reason);
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll("%victim%", victim.getName());
				plugin.getServer().broadcastMessage(formatMessage(tempjailMsgBroadcast));
			}else{
				String tempjailMsgBroadcast = config.getString("messages.tempjailMsgBroadcast", "%victim% was temp. jailed by %admin%. Reason: %reason%!");
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll("%admin%", admin);
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll("%reason%", reason);
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll("%victim%", victim.getName());
				sender.sendMessage(formatMessage(":S:" + tempjailMsgBroadcast));
			}
		}else{
			if(plugin.jailed.contains(p.toLowerCase())){
				sender.sendMessage(ChatColor.BLUE + p +  ChatColor.GRAY + " is already jailed for " + reason);
				return true;
			}
			plugin.tempJail.put(p.toLowerCase(), temp);
			plugin.jailed.add(p.toLowerCase());
			plugin.db.addPlayer(p, reason, admin, temp, 6);
			log.log(Level.INFO, "[UltraBan] " + admin + " temp jail player " + p + ".");
			if(broadcast){
				String tempjailMsgBroadcast = config.getString("messages.tempjailMsgBroadcast", "%victim% was temp. jailed by %admin%. Reason: %reason%!");
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll("%admin%", admin);
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll("%reason%", reason);
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll("%victim%", p);
				plugin.getServer().broadcastMessage(formatMessage(tempjailMsgBroadcast));
			}else{
				String tempjailMsgBroadcast = config.getString("messages.tempjailMsgBroadcast", "%victim% was temp. jailed by %admin%. Reason: %reason%!");
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll("%admin%", admin);
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll("%reason%", reason);
				tempjailMsgBroadcast = tempjailMsgBroadcast.replaceAll("%victim%", p);
				sender.sendMessage(formatMessage(":S:" + tempjailMsgBroadcast));
			}
		}
		return true;
	}
	public String combineSplit(int startIndex, String[] string, String seperator) {
		StringBuilder builder = new StringBuilder();

		for (int i = startIndex; i < string.length; i++) {
			builder.append(string[i]);
			builder.append(seperator);
		}

		builder.deleteCharAt(builder.length() - seperator.length()); // remove
		return builder.toString();
	}
	public static long parseTimeSpec(String time, String unit) {
		long sec;
		try {
			sec = Integer.parseInt(time)*60;
		} catch (NumberFormatException ex) {
			return 0;
		}
		if (unit.startsWith("hour"))
			sec *= 60;
		else if (unit.startsWith("day"))
			sec *= (60*24);
		else if (unit.startsWith("week"))
			sec *= (7*60*24);
		else if (unit.startsWith("month"))
			sec *= (30*60*24);
		else if (unit.startsWith("min"))
			sec *= 1;
		else if (unit.startsWith("sec"))
			sec /= 60;
		return sec;
	}
	public String formatMessage(String str){
		String funnyChar = new Character((char) 167).toString();
		str = str.replaceAll("&", funnyChar);
		return str;
	}

    public Location getJail(){
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
        Location setlp = new Location(
                plugin.getServer().getWorld(config.getString("jail.world", plugin.getServer().getWorlds().get(0).getName())),
                config.getInt("jail.x", 0),
                config.getInt("jail.y", 0),
                config.getInt("jail.z", 0));
        	return setlp;
    }
}
