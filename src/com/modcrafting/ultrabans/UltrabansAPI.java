/* COPYRIGHT (c) 2013 Deathmarine (Joshua McCurry)
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
package com.modcrafting.ultrabans;

import java.util.ArrayList;
import java.util.List;

import com.modcrafting.ultrabans.util.BanInfo;
import com.modcrafting.ultrabans.util.BanType;

public class UltrabansAPI {
	Ultrabans plugin;
	public UltrabansAPI(Ultrabans instance){
		plugin = instance;
	}
	public void addPlayer(final String player_name, final String reason, final String admin, final long time, final BanType type){
		plugin.getServer().getScheduler().runTaskAsynchronously(Ultrabans.getPlugin(),new Runnable(){
			@Override
			public void run() {
				plugin.getUBDatabase().addPlayer(player_name, reason, admin, time, type.getId());
			}	
		});
	}
	
	public void banPlayer(final String player_name, final String reason, final String admin){
		List<BanInfo> list = new ArrayList<BanInfo>();
		if(plugin.cache.containsKey(player_name))
			list = plugin.cache.get(player_name);
				list.add(new BanInfo(player_name, reason, admin, 0, BanType.BAN.getId()));
		plugin.cache.put(player_name.toLowerCase(), list);
		addPlayer(player_name, reason, admin, 0, BanType.BAN);
	}
	
	public void ipbanPlayer(final String player_name, final String ip, final String reason, final String admin){
		BanInfo info = new BanInfo(player_name, reason, admin, 0, BanType.IPBAN.getId());
		List<BanInfo> list = new ArrayList<BanInfo>();
		if(plugin.cacheIP.containsKey(player_name))
			list = plugin.cache.get(player_name);
		if(plugin.cacheIP.containsKey(player_name))
			list = plugin.cache.get(player_name);
		list.add(info);
		plugin.cache.put(player_name.toLowerCase(), list);
		plugin.cacheIP.put(ip, list);
		addPlayer(player_name, reason, admin, 0, BanType.IPBAN);
	}
	
	public void jailPlayer(final String player_name, final String reason, final String admin){
		List<BanInfo> list = new ArrayList<BanInfo>();
		if(plugin.cache.containsKey(player_name))
			list = plugin.cache.get(player_name);
		list.add(new BanInfo(player_name, reason, admin, 0, BanType.JAIL.getId()));
		plugin.cache.put(player_name.toLowerCase(), list);
		addPlayer(player_name, reason, admin, 0, BanType.JAIL);
	}
	
	public void warnPlayer(String player_name, String reason, String admin) {
		addPlayer(player_name, reason, admin, 0, BanType.WARN);
	}
	
	public void pardonPlayer(final String player_name, final String admin){
		plugin.getServer().getScheduler().runTaskAsynchronously(Ultrabans.getPlugin(),new Runnable(){
			@Override
			public void run() {
				plugin.getUBDatabase().removeFromJaillist(player_name);
			}	
		});
		addPlayer(player_name, "Released From Jail", admin, 0, BanType.INFO);
	}
	
	public void mutePlayer(final String player_name, final String reason, final String admin){
		plugin.muted.add(player_name.toLowerCase());
		addPlayer(player_name, reason, admin, 0, BanType.MUTE);
	}
	
	public void kickPlayer(final String player_name, final String reason, final String admin){
		addPlayer(player_name, reason, admin, 0, BanType.KICK);
	}
	public void permabanPlayer(final String player_name, final String reason, final String admin){
		List<BanInfo> list = new ArrayList<BanInfo>();
		if(plugin.cache.containsKey(player_name))
			list = plugin.cache.get(player_name);
		list.add(new BanInfo(player_name, reason, admin, 0, BanType.PERMA.getId()));
		plugin.cache.put(player_name.toLowerCase(), list);
		addPlayer(player_name, reason, admin, 0, BanType.PERMA);
	}

	public void tempbanPlayer(String player_name, String reason, long temp, String admin) {
		List<BanInfo> list = new ArrayList<BanInfo>();
		if(plugin.cache.containsKey(player_name))
			list = plugin.cache.get(player_name);
		list.add(new BanInfo(player_name, reason, admin, 0, BanType.TEMPBAN.getId()));
		plugin.cache.put(player_name.toLowerCase(), list);
		addPlayer(player_name, reason, admin, temp, BanType.TEMPBAN);
	}

	public void tempipbanPlayer(String player_name, String ip, String reason, long temp, String admin) {
		BanInfo info = new BanInfo(player_name, reason, admin, 0, BanType.TEMPIPBAN.getId());
		List<BanInfo> list = new ArrayList<BanInfo>();
		if(plugin.cacheIP.containsKey(player_name))
			list = plugin.cache.get(player_name);
		if(plugin.cacheIP.containsKey(player_name))
			list = plugin.cache.get(player_name);
		list.add(info);
		plugin.cache.put(player_name.toLowerCase(), list);
		plugin.cacheIP.put(ip, list);
		addPlayer(player_name, reason, admin, temp, BanType.TEMPIPBAN);
	}

	public void tempjailPlayer(String player_name, String reason, long temp, String admin) {
		List<BanInfo> list = new ArrayList<BanInfo>();
		if(plugin.cache.containsKey(player_name))
			list = plugin.cache.get(player_name);
		list.add(new BanInfo(player_name, reason, admin, 0, BanType.TEMPJAIL.getId()));
		addPlayer(player_name, reason, admin, temp, BanType.TEMPJAIL);
	}
	
	public void clearWarn(final String player_name){
		plugin.getServer().getScheduler().runTaskAsynchronously(Ultrabans.getPlugin(),new Runnable(){
			@Override
			public void run() {
				plugin.getUBDatabase().clearWarns(player_name);
			}	
		});
	}
}
