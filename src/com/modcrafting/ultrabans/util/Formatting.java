/* COPYRIGHT (c) 2015 Deathmarine
 * This file is part of Ultrabans.
 * Ultrabans is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Ultrabans is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Ultrabans.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.modcrafting.ultrabans.util;

import java.io.File;
import java.util.HashSet;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.modcrafting.ultrabans.Ultrabans;

public class Formatting {
	public static Pattern ipv4 = Pattern
			.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

	public static String expandName(String p) {
		HashSet<String> set = new HashSet<String>();
		for (int n = 0; n < Bukkit.getOfflinePlayers().length; n++) {
			String name = Bukkit.getOfflinePlayers()[n].getName();
			if (name.equalsIgnoreCase(p))
				return name;
			if (containsIgnoreCase(name, p)) {
				set.add(name);
			}
		}
		if (set.size() == 1) {
			return (String) set.toArray()[0];
		}
		return p;
	}

	private static boolean containsIgnoreCase(String search, String entry) {
		return search.toLowerCase().contains(entry.toLowerCase());
	}

	public static String combineSplit(int startIndex, String[] string) {
		StringBuilder builder = new StringBuilder();
		if (string.length >= 1) {
			for (int i = startIndex; i < string.length; i++) {
				builder.append(string[i]);
				builder.append(" ");
			}

			if (builder.length() > 1) {
				builder.deleteCharAt(builder.length() - 1);
				return builder.toString();
			}
		}
		return Ultrabans.DEFAULT_REASON;
	}

	/**
	 * @deprecated Using InetAddress.getByName(String) allows for acceptance of IPv4/IPv6/HostName 
	 * <br>Catching an exception will validate the address
	 * @param ip
	 * @return
	 */
	@Deprecated
	public static boolean validateIP(String ip) {
		if (ip == null || ip.isEmpty())
			return false;
		ip = ip.trim();
		try {
			return ipv4.matcher(ip).matches();// || ipv6.matcher(ip).matches();
		} catch (PatternSyntaxException ex) {
			return false;
		}
	}

	public static long parseTimeSpec(String time, String unit)
			throws NumberFormatException {
		long sec = Integer.parseInt(time) * 60;
		if (unit.toLowerCase().startsWith(
				Ultrabans.getPlugin().getConfig().getString("Mode.Hour", "h")
						.substring(0, 1).toLowerCase())) {
			sec *= 60;
		} else if (unit.toLowerCase().startsWith(
				Ultrabans.getPlugin().getConfig().getString("Mode.Day", "d")
						.substring(0, 1).toLowerCase())) {
			sec *= (60 * 24);
		} else if (unit.toLowerCase().startsWith(
				Ultrabans.getPlugin().getConfig().getString("Mode.Week", "w")
						.substring(0, 1).toLowerCase())) {
			sec *= (7 * 60 * 24);
		} else if (unit.toLowerCase().startsWith(
				Ultrabans.getPlugin().getConfig().getString("Mode.Month", "mo")
						.substring(0, 2).toLowerCase())) {
			sec *= (30 * 60 * 24);
		} else if (unit.toLowerCase().startsWith(
				Ultrabans.getPlugin().getConfig().getString("Mode.Minute", "m")
						.substring(0, 1).toLowerCase())) {
			sec *= 1;
		} else if (unit.toLowerCase().startsWith(
				Ultrabans.getPlugin().getConfig().getString("Mode.Second", "s")
						.substring(0, 1).toLowerCase())) {
			sec /= 60;
		}
		return sec;
	}
	
	/**
	 * Returns a comma delimited string from the array
	 * @param array
	 * @return
	 */
	public static String collapseArray(String[] array){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<array.length;i++){
			sb.append(array[i]).append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	
	public static void deletePlyrdat(String name) {
		@SuppressWarnings("deprecation")
		OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(name);
		if (player != null && !player.isOnline()) {
			for(World world : Bukkit.getServer().getWorlds()){
				new File(world.getName() + "/playerdata/", player.getUniqueId().toString() + ".dat").delete();
			}
		}
	}
	
	public static void deletePlayerdat(UUID uuid) {
		OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(uuid);
		if (player != null && !player.isOnline()) {
			for(World world : Bukkit.getServer().getWorlds()){
				new File(world.getName() + "/playerdata/", uuid.toString() + ".dat").delete();
			}
		}
	}
	
}
