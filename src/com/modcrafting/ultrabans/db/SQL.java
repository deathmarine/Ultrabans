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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.EditBan;

public class SQL implements Database{
	Ultrabans plugin;
	String bantable;
	String iptable;
	String database;
	String username;
	String password;
	public SQL(Ultrabans instance){
		plugin = instance;
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		database = config.getString("MySQL.Database","jdbc:mysql://localhost:3306/minecraft");
		username = config.getString("MySQL.User","root");
		password = config.getString("MySQL.Password","root");
		bantable = config.getString("MySQL.Table","banlist");
		iptable = config.getString("MySQL.IPTable","banlistip");
	}
	public Connection getSQLConnection() {
		try {
			Properties info = new Properties();
			info.put("autoReconnect", "true");
			info.put("user", username);
			info.put("password", password);
			info.put("useUnicode", "true");
			info.put("characterEncoding", "utf8");
			return DriverManager.getConnection(database,info);
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
		return null;	
	}
	public void initialize(){
		Connection conn = getSQLConnection();
		if(conn != null){
			PreparedStatement ps = null;
			try{
				ps = conn.prepareStatement("SELECT * FROM " + bantable + " WHERE (type = 0 OR type = 1 OR type = 9) AND (temptime > ? OR temptime = 0)");
				ps.setLong(1, System.currentTimeMillis()/1000);
				ResultSet rs = ps.executeQuery();
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
				close(conn,ps,rs);
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
			}
		}else{
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection");
		}
	}
	public String SQLCreateBansTable = "CREATE TABLE IF NOT EXISTS %table% (" +
			"`name` varchar(32) NOT NULL," +
			"`reason` text NOT NULL," + 
			"`admin` varchar(32) NOT NULL," + 
			"`time` bigint(20) NOT NULL," + 
			"`temptime` bigint(20) NOT NULL," + 
			"`id` int(11) NOT NULL AUTO_INCREMENT," + 
			"`type` int(1) NOT NULL DEFAULT '0'," + 
			"PRIMARY KEY (`id`) USING BTREE" + 
			") ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;";
	public String SQLCreateBanipTable = "CREATE TABLE IF NOT EXISTS %table% (" +
			"`name` varchar(32) NOT NULL," + 
			"`lastip` tinytext NOT NULL," + 
			"PRIMARY KEY (`name`)" + 
			") ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;";
	@Override
	public void load() {
		Connection conn = getSQLConnection();
		PreparedStatement s;
		String str;
		try {
			str=SQLCreateBansTable;
			s = conn.prepareStatement(str.replaceAll("%table%",bantable));
			s.execute();
			str=SQLCreateBanipTable;
			s = conn.prepareStatement(str.replaceAll("%table%",iptable));
			s.execute();
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		initialize();
	}
	@Override
	public List<String> getBans(){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM " + bantable + " WHERE (type = 0)");
			rs = ps.executeQuery();
			List<String> list = new ArrayList<String>();
			while (rs.next()){
				list.add(rs.getString("name"));
			}
			close(conn,ps,rs);
			return list;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
		return null;		
	}
	@Override
	public void setAddress(String pName, String logIp){
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("REPLACE INTO " + iptable+ " (name,lastip) VALUES(?,?)");
			ps.setString(1, pName);
			ps.setString(2, logIp);
			ps.executeUpdate();
			close(conn,ps,null);
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
		return;
	}
	@Override
	public String getAddress(String pName) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM " + iptable+ " WHERE name = ?");
			ps.setString(1, pName);
			rs = ps.executeQuery();
			String ip = null;
			while (rs.next()){
				ip = rs.getString("lastip");
			}
			close(conn,ps,rs);
			return ip;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
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
			ps = conn.prepareStatement("SELECT * FROM " + iptable+ " WHERE lastip = ?");
			ps.setString(1, ip);
			rs = ps.executeQuery();
			String name = null;
			while (rs.next()){
				name = rs.getString("name");
			}
			close(conn,ps,rs);
			return name;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
		return null;
	}
	@Override
	public boolean removeFromBanlist(String player) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("DELETE FROM " + bantable + " WHERE name = ? AND (type = 0 OR type = 1) ORDER BY time DESC LIMIT 1");
			ps.setString(1, player);
			ps.executeUpdate();
			close(conn,ps,null);
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
			return false;
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
			ps = conn.prepareStatement("SELECT * FROM " + bantable + " WHERE name = ?");
			ps.setString(1, bname);
			rs = ps.executeQuery();
			boolean set = false;
			while(rs.next()){
				if(rs.getInt("type") == 9)	set = true;
			}
			close(conn,ps,rs);
			return set;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
		return false;
	}
	@Override
	public void addPlayer(String player, String reason, String admin, long tempTime , int type){
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("INSERT INTO " + bantable + " (name,reason,admin,time,temptime,type) VALUES(?,?,?,?,?,?)");
			ps.setLong(5, tempTime);
			ps.setString(1, player);
			ps.setString(2, reason);
			ps.setString(3, admin);
			ps.setLong(4, System.currentTimeMillis()/1000);
			ps.setLong(6, type);
			ps.executeUpdate();
			close(conn,ps,null);
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
	}
	@Override
	public void importPlayer(String player, String reason, String admin, long tempTime , long time, int type){
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("INSERT INTO " + bantable + " (name,reason,admin,time,temptime,type) VALUES(?,?,?,?,?,?)");
			ps.setLong(5, tempTime);
			ps.setString(1, player);
			ps.setString(2, reason);
			ps.setString(3, admin);
			ps.setLong(4, time);
			ps.setLong(6, type);
			ps.executeUpdate();
			close(conn,ps,null);
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
	}
	@Override
	public String getBanReason(String player) {
		Connection conn = getSQLConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("SELECT * FROM " + bantable + " WHERE name = ? AND (type = 0 OR type = 1) ORDER BY time DESC LIMIT 1");
			ps.setString(1, player);
			rs = ps.executeQuery();
			String reason = "";
			while (rs.next()){
				reason = rs.getString("reason");
			}
			close(conn,ps,rs);
			return reason;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
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
			ps = conn.prepareStatement("SELECT lastip FROM " + iptable+ " WHERE name = ? AND lastip = ?");
			ps.setString(1, player);
			ps.setString(2, ip);
			rs = ps.executeQuery();
			boolean set = false;
			while(rs.next()){
				set = true;
			}
			close(conn,ps,rs);
			return set;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
		return false;
	}
	@Override
	public void updateAddress(String p, String ip) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("UPDATE " + iptable+ " SET lastip = ? WHERE name = ?");
			ps.setString(1, ip);
			ps.setString(2, p);
			ps.executeUpdate();
			close(conn,ps,null);
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
	}
	@Override
	public List<EditBan> listRecords(String name, CommandSender sender) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM " + bantable + " WHERE name = ?");
			ps.setString(1, name);
			rs = ps.executeQuery();
			List<EditBan> bans = new ArrayList<EditBan>();
			while (rs.next()){
				bans.add(new EditBan(rs.getInt("id"),rs.getString("name"),rs.getString("reason"),rs.getString("admin"),rs.getLong("time"),rs.getLong("temptime"),rs.getInt("type")));
			}
			close(conn,ps,rs);
			return bans;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
		return null;
	}
	@Override
	public List<EditBan> listRecent(String number){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Integer num = Integer.parseInt(number.trim());
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM " + bantable + " ORDER BY time DESC LIMIT ?");
			ps.setInt(1, num);
			rs = ps.executeQuery();
			List<EditBan> bans = new ArrayList<EditBan>();
			while (rs.next()){
				bans.add(new EditBan(rs.getInt("id"),rs.getString("name"),rs.getString("reason"),rs.getString("admin"),rs.getLong("time"),rs.getLong("temptime"),rs.getInt("type")));
			}
			close(conn,ps,rs);
			return bans;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		} catch (NumberFormatException nfe){
			plugin.getLogger().warning("Input was not a number.");
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
			ps = conn.prepareStatement("SELECT * FROM " + bantable + " WHERE name = ?");
			ps.setString(1, pName);
			rs = ps.executeQuery();
			EditBan eb = null;
			while (rs.next()){
				eb = new EditBan(rs.getInt("id"),rs.getString("name"),rs.getString("reason"),rs.getString("admin"),rs.getLong("time"),rs.getLong("temptime"),rs.getInt("type"));
			}
			close(conn,ps,rs);
			return eb;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
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
			ps = conn.prepareStatement("SELECT * FROM " + bantable + " WHERE name = ? AND type = ?");
			ps.setString(1, Name);
			ps.setInt(2, 2);
			rs = ps.executeQuery();
			List<EditBan> bans = new ArrayList<EditBan>();
			while (rs.next()){
				bans.add(new EditBan(rs.getInt("id"),rs.getString("name"),rs.getString("reason"),rs.getString("admin"),rs.getLong("time"),rs.getLong("temptime"),rs.getInt("type")));
			}
			close(conn,ps,rs);
			return bans;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
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
			ps = conn.prepareStatement("SELECT * FROM " + bantable + " WHERE id = ?");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			EditBan eb = null;
			while (rs.next()){
				eb = new EditBan(rs.getInt("id"),rs.getString("name"),rs.getString("reason"),rs.getString("admin"),rs.getLong("time"),rs.getLong("temptime"),rs.getInt("type"));
			}
			close(conn,ps,rs);
			return eb;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
		return null;
	}
	@Override
	public void saveFullRecord(EditBan ban){
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("UPDATE " + bantable + " SET name = ?, reason = ?, admin = ?, time = ?, temptime = ?, type = ? WHERE id = ?");
			ps.setLong(5, ban.endTime);
			ps.setString(1, ban.name);
			ps.setString(2, ban.reason);
			ps.setString(3, ban.admin);
			ps.setLong(4, ban.time);
			ps.setLong(6, ban.type);
			ps.setInt(7, ban.id);
			ps.executeUpdate();
			close(conn,ps,null);
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
	}
	@Override
	public boolean removeFromJaillist(String player) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
				ps = conn.prepareStatement("DELETE FROM " + bantable + " WHERE name = ? AND type = ? ORDER BY time DESC LIMIT 1");
				ps.setString(1, player);
				ps.setInt(2, 6);
			
			ps.executeUpdate();
			close(conn,ps,null);
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
			return false;
		}
		return true;
		
	}
	@Override
	public String getjailReason(String player) {
		Connection conn = getSQLConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("SELECT * FROM " + bantable + " WHERE name = ? AND type = 6 ORDER BY time DESC LIMIT 1");
			ps.setString(1, player);
			rs = ps.executeQuery();
			String reason = null;
			while (rs.next()){
				reason = rs.getString("reason");
			}
			close(conn,ps,rs);
			return reason;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
		return null;
	}
	@Override
	public void loadJailed(){
		Connection conn = getSQLConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT * FROM " + bantable + " WHERE type = 6 AND (temptime > ? OR temptime = 0)");
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
			close(conn,ps,rs);
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
	}
	@Override
	public String getAdmin(String player) {
		Connection conn = getSQLConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("SELECT * FROM " + bantable + " WHERE name = ? AND (type = 0 OR type = 1) ORDER BY time DESC LIMIT 1");
			ps.setString(1, player);
			rs = ps.executeQuery();
			String admin = null;
			while (rs.next()){
				admin = rs.getString("admin");
			}
			close(conn,ps,rs);
			return admin;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
		return null;
	}		

	@Override
	public List<String> listPlayers(String ip){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM " + iptable + " WHERE lastip = ?");
			ps.setString(1, ip);
			rs = ps.executeQuery();
			List<String> bans = new ArrayList<String>();
			while(rs.next()){
				bans.add(rs.getString("name"));
			}
			close(conn,ps,rs);
			return bans;
		} catch (SQLException ex) {
			Error.execute(plugin, ex);
		}
		return null;
	}
	public void close(Connection conn,PreparedStatement ps,ResultSet rs){
		try {
			if (ps != null)
				ps.close();
			if (conn != null)
				conn.close();
			if (rs != null)
				rs.close();
		} catch (SQLException ex) {
			Error.close(plugin, ex);
		}
	}
}