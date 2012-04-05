package com.modcrafting.ultrabans.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
import com.modcrafting.ultrabans.commands.EditBan;

public class SQLDatabases{
	static UltraBan plugin;
	
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
			
				UltraBan.log.log(Level.SEVERE, "Unable to retreive connection", ex);
			}
			return null;
		}
		if(dataHandler.equalsIgnoreCase("sqlite")){

			String dbname = Config.getString("sqlite-dbname", "banlist");
			String maindir = "plugins/UltraBan/";
			File dataFolder = new File(maindir, dbname + ".db");
			if (!dataFolder.exists()){
				try {
					dataFolder.createNewFile();
					Class.forName("org.sqlite.JDBC");
		            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
		            Statement st = conn.createStatement();
	        		st.execute(this.SQLiteCreateBansTable);
	        		st.execute(this.SQLiteCreateBanipTable);
	        		return conn;
				} catch (IOException ex) {
					UltraBan.log.log(Level.SEVERE, "File write error: " + dbname);
				} catch (SQLException ex) {
			            UltraBan.log.log(Level.SEVERE,"SQLite exception on initialize", ex);
			    } catch (ClassNotFoundException ex) {
			        	UltraBan.log.log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
			    }
			}
			try {
	            Class.forName("org.sqlite.JDBC");
	            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
	            return conn;
        		
	        } catch (SQLException ex) {
	            UltraBan.log.log(Level.SEVERE,"SQLite exception on initialize", ex);
	        } catch (ClassNotFoundException ex) {
	        	UltraBan.log.log(Level.SEVERE, "You need the SQLite library.", ex);
	        }
	    }
		return null;
	}

	protected String SQLiteCreateBansTable = "CREATE TABLE IF NOT EXISTS `banlist` (" +
			"`name` TEXT," +
			"`reason` TEXT," + 
			"`admin` TEXT," + 
			"`time` INTEGER," + 
			"`temptime` INTEGER ," + 
			"`id` INTEGER PRIMARY KEY," + 
			"`type` INTEGER DEFAULT '0'" + 
			");";
	protected String SQLiteCreateBanipTable = "CREATE TABLE IF NOT EXISTS `banlistip` (" +
			"`name` TEXT," + 
			"`lastip` TEXT," + 
			"PRIMARY KEY (`name`)" + 
			");";
	protected String SQLCreateBansTable = "CREATE TABLE IF NOT EXISTS `banlist` (" +
			"`name` varchar(32) NOT NULL," +
			"`reason` text NOT NULL," + 
			"`admin` varchar(32) NOT NULL," + 
			"`time` bigint(20) NOT NULL," + 
			"`temptime` bigint(20) NOT NULL," + 
			"`id` int(11) NOT NULL AUTO_INCREMENT," + 
			"`type` int(1) NOT NULL DEFAULT '0'," + 
			"PRIMARY KEY (`id`) USING BTREE" + 
			") ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC;";
	protected String SQLCreateBanipTable = "CREATE TABLE IF NOT EXISTS `banlistip` (" +
			"`name` varchar(32) NOT NULL," + 
			"`lastip` tinytext NOT NULL," + 
			"PRIMARY KEY (`name`)" + 
			") ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC;";
	
	public void initialize(UltraBan plugin){
		YamlConfiguration Config = (YamlConfiguration) plugin.getConfig();
		String mysqlTable = Config.getString("mysql-table");
		String logip = Config.getString("mysql-table-ip");
		SQLDatabases.plugin = plugin;
		Connection conn = getSQLConnection();
		
		
		if (conn == null) {
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Could not establish SQL connection. Disabling UltraBan");
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Adjust Settings in Config or set MySql: False");
			plugin.getServer().getPluginManager().disablePlugin(plugin);
			return;
		} else {
			PreparedStatement ps = null;
			ResultSet rs = null;
			Statement st = null;
			
			try {
				ps = conn.prepareStatement("SELECT * FROM " + mysqlTable + " WHERE (type = 0 OR type = 1 OR type = 9) AND (temptime > ? OR temptime = 0)");
				ps.setLong(1, System.currentTimeMillis()/1000);
				try{
					
					DatabaseMetaData dbm = conn.getMetaData();
					rs = dbm.getTables(null, null, "banlist", null);
		            	if (!rs.next()){
		            		conn.setAutoCommit(false);
		            		st = conn.createStatement();
		            		st.execute(this.SQLCreateBansTable);
		            		st.execute(this.SQLCreateBanipTable);
		            		conn.commit();
		            		UltraBan.log.log(Level.INFO, "[UltraBan]: Table " + mysqlTable + " created.");
		            		UltraBan.log.log(Level.INFO, "[UltraBan]: Table " + logip + " created.");
		            	}
		            	rs = ps.executeQuery();
							            
				} catch (SQLException ex) {
					UltraBan.log.log(Level.SEVERE, "[UltraBan] Database Error: No Table Found");
                }
				
				try {
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
				}catch (NullPointerException ex){
					UltraBan.log.log(Level.SEVERE, "[UltraBan] Detected Major issues with database.");
					plugin.getServer().getPluginManager().disablePlugin(plugin);
					UltraBan.log.log(Level.SEVERE, "[UltraBan] Attempting Restart.");
					plugin.getServer().getPluginManager().enablePlugin(plugin);
					return;
				}
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
				return;
			} finally {
				try {
					if (ps != null)
						ps.close();
					if (rs != null)
						rs.close();
					if (conn != null)
						conn.close();
				} catch (SQLException ex) {
					UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
				}
			}	

			try {
				if (!plugin.isEnabled()){
					return;
				}
				conn.close();
				UltraBan.log.log(Level.INFO, "[UltraBan] Initialized db connection" );
			} catch (SQLException e) {
				e.printStackTrace();
				plugin.getServer().getPluginManager().disablePlugin(plugin);
			}
		}
		
	}
	
	public void setAddress(String pName, String logIp){
		YamlConfiguration Config = (YamlConfiguration) plugin.getConfig();
		String logip = Config.getString("mysql-table-ip");
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("REPLACE INTO " + logip + " (name,lastip) VALUES(?,?)");
			ps.setString(1, pName);
			ps.setString(2, logIp);
			ps.executeUpdate();
		} catch (SQLException ex) {
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
	}
	public String getAddress(String pName) {
		YamlConfiguration Config = (YamlConfiguration) plugin.getConfig();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String logip = Config.getString("mysql-table-ip");
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
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return null;
	}
	public String getName(String ip) {
		YamlConfiguration Config = (YamlConfiguration) plugin.getConfig();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String logip = Config.getString("mysql-table-ip");
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
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't find player.");
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return null;
	}
	public boolean removeFromBanlist(String player) {
		YamlConfiguration Config = (YamlConfiguration) plugin.getConfig();
		String dataHandler = Config.getString("Database");

		String mysqlTable = Config.getString("mysql-table");

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
			if(dataHandler.equalsIgnoreCase("sqlite")){
				ps = conn.prepareStatement("DELETE FROM " + mysqlTable + " WHERE name = ? AND type = ?");
			}else{
				ps = conn.prepareStatement("DELETE FROM " + mysqlTable + " WHERE name = ? AND type = ? ORDER BY time DESC LIMIT 1");
			}
			ps.setString(1, player);
			ps.setInt(2, 0);
			ps.executeUpdate();
		} catch (SQLException ex) {
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
			return false;
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return true;

	}
	public boolean permaBan(String bname){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM banlist WHERE name = ?");
			ps.setString(1, bname);
			rs = ps.executeQuery();
			while (rs.next()){
				if(rs.getInt("type") == 9){
				return true;
				}
			}
		} catch (SQLException ex) {
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
			return false;
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
				return false;
			}
		}
		return false;
	}
	public void addPlayer(String player, String reason, String admin, long tempTime , int type){
		YamlConfiguration Config = (YamlConfiguration) plugin.getConfig();
		String mysqlTable = Config.getString("mysql-table");
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
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
	}
	public String getBanReason(String player) {
		YamlConfiguration Config = (YamlConfiguration) plugin.getConfig();
		Connection conn = getSQLConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String mysqlTable = Config.getString("mysql-table");
		try {
			ps = conn.prepareStatement("SELECT * FROM " + mysqlTable + " WHERE name = ? AND (type = 0 OR type = 1)");
			ps.setString(1, player);
			rs = ps.executeQuery();
			while (rs.next()){
				String reason = rs.getString("reason");
				return reason;
			}
		} catch (SQLException ex) {
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return null;
	}

	public boolean matchAddress(String player, String ip) {
		YamlConfiguration Config = (YamlConfiguration) plugin.getConfig();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String logip = Config.getString("mysql-table-ip");
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
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return false;
	}
	public void updateAddress(String p, String ip) {
		YamlConfiguration Config = (YamlConfiguration) plugin.getConfig();
		Connection conn = null;
		PreparedStatement ps = null;
		String logip = Config.getString("mysql-table-ip");
		try {
			System.out.println("trying to update address.");
			conn = getSQLConnection();
			ps = conn.prepareStatement("UPDATE " + logip + " SET lastip = ? WHERE name = ?");
			ps.setString(1, ip);
			ps.setString(2, p);
			ps.executeUpdate();
		} catch (SQLException ex) {
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
	}
	public List<EditBan> listRecords(String name, CommandSender sender) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM banlist WHERE name = ?");
			ps.setString(1, name);
			rs = ps.executeQuery();
			List<EditBan> bans = new ArrayList<EditBan>();
			while(rs.next()){
				bans.add(new EditBan(rs.getInt("id"),rs.getString("name"),rs.getString("reason"),rs.getString("admin"),rs.getLong("time"),rs.getLong("temptime"),rs.getInt("type")));
			}
			return bans;
		} catch (SQLException ex) {
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return null;
	}
	public List<EditBan> listRecent(String number){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Integer num = Integer.parseInt(number.trim());
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM banlist ORDER BY time DESC LIMIT ?");
			ps.setInt(1, num);
			rs = ps.executeQuery();
			List<EditBan> bans = new ArrayList<EditBan>();
			while(rs.next()){
				bans.add(new EditBan(rs.getInt("id"),rs.getString("name"),rs.getString("reason"),rs.getString("admin"),rs.getLong("time"),rs.getLong("temptime"),rs.getInt("type")));
			}
			return bans;
		} catch (SQLException ex) {
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return null;
	}
	public EditBan loadFullRecord(String pName) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM banlist WHERE name = ?");
			ps.setString(1, pName);
			rs = ps.executeQuery();
			while (rs.next()){
				return new EditBan(rs.getInt("id"),rs.getString("name"),rs.getString("reason"),rs.getString("admin"),rs.getLong("time"),rs.getLong("temptime"),rs.getInt("type"));
			}
		} catch (SQLException ex) {
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return null;
	}
	public List<EditBan> maxWarns(String Name) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM banlist WHERE name = ? AND type = ?");
			ps.setString(1, Name);
			ps.setInt(2, 2);
			rs = ps.executeQuery();
			List<EditBan> bans = new ArrayList<EditBan>();
			while (rs.next()){
				bans.add(new EditBan(rs.getInt("id"),rs.getString("name"),rs.getString("reason"),rs.getString("admin"),rs.getLong("time"),rs.getLong("temptime"),rs.getInt("type")));
			}
		} catch (SQLException ex) {
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return null;
	}
	public EditBan loadFullRecordFromId(int id) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM banlist WHERE id = ?");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			while (rs.next()){
				return new EditBan(rs.getInt("id"),rs.getString("name"),rs.getString("reason"),rs.getString("admin"),rs.getLong("time"),rs.getLong("temptime"),rs.getInt("type"));
			}
		} catch (SQLException ex) {
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return null;
	}
	public void saveFullRecord(EditBan ban){
		YamlConfiguration Config = (YamlConfiguration) plugin.getConfig();
		String mysqlTable = Config.getString("mysql-table");

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
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
	}
	public static String resourceToString(String name) {
        InputStream input = UltraBan.class.getResourceAsStream(name);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];

        if(input != null) {
            try {
                int n;
                Reader reader = new BufferedReader(new InputStreamReader(input));
                while ((n = reader.read(buffer)) != -1)
                    writer.write(buffer, 0, n);
            } catch (IOException e) {
                try {
                    input.close();
                } catch (IOException ex) { }
                return null;
            } finally {
                try {
                    input.close();
                } catch (IOException e) { }
            }
        } else {
            return null;
        }

        String text = writer.toString().trim();
        text = text.replace("\r\n", " ").replace("\n", " ");
        return text.trim();
    }

	public boolean removeFromJaillist(String player) {
		YamlConfiguration Config = (YamlConfiguration) plugin.getConfig();
		String dataHandler = Config.getString("Database");
		
		String mysqlTable = Config.getString("mysql-table");

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
			if(dataHandler.equalsIgnoreCase("sqlite")){
				ps = conn.prepareStatement("DELETE FROM " + mysqlTable + " WHERE name = ? AND type = ?");
			}else{
				ps = conn.prepareStatement("DELETE FROM " + mysqlTable + " WHERE name = ? AND type = ? ORDER BY time DESC LIMIT 1");
			}
			ps.setString(1, player);
			ps.setInt(2, 6);
			ps.executeUpdate();
		} catch (SQLException ex) {
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
			return false;
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
			}
		}
		return true;
		
	}
}
