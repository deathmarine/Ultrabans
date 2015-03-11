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
package com.modcrafting.ultrabans.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.BanInfo;

public abstract class Database {
	Ultrabans plugin;
	String bantable;
	String iptable;
	Connection connection;

	public Database(Ultrabans instance) {
		plugin = instance;
	}

	public abstract Connection getSQLConnection();

	public abstract void load();

	public void initialize() {
		connection = getSQLConnection();
		try {
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM "
					+ bantable + " WHERE type != 8 AND type != 5");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String puuid = rs.getString("uuid");
				List<BanInfo> list = new ArrayList<BanInfo>();
				if (plugin.cache.containsKey(puuid.toLowerCase()))
					list = plugin.cache.get(puuid.toLowerCase());
				list.add(new BanInfo(rs.getString("uuid"), rs
						.getString("reason"), rs.getString("admin"), rs
						.getLong("temptime"), rs.getInt("type")));
				plugin.cache.put(puuid.toLowerCase(), list);
				if (rs.getInt("type") == 1 || rs.getInt("type") == 11) {
					list = new ArrayList<BanInfo>();
					String ip = getAddress(puuid);
					if (ip != null && plugin.cacheIP.containsKey(ip))
						list = plugin.cacheIP.get(ip);
					list.add(new BanInfo(rs.getString("uuid"), rs
							.getString("reason"), rs.getString("admin"), rs
							.getLong("temptime"), rs.getInt("type")));
					plugin.cacheIP.put(ip, list);
				}
			}
			close(ps, rs);
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE,
					"Unable to retreive connection", ex);
		}
	}

	public void setAddress(String uuid, String logIp) {
		try {
			connection = getSQLConnection();
			PreparedStatement ps = connection.prepareStatement("REPLACE INTO "
					+ iptable + " (uuid,lastip) VALUES(?,?)");
			ps.setString(1, uuid);
			ps.setString(2, logIp);
			ps.executeUpdate();
			close(ps, null);
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
		return;
	}

	public String getAddress(String uuid) {
		try {
			connection = getSQLConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM "
					+ iptable + " WHERE uuid = ?");
			ps.setString(1, uuid);
			ResultSet rs = ps.executeQuery();
			String ip = null;
			while (rs.next()) {
				ip = rs.getString("lastip");
			}
			close(ps, rs);
			return ip;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
		return null;
	}

	public String getUUID(String ip) {
		try {
			connection = getSQLConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM "
					+ iptable + " WHERE lastip = ?");
			ps.setString(1, ip);
			ResultSet rs = ps.executeQuery();
			String uuid = null;
			while (rs.next()) {
				uuid = rs.getString("uuid");
			}
			close(ps, rs);
			return uuid;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
		return null;
	}

	public boolean matchAddress(String uuid, String ip) {
		try {
			connection = getSQLConnection();
			PreparedStatement ps = connection
					.prepareStatement("SELECT lastip FROM " + iptable
							+ " WHERE uuid = ? AND lastip = ?");
			ps.setString(1, uuid);
			ps.setString(2, ip);
			ResultSet rs = ps.executeQuery();
			boolean set = false;
			while (rs.next()) {
				set = true;
			}
			close(ps, rs);
			return set;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
		return false;
	}

	public void updateAddress(String uuid, String ip) {
		try {
			connection = getSQLConnection();
			PreparedStatement ps = connection.prepareStatement("UPDATE "
					+ iptable + " SET lastip = ? WHERE uuid = ?");
			ps.setString(1, ip);
			ps.setString(2, uuid);
			ps.executeUpdate();
			close(ps, null);
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
	}

	public void addPlayer(String uuid, String reason, String admin,
			long tempTime, int type) {
		try {
			connection = getSQLConnection();
			PreparedStatement ps = connection
					.prepareStatement("INSERT INTO "
							+ bantable
							+ " (uuid,reason,admin,time,temptime,type) VALUES(?,?,?,?,?,?)");
			ps.setLong(5, tempTime);
			ps.setString(1, uuid);
			ps.setString(2, reason);
			ps.setString(3, admin);
			ps.setLong(4, System.currentTimeMillis() / 1000);
			ps.setLong(6, type);
			ps.executeUpdate();
			close(ps, null);
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
	}

	public void importPlayer(String uuid, String reason, String admin,
			long tempTime, long time, int type) {
		try {
			connection = getSQLConnection();
			PreparedStatement ps = connection
					.prepareStatement("INSERT INTO "
							+ bantable
							+ " (uuid,reason,admin,time,temptime,type) VALUES(?,?,?,?,?,?)");
			ps.setLong(5, tempTime);
			ps.setString(1, uuid);
			ps.setString(2, reason);
			ps.setString(3, admin);
			ps.setLong(4, time);
			ps.setLong(6, type);
			ps.executeUpdate();
			close(ps, null);
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
	}

	public boolean removeFromBanlist(String uuid) {
		try {
			connection = getSQLConnection();
			PreparedStatement ps = connection.prepareStatement("DELETE FROM "
					+ bantable + " WHERE uuid = ? AND (type = 0 OR type = 1)");
			ps.setString(1, uuid);
			ps.executeUpdate();
			close(ps, null);
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
			return false;
		}
		return true;
	}

	public List<BanInfo> listRecords(String uuid) {
		try {
			connection = getSQLConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM "
					+ bantable + " WHERE uuid = ?");
			ps.setString(1, uuid);
			ResultSet rs = ps.executeQuery();
			List<BanInfo> bans = new ArrayList<BanInfo>();
			while (rs.next()) {
				bans.add(new BanInfo(rs.getString("uuid"), rs
						.getString("reason"), rs.getString("admin"), rs
						.getLong("temptime"), rs.getInt("type")));
			}
			close(ps, rs);
			return bans;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
		return null;
	}

	public List<BanInfo> listRecent(String number) {
		Integer num = Integer.parseInt(number.trim());
		try {
			connection = getSQLConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM "
					+ bantable + " ORDER BY time DESC LIMIT ?");
			ps.setInt(1, num);
			ResultSet rs = ps.executeQuery();
			List<BanInfo> bans = new ArrayList<BanInfo>();
			while (rs.next()) {
				bans.add(new BanInfo(rs.getString("uuid"), rs
						.getString("reason"), rs.getString("admin"), rs
						.getLong("temptime"), rs.getInt("type")));
			}
			close(ps, rs);
			return bans;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		} catch (NumberFormatException nfe) {
			plugin.getLogger().warning("Input was not a number.");
		}
		return null;
	}

	public List<BanInfo> maxWarns(String uuid) {
		try {
			connection = getSQLConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM "
					+ bantable + " WHERE uuid = ? AND type = ?");
			ps.setString(1, uuid);
			ps.setInt(2, 2);
			ResultSet rs = ps.executeQuery();
			List<BanInfo> bans = new ArrayList<BanInfo>();
			while (rs.next()) {
				bans.add(new BanInfo(rs.getString("uuid"), rs
						.getString("reason"), rs.getString("admin"), rs
						.getLong("temptime"), rs.getInt("type")));
			}
			close(ps, rs);
			return bans;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
		return null;
	}

	public boolean removeFromJaillist(String uuid) {
		try {
			connection = getSQLConnection();
			PreparedStatement ps = connection.prepareStatement("DELETE FROM "
					+ bantable + " WHERE uuid = ? AND type = 6");
			ps.setString(1, uuid);
			ps.executeUpdate();
			close(ps, null);
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
			return false;
		}
		return true;

	}

	public List<String> listPlayers(String ip) {
		try {
			connection = getSQLConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM "
					+ iptable + " WHERE lastip = ?");
			ps.setString(1, ip);
			ResultSet rs = ps.executeQuery();
			List<String> bans = new ArrayList<String>();
			while (rs.next()) {
				bans.add(rs.getString("uuid"));
			}
			close(ps, rs);
			return bans;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
		return null;
	}

	public void clearWarns(String uuid) {
		try {
			connection = getSQLConnection();
			PreparedStatement ps = connection.prepareStatement("DELETE FROM "
					+ bantable + " WHERE uuid = ? AND type = 2");
			ps.setString(1, uuid);
			close(ps, null);
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
	}

	public void close(PreparedStatement ps, ResultSet rs) {
		try {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		} catch (SQLException ex) {
			Error.close(plugin, ex);
		}
	}
}