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
package com.modcrafting.ultrabans.db;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.modcrafting.ultrabans.util.BanInfo;

public interface Database {
	public void load();
	public List<String> getBans();
	public void setAddress(String pName, String logIp);
	public String getAddress(String pName);
	public String getName(String ip);
	public boolean removeFromBanlist(String player);
	public void addPlayer(String player, String reason, String admin, long tempTime, int type);
	public boolean permaBan(String bname);
	public String getBanReason(String player);
	public boolean matchAddress(String player, String ip);
	public void updateAddress(String p, String ip);
	public List<BanInfo> listRecords(String name, CommandSender sender);
	public List<BanInfo> listRecent(String number);
	public BanInfo loadFullRecord(String pName);
	public List<BanInfo> maxWarns(String Name);
	public BanInfo loadFullRecordFromId(int id);
	public void saveFullRecord(BanInfo ban);
	public boolean removeFromJaillist(String player);
	public String getjailReason(String player);
	public void loadJailed();
	public String getAdmin(String player);
	public void importPlayer(String player, String reason, String admin,long tempTime, long time, int type);
	public List<String> listPlayers(String ip);
	public List<BanInfo> listRecentBans(String number);
	public void clearWarns(String player);
}