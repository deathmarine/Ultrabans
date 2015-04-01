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
package com.modcrafting.ultrabans;

import java.util.ArrayList;
import java.util.List;

import com.modcrafting.ultrabans.util.InfoBan;
import com.modcrafting.ultrabans.util.BanType;

public class UltrabansAPI {
	Ultrabans plugin;
	public UltrabansAPI(Ultrabans instance){
		plugin = instance;
	}
	
	public void addPlayer(final String uuid, final String reason, final String admin, final long time, final BanType type){
		plugin.getServer().getScheduler().runTaskAsynchronously(Ultrabans.getPlugin(),new Runnable(){
			@Override
			public void run() {
				plugin.getUBDatabase().addPlayer(uuid, reason, admin, time, type.getId());
			}	
		});
	}
	
	public void banPlayer(final String uuid, final String reason, final String admin){
		List<InfoBan> list = new ArrayList<InfoBan>();
		if(plugin.cache.containsKey(uuid))
			list = plugin.cache.get(uuid);
				list.add(new InfoBan(uuid, reason, admin, 0, BanType.BAN.getId()));
		plugin.cache.put(uuid, list);
		addPlayer(uuid, reason, admin, 0, BanType.BAN);
	}
	
	public void ipbanPlayer(final String uuid, String ip, final String reason, final String admin){
		InfoBan info = new InfoBan(uuid, reason, admin, 0, BanType.IPBAN.getId());
		List<InfoBan> list = new ArrayList<InfoBan>();
		if(plugin.cacheIP.containsKey(uuid))
			list = plugin.cache.get(uuid);
		if(plugin.cacheIP.containsKey(uuid))
			list = plugin.cache.get(uuid);
		list.add(info);
		plugin.cache.put(uuid, list);
		plugin.cacheIP.put(ip, list);
		addPlayer(uuid, reason, admin, 0, BanType.IPBAN);
	}
	
	public void jailPlayer(final String uuid, final String reason, final String admin){
		List<InfoBan> list = new ArrayList<InfoBan>();
		if(plugin.cache.containsKey(uuid))
			list = plugin.cache.get(uuid);
		list.add(new InfoBan(uuid, reason, admin, 0, BanType.JAIL.getId()));
		plugin.cache.put(uuid, list);
		addPlayer(uuid, reason, admin, 0, BanType.JAIL);
	}
	
	public void warnPlayer(String uuid, String reason, String admin) {
		addPlayer(uuid, reason, admin, 0, BanType.WARN);
	}
	
	public void pardonPlayer(final String uuid, final String admin){
		plugin.getServer().getScheduler().runTaskAsynchronously(Ultrabans.getPlugin(),new Runnable(){
			@Override
			public void run() {
				plugin.getUBDatabase().removeFromJaillist(uuid);
			}	
		});
		addPlayer(uuid, "Released From Jail", admin, 0, BanType.INFO);
	}
	
	public void mutePlayer(final String uuid, final String reason, final String admin){
		plugin.muted.add(uuid);
		addPlayer(uuid, reason, admin, 0, BanType.MUTE);
	}
	
	public void kickPlayer(final String uuid, final String reason, final String admin){
		addPlayer(uuid, reason, admin, 0, BanType.KICK);
	}
	public void permabanPlayer(final String uuid, final String reason, final String admin){
		List<InfoBan> list = new ArrayList<InfoBan>();
		if(plugin.cache.containsKey(uuid))
			list = plugin.cache.get(uuid);
		list.add(new InfoBan(uuid, reason, admin, 0, BanType.PERMA.getId()));
		plugin.cache.put(uuid, list);
		addPlayer(uuid, reason, admin, 0, BanType.PERMA);
	}

	public void tempbanPlayer(String uuid, String reason, long temp, String admin) {
		List<InfoBan> list = new ArrayList<InfoBan>();
		if(plugin.cache.containsKey(uuid))
			list = plugin.cache.get(uuid);
		list.add(new InfoBan(uuid, reason, admin, 0, BanType.TEMPBAN.getId()));
		plugin.cache.put(uuid, list);
		addPlayer(uuid, reason, admin, temp, BanType.TEMPBAN);
	}

	public void tempipbanPlayer(String uuid, String ip, String reason, long temp, String admin) {
		InfoBan info = new InfoBan(uuid, reason, admin, 0, BanType.TEMPIPBAN.getId());
		List<InfoBan> list = new ArrayList<InfoBan>();
		if(plugin.cacheIP.containsKey(uuid))
			list = plugin.cache.get(uuid);
		if(plugin.cacheIP.containsKey(uuid))
			list = plugin.cache.get(uuid);
		list.add(info);
		plugin.cache.put(uuid, list);
		plugin.cacheIP.put(ip, list);
		addPlayer(uuid, reason, admin, temp, BanType.TEMPIPBAN);
	}

	public void tempjailPlayer(String uuid, String reason, long temp, String admin) {
		List<InfoBan> list = new ArrayList<InfoBan>();
		if(plugin.cache.containsKey(uuid))
			list = plugin.cache.get(uuid);
		list.add(new InfoBan(uuid, reason, admin, 0, BanType.TEMPJAIL.getId()));
		addPlayer(uuid, reason, admin, temp, BanType.TEMPJAIL);
	}
	
	public void clearWarn(final String uuid){
		plugin.getServer().getScheduler().runTaskAsynchronously(Ultrabans.getPlugin(),new Runnable(){
			@Override
			public void run() {
				plugin.getUBDatabase().clearWarns(uuid);
			}	
		});
	}
}
