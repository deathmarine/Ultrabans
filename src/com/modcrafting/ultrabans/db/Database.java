/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.db;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.modcrafting.ultrabans.util.EditBan;

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
	public List<EditBan> listRecords(String name, CommandSender sender);
	public List<EditBan> listRecent(String number);
	public EditBan loadFullRecord(String pName);
	public List<EditBan> maxWarns(String Name);
	public EditBan loadFullRecordFromId(int id);
	public void saveFullRecord(EditBan ban);
	public boolean removeFromJaillist(String player);
	public String getjailReason(String player);
	public void loadJailed();
	public String getAdmin(String player);
	public void importPlayer(String player, String reason, String admin,long tempTime, long time, int type);
	public List<String> listPlayers(String ip);
	public List<EditBan> listRecentBans(String number);
}