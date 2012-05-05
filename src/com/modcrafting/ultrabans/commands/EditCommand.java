package com.modcrafting.ultrabans.commands;


import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class EditCommand implements CommandExecutor{
	UltraBan plugin;
	String permission = "ultraban.editban";
	public EditCommand(UltraBan ultraBan){
		this.plugin = ultraBan;
	}

	private String banType(int num){
		switch(num){
		case 0: return "B";
		case 1: return "IP";
		case 2: return "W";
		case 3: return "K";
		case 4: return "F";
		case 5: return "UN";
		case 6: return "J";
		case 7: return "M";
		case 8: return "UNJ";
		case 9: return "PB";
		default: return "?";
		}
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player)
			if(!((Player) sender).hasPermission(permission) || !((Player) sender).isOp()){
				return true;
			}
		if(args.length < 1)
			return false;

		if(args[0].equalsIgnoreCase("help")){
			if(args.length < 2){
			sender.sendMessage(ChatColor.BLUE + "Ultraban - Edit Bans Help");
			sender.sendMessage(ChatColor.GRAY + "-----------------------------------------------");
			sender.sendMessage(ChatColor.BLUE + "Required Info {}" + ChatColor.GREEN + " Optional ()");
			sender.sendMessage(ChatColor.GRAY + "/editban <list/load/id/save/view/reason/time/cancel/help>");
			sender.sendMessage(ChatColor.GRAY + "     list {player}");
			sender.sendMessage(ChatColor.GRAY + "     load {player}");
			sender.sendMessage(ChatColor.GRAY + "     id {banid}");
			sender.sendMessage(ChatColor.GRAY + "     save");
			sender.sendMessage(ChatColor.GRAY + "     view");
			sender.sendMessage(ChatColor.GRAY + "     reason {add/set/show} (text)");
			sender.sendMessage(ChatColor.GRAY + "     time {add/set} {time} (format)");
			sender.sendMessage(ChatColor.GRAY + "     cancel");
			sender.sendMessage(ChatColor.GRAY + "     help");
			return true;
			}
		}
		if(args[0].equalsIgnoreCase("list")){
			if(args.length < 2){
				sender.sendMessage(ChatColor.RED + "Usage: list <player>");
				return true;
			}
			List<EditBan> bans = plugin.db.listRecords(args[1], sender);
			if(bans.isEmpty()){
				sender.sendMessage(ChatColor.RED + "No records");
				return true;
			}
			sender.sendMessage(ChatColor.GOLD + "Found " + bans.size() + " records for user "+bans.get(0).name+":");
			for(EditBan ban : bans){
				sender.sendMessage(ChatColor.AQUA + banType(ban.type) + ChatColor.YELLOW + ban.id + ": " + ChatColor.GREEN + ban.reason + ChatColor.YELLOW +" by " + ban.admin);
			}
			return true;
		}

		if(args[0].equalsIgnoreCase("load")){
			EditBan ban = plugin.banEditors.get(sender.getName());
			if(ban != null){
				sender.sendMessage(ChatColor.RED + "Finish what you're doing first!");
				return true;
			}

			if(args.length < 2){
				sender.sendMessage(ChatColor.RED + "Usage: load <player>");
				return true;
			}

			EditBan eb = plugin.db.loadFullRecord(args[1]);
			if(eb == null){
				sender.sendMessage(ChatColor.RED + "Unable to find the last ban/warn of this player");
				return true;
			}
			plugin.banEditors.put(sender.getName(), eb);
			sender.sendMessage(ChatColor.GREEN + "Editing the last ban/warn of player " + eb.name + ": ");
			showBanInfo(eb, sender);
			return true;

		}
		if(args[0].equalsIgnoreCase("id")){
			EditBan ban = plugin.banEditors.get(sender.getName());
			if(ban != null){
				sender.sendMessage(ChatColor.RED + "Finish what you're doing first!");
				return true;
			}

			if(args.length < 2){
				sender.sendMessage(ChatColor.RED + "Usage: load <ban id>");
				return true;
			}

			int id = Integer.parseInt(args[1]);
			if(id == 0){
				sender.sendMessage(ChatColor.RED + "ID has to be a number!");
				return true;
			}

			EditBan eb = plugin.db.loadFullRecordFromId(id);
			if(eb == null){
				sender.sendMessage(ChatColor.RED + "Unable to find a ban of this player");
				return true;
			}
			plugin.banEditors.put(sender.getName(), eb);
			sender.sendMessage(ChatColor.GREEN + "Editing the last ban/warn of player " + eb.name + ": ");
			showBanInfo(eb, sender);
			return true;
		}

		EditBan ban = plugin.banEditors.get(sender.getName());
		if(ban == null){
			sender.sendMessage(ChatColor.RED + "You aren't editing a ban");
			return true;
		}

		if(args[0].equalsIgnoreCase("save")){
			plugin.db.saveFullRecord(ban);
			plugin.banEditors.remove(sender.getName());
			sender.sendMessage(ChatColor.GREEN + "Saved ban!");
			return true;
		}

		if(args[0].equalsIgnoreCase("cancel")){
			plugin.banEditors.remove(sender.getName());
			sender.sendMessage(ChatColor.YELLOW + "Cancelled.");
			return true;
		}

		if(args[0].equalsIgnoreCase("view")){
			showBanInfo(ban, sender);
			return true;
		}

		if(args[0].equalsIgnoreCase("reason")){
			if(args.length < 2){
				sender.sendMessage(ChatColor.RED + "Usage: reason <add/set/show> (text)");
				return true;
			}

			if(args[1].equalsIgnoreCase("add")){
				if(args.length < 3){
					sender.sendMessage(ChatColor.RED + "Usage: reason add <text>");
					return true;
				}
				ban.reason += " " + combineSplit(2, args, " ");
				return true;
			}

			if(args[1].equalsIgnoreCase("set")){
				if(args.length < 3){
					sender.sendMessage(ChatColor.RED + "Usage: reason set <text>");
					return true;
				}
				ban.reason = combineSplit(2, args, " ");
				return true;
			}
		}

		if(args[0].equalsIgnoreCase("time")){
			if(args.length < 4){
				sender.sendMessage(ChatColor.RED + "Usage: time <add/set> <time> <format>");
				return true;
			}

			long time = parseTimeSpec(args[2], args[3]);
			if(time == 0){
				sender.sendMessage(ChatColor.RED + "Invalid time format");
				return true;
			}

			if(args[1].equalsIgnoreCase("add")){
				ban.endTime += time;
				return true;
			}
			if(args[1].equalsIgnoreCase("set")){

				ban.endTime = ban.time+time;
				return true;
			}
		}
		
		return false;		

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

	private void showBanInfo(EditBan eb, CommandSender sender){
		DateFormat shortTime = DateFormat.getDateTimeInstance();
		sender.sendMessage(ChatColor.GOLD+" | " + ChatColor.WHITE + eb.name +ChatColor.YELLOW+ " was banned by " + ChatColor.WHITE + eb.admin + ChatColor.YELLOW + " at " + shortTime.format((new Date(eb.time*1000))));
		if(eb.endTime > 0)
			sender.sendMessage(ChatColor.GOLD+" | "+ChatColor.YELLOW+"Will be unbanned at " + shortTime.format((new Date(eb.endTime*1000))));
		sender.sendMessage(ChatColor.GOLD+" | "+ChatColor.YELLOW+"Reason: " + ChatColor.GRAY + eb.reason);
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
}
