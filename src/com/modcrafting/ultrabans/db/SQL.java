/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import com.modcrafting.ultrabans.UltraBan;
import com.modcrafting.ultrabans.util.EditBan;

public class SQL implements Database{
	UltraBan plugin;
	public SQL(UltraBan instance){
		plugin = instance;
	}
	public String mysqlTable = plugin.getConfig().getString("mysql-table","banlist");
	public String logip = plugin.getConfig().getString("mysql-table-ip","banlistip");
	public String SQLCreateBansTable = "CREATE TABLE IF NOT EXISTS `"+mysqlTable+"` (" +
			"`name` varchar(32) NOT NULL," +
			"`reason` text NOT NULL," + 
			"`admin` varchar(32) NOT NULL," + 
			"`time` bigint(20) NOT NULL," + 
			"`temptime` bigint(20) NOT NULL," + 
			"`id` int(11) NOT NULL AUTO_INCREMENT," + 
			"`type` int(1) NOT NULL DEFAULT '0'," + 
			"PRIMARY KEY (`id`) USING BTREE" + 
			") ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC;";
	public String SQLCreateBanipTable = "CREATE TABLE IF NOT EXISTS `"+logip+"` (" +
			"`name` varchar(32) NOT NULL," + 
			"`lastip` tinytext NOT NULL," + 
			"PRIMARY KEY (`name`)" + 
			") ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC;";
	public Connection getSQLConnection() {
		YamlConfiguration Config = (YamlConfiguration) plugin.getConfig();
		String dataHandler = Config.getString("Database");
		String mysqlDatabase = Config.getString("mysql-database","jdbc:mysql://localhost:3306/minecraft");
		String mysqlUser = Config.getString("mysql-user","root");
		String mysqlPassword = Config.getString("mysql-password","root");
		if(dataHandler.equalsIgnoreCase("mysql")){
			try {
				return DriverManager.getConnection(mysqlDatabase + "?autoReconnect=true&user=" + mysqlUser + "&password=" + mysqlPassword);
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
			}
			return null;
		}
		return null;
	}
	public void initialize(){
		Connection conn = getSQLConnection();
		if(conn != null){
			PreparedStatement ps = null;
			ResultSet rs = null;
			try{
				Statement s = conn.createStatement();
				s.executeUpdate(SQLCreateBansTable);
				s.executeUpdate(SQLCreateBanipTable);
				ps = conn.prepareStatement("SELECT * FROM " + mysqlTable + " WHERE (type = 0 OR type = 1 OR type = 9) AND (temptime > ? OR temptime = 0)");
				ps.setLong(1, System.currentTimeMillis()/1000);
	            rs = ps.executeQuery();
				while (rs.next()){
					String pName = rs.getString("name").toLowerCase();
					long pTime = rs.getLong("temptime");
					plugin.bannedPlayers.add(pName);
					if(pTime != 0){
						plugin.tempBans.put(pName,pTime);
					}
					if(rs.getInt("type") == 1){
						String ip = getAddress(pName);
						plugin.bannedIPs.add(ip);
			
					}
				}
				try {
					if (ps != null)
						ps.close();
					if (rs != null)
						rs.close();
					if (conn != null)
						conn.close();
				} catch (SQLException ex) {
					plugin.getLogger().log(Level.SEVERE, "SQLite:Failed to close MySQL connection: ", ex);
				}
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "SQLite:Unable to retreive database connection: ", ex);
            }
		}else{
			plugin.getLogger().log(Level.SEVERE, "SQLite:Unable to retreive database connection: ");
		}
	}
	@Override
	public void load() {
		initialize();
	}
	@Override
	public List<String> getBans(){
		Connection conn = getSQLConnection();
		if (conn != null) {
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				ps = conn.prepareStatement("SELECT * FROM " + mysqlTable + " WHERE (type = 0)");
				rs = ps.executeQuery();
				List<String> list = new ArrayList<String>();
				while (rs.next()){
					list.add(rs.getString("name"));
				}
				return list;
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
			} finally {
				try {
					if (ps != null)
						ps.close();
					if (rs != null)
						rs.close();
					if (conn != null)
						conn.close();
				} catch (SQLException ex) {
					plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
				}
			}	
		}
		return null;		
	}
	@Override
	public void setAddress(String pName, String logIp){
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("REPLACE INTO " + logip + " (name,lastip) VALUES(?,?)");
			ps.setString(1, pName);
			ps.setString(2, logIp);
			ps.executeUpdate();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
	}
	@Override
	public String getAddress(String pName) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM " + logip + " WHERE name = ?");
			ps.setString(1, pName);
			rs = ps.executeQuery();
			while (rs.next()){
				String ip = rs.getString("lastip");
				return ip;
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return null;
	}
	@Override
	public String getName(String ip) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM " + logip + " WHERE lastip = ?");
			ps.setString(1, ip);
			rs = ps.executeQuery();
			while (rs.next()){
				String name = rs.getString("name");
				return name;
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't find player.");
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return null;
	}
	@Override
	public boolean removeFromBanlist(String player) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
				ps = conn.prepareStatement("DELETE FROM " + mysqlTable + " WHERE name = ? AND (type = 0 OR type = 1) ORDER BY time DESC LIMIT 1");
				ps.setString(1, player);
			
			ps.executeUpdate();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
			return false;
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return true;

	}
	@Override
	public boolean permaBan(String bname){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM " + mysqlTable + " WHERE name = ?");
			ps.setString(1, bname);
			rs = ps.executeQuery();
			while (rs.next()){
				if(rs.getInt("type") == 9){
				return true;
				}
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
			return false;
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
				return false;
			}
		}
		return false;
	}
	@Override
	public void addPlayer(String player, String reason, String admin, long tempTime , int type){
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("INSERT INTO " + mysqlTable + " (name,reason,admin,time,temptime,type) VALUES(?,?,?,?,?,?)");
			ps.setLong(5, tempTime);
			ps.setString(1, player);
			ps.setString(2, reason);
			ps.setString(3, admin);
			ps.setLong(4, System.currentTimeMillis()/1000);
			ps.setLong(6, type);
			ps.executeUpdate();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
	}
	@Override
	public void importPlayer(String player, String reason, String admin, long tempTime , long time, int type){
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("INSERT INTO " + mysqlTable + " (name,reason,admin,time,temptime,type) VALUES(?,?,?,?,?,?)");
			ps.setLong(5, tempTime);
			ps.setString(1, player);
			ps.setString(2, reason);
			ps.setString(3, admin);
			ps.setLong(4, time);
			ps.setLong(6, type);
			ps.executeUpdate();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
	}
	@Override
	public String getBanReason(String player) {
		Connection conn = getSQLConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("SELECT * FROM " + mysqlTable + " WHERE name = ? AND (type = 0 OR type = 1) ORDER BY time DESC LIMIT 1");
			ps.setString(1, player);
			rs = ps.executeQuery();
			while (rs.next()){
				String reason = rs.getString("reason");
				return reason;
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return "";
	}
	@Override
	public boolean matchAddress(String player, String ip) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT lastip FROM " + logip + " WHERE name = ? AND lastip = ?");
			ps.setString(1, player);
			ps.setString(2, ip);
			rs = ps.executeQuery();
			while(rs.next()){
				return true;
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return false;
	}
	@Override
	public void updateAddress(String p, String ip) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("UPDATE " + logip + " SET lastip = ? WHERE name = ?");
			ps.setString(1, ip);
			ps.setString(2, p);
			ps.executeUpdate();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
	}
	@Override
	public List<EditBan> listRecords(String name, CommandSender sender) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM " + mysqlTable + " WHERE name = ?");
			ps.setString(1, name);
			rs = ps.executeQuery();
			List<EditBan> bans = new ArrayList<EditBan>();
			while(rs.next()){
				bans.add(new EditBan(rs.getInt("id"),rs.getString("name"),rs.getString("reason"),rs.getString("admin"),rs.getLong("time"),rs.getLong("temptime"),rs.getInt("type")));
			}
			return bans;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return null;
	}
	@Override
	public List<EditBan> listRecent(String number){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Integer num = Integer.parseInt(number.trim());
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM " + mysqlTable + " ORDER BY time DESC LIMIT ?");
			ps.setInt(1, num);
			rs = ps.executeQuery();
			List<EditBan> bans = new ArrayList<EditBan>();
			while(rs.next()){
				bans.add(new EditBan(rs.getInt("id"),rs.getString("name"),rs.getString("reason"),rs.getString("admin"),rs.getLong("time"),rs.getLong("temptime"),rs.getInt("type")));
			}
			return bans;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return null;
	}
	@Override
	public EditBan loadFullRecord(String pName) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM " + mysqlTable + " WHERE name = ?");
			ps.setString(1, pName);
			rs = ps.executeQuery();
			while (rs.next()){
				return new EditBan(rs.getInt("id"),rs.getString("name"),rs.getString("reason"),rs.getString("admin"),rs.getLong("time"),rs.getLong("temptime"),rs.getInt("type"));
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return null;
	}
	@Override
	public List<EditBan> maxWarns(String Name) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM " + mysqlTable + " WHERE name = ? AND type = ?");
			ps.setString(1, Name);
			ps.setInt(2, 2);
			rs = ps.executeQuery();
			List<EditBan> bans = new ArrayList<EditBan>();
			while (rs.next()){
				bans.add(new EditBan(rs.getInt("id"),rs.getString("name"),rs.getString("reason"),rs.getString("admin"),rs.getLong("time"),rs.getLong("temptime"),rs.getInt("type")));
			}
			return bans;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return null;
	}
	@Override
	public EditBan loadFullRecordFromId(int id) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM " + mysqlTable + " WHERE id = ?");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			while (rs.next()){
				return new EditBan(rs.getInt("id"),rs.getString("name"),rs.getString("reason"),rs.getString("admin"),rs.getLong("time"),rs.getLong("temptime"),rs.getInt("type"));
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return null;
	}
	@Override
	public void saveFullRecord(EditBan ban){
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("UPDATE " + mysqlTable + " SET name = ?, reason = ?, admin = ?, time = ?, temptime = ?, type = ? WHERE id = ?");
			ps.setLong(5, ban.endTime);
			ps.setString(1, ban.name);
			ps.setString(2, ban.reason);
			ps.setString(3, ban.admin);
			ps.setLong(4, ban.time);
			ps.setLong(6, ban.type);
			ps.setInt(7, ban.id);
			ps.executeUpdate();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
	}
	@Override
	public boolean removeFromJaillist(String player) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
				ps = conn.prepareStatement("DELETE FROM " + mysqlTable + " WHERE name = ? AND type = ? ORDER BY time DESC LIMIT 1");
				ps.setString(1, player);
				ps.setInt(2, 6);
			
			ps.executeUpdate();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
			return false;
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return true;
		
	}
	@Override
	public String getjailReason(String player) {
		Connection conn = getSQLConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("SELECT * FROM " + mysqlTable + " WHERE name = ? AND type = 6 ORDER BY time DESC LIMIT 1");
			ps.setString(1, player);
			rs = ps.executeQuery();
			while (rs.next()){
				String reason = rs.getString("reason");
				return reason;
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return null;
	}
	@Override
	public void loadJailed(){
		Connection conn = getSQLConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT * FROM " + mysqlTable + " WHERE type = 6 AND (temptime > ? OR temptime = 0)");
			ps.setLong(1, System.currentTimeMillis()/1000);
        	rs = ps.executeQuery();
			while (rs.next()){
			String pName = rs.getString("name").toLowerCase();
			long pTime = rs.getLong("temptime");
			plugin.jailed.add(pName);
				if(pTime != 0){
					plugin.tempJail.put(pName,pTime);
				}
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
	}
	@Override
	public String getAdmin(String player) {
		Connection conn = getSQLConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("SELECT * FROM " + mysqlTable + " WHERE name = ? AND (type = 0 OR type = 1) ORDER BY time DESC LIMIT 1");
			ps.setString(1, player);
			rs = ps.executeQuery();
			while (rs.next()){
				String admin = rs.getString("admin");
				return admin;
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return null;
	}		

}