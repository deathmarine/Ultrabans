/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

import com.modcrafting.ultrabans.Ultrabans;

public class Formatting {
	public static String expandName(String p) {
		int m = 0;
		String Result = "";
		for (int n = 0; n < Bukkit.getOnlinePlayers().length; n++) {
			String str = Bukkit.getOnlinePlayers()[n].getName();
			if (StringUtils.containsIgnoreCase(str, p)) {
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
	public static String combineSplit(int startIndex, String[] string, String seperator) {
		StringBuilder builder = new StringBuilder();
		if(string.length >= 1){
			for (int i = startIndex; i < string.length; i++) {
				builder.append(string[i]);
				builder.append(seperator);
			}

			if(builder.length() > seperator.length()){
				builder.deleteCharAt(builder.length() - seperator.length()); // remove
				return builder.toString();
			}
		}
		return Ultrabans.DEFAULT_REASON;
	}
	public static String formatMessage(String str){
		String funnyChar = new Character((char) 167).toString();
		str = str.replaceAll("&", funnyChar);
		return str;
	}
	public static String banType(int num){
		switch(num){
		case 0: return "B";
		case 1: return "IP";
		case 2: return "W";
		case 3: return "K";
		case 4: return "F";
		case 5: return "UN";
		case 6: return "J";
		case 7: return "M";
		case 9: return "PB";
		default: return "?";
		}
	}
	public static boolean validIP(String ip) {
	    if (ip == null || ip.isEmpty()) return false;
	    ip = ip.trim();
	    if ((ip.length() < 6) & (ip.length() > 15)) return false;

	    try {
	        Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
	        Matcher matcher = pattern.matcher(ip);
	        return matcher.matches();
	    } catch (PatternSyntaxException ex) {
	        return false;
	    }
	}

	public static long parseTimeSpec(String time, String unit) {
		long sec;
		try {
			sec = Integer.parseInt(time)*60;
		} catch (NumberFormatException ex) {
			return 0;
		}
		if (unit.startsWith("h")||unit.startsWith("H")){
			sec *= 60;
		}else if(unit.startsWith("d")||unit.startsWith("D")){
			sec *= (60*24);
		}else if(unit.startsWith("w")||unit.startsWith("W")){
			sec *= (7*60*24);
		}else if(unit.startsWith("mo")||unit.startsWith("Mo")){
			sec *= (30*60*24);
		}else if(unit.startsWith("m")||unit.startsWith("M")){
			sec *= 1;
		}else if(unit.startsWith("s")||unit.startsWith("S")){
			sec /= 60;
		}
		return sec;
	}
}
