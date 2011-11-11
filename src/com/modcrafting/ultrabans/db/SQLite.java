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
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import com.modcrafting.ultrabans.UltraBan;
import com.modcrafting.ultrabans.commands.EditBan;

public class SQLite{
	static Plugin plugin;
	static Connection conn;
	public String maindir = "plugins/UltraBan/";
	public static final Logger log = Logger.getLogger("Minecraft");
	File dataFolder = new File(maindir, "banlist.db");
	public Connection SQLinitialize(){
	        try {
	            Class.forName("org.sqlite.JDBC");
	            conn = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/banlist.db");
	            conn.setAutoCommit(false);
	            return conn;
	        } catch (SQLException ex) {
	            log.log(Level.SEVERE,"SQLite exception on initialize", ex);
	        } catch (ClassNotFoundException ex) {
	            log.log(Level.SEVERE, "You need the SQLite library.", ex);
	        }
	        return conn;
	    }

	    public static Connection getConnection() {
	        return conn;
	    }

	    public void closeConnection() {
	        if(conn != null) {
	            try {
	                conn.close();
	            } catch (SQLException ex) {
	            	log.log(Level.SEVERE, "Error on Connection close", ex);
	            }
	        }
	    }
	protected String Database = "jdbc:sqlite:banlist.db";
	protected static String SQLCreateBansTable = "CREATE TABLE IF NOT EXISTS `banlist` (" +
			"`name` varchar(32) NOT NULL," +
			"`reason` text NOT NULL," + 
			"`admin` varchar(32) NOT NULL," + 
			"`time` bigint(20) NOT NULL," + 
			"`temptime` bigint(20) NOT NULL," + 
			"`id` int(11) NOT NULL AUTO_INCREMENT," + 
			"`type` int(1) NOT NULL DEFAULT '0'," + 
			"PRIMARY KEY (`id`) USING BTREE" + 
			") ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC;";
	protected static String SQLCreateBanipTable = "CREATE TABLE IF NOT EXISTS `banlistip` (" +
			"`name` varchar(32) NOT NULL," + 
			"`lastip` tinytext NOT NULL," + 
			"PRIMARY KEY (`name`)" + 
			") ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC;";
	

    public void initialize() {
        if (!tableExists()) {
            createTable();
        }
    }
    private static boolean tableExists() {
        ResultSet rs = null;
        try {
            Connection conn = getConnection();
            DatabaseMetaData dbm = conn.getMetaData();
            rs = dbm.getTables(null, null, "homeTable", null);
            if (!rs.next()) {
                return false;
            }
            return true;
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "[UltraBan]: Table Check Exception", ex);
            return false;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                log.log(Level.SEVERE, "[UltraBan]: Table Check SQL Exception (on closing)");
            }

        }
    }
    private static void createTable() {
        Statement st = null;
        try {
            Connection conn = getConnection();
            st = conn.createStatement();
            st.executeUpdate(SQLCreateBansTable);
            st.executeUpdate(SQLCreateBanipTable);
            conn.commit();
        } catch (SQLException e) {
            
            log.log(Level.SEVERE, "[UltraBan]: Create Table Exception", e);
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
            } catch (SQLException e) {
                Logger log = Logger.getLogger("Minecraft");
                log.log(Level.SEVERE, "[UltraBan]: Could not create the table (on close)");
            }
        }
    }
    
	public void setAddress(String pName, String logIp){
		String logip = plugin.getConfiguration().getString("mysql-table-ip");
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement("REPLACE INTO " + logip + " (name,lastip) VALUES(?,?)");
			ps.setString(1, pName);
			ps.setString(2, logIp);
			ps.executeUpdate();
		} catch (SQLException ex) {
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute SQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close SQL connection: ", ex);
			}
		}
	}
	public String getAddress(String pName) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String logip = plugin.getConfiguration().getString("mysql-table-ip");
		try {
			conn = getConnection();
			ps = conn.prepareStatement("SELECT * FROM " + logip + " WHERE name = ?");
			ps.setString(1, pName);
			rs = ps.executeQuery();
			while (rs.next()){
				String ip = rs.getString("lastip");
				return ip;
			}
		} catch (SQLException ex) {
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute SQL statement: ", ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close SQL connection: ", ex);
			}
		}
		return null;
	}
	public boolean removeFromBanlist(String player) {

		String mysqlTable = plugin.getConfiguration().getString("mysql-table");

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement("DELETE FROM " + mysqlTable + " WHERE name = ? ORDER BY time DESC LIMIT 1");
			ps.setString(1, player);
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
	public void addPlayer(String player, String reason, String admin, long tempTime , int type){
		String mysqlTable = plugin.getConfiguration().getString("mysql-table");
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
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
		Connection conn = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String mysqlTable = plugin.getConfiguration().getString("mysql-table");
		try {
			ps = conn.prepareStatement("SELECT * FROM " + mysqlTable + " WHERE name = ?");
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
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String logip = plugin.getConfiguration().getString("mysql-table-ip");
		try {
			conn = getConnection();
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
		Connection conn = null;
		PreparedStatement ps = null;
		String logip = plugin.getConfiguration().getString("mysql-table-ip");
		try {
			System.out.println("trying to update address.");
			conn = getConnection();
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
			conn = getConnection();
			ps = conn.prepareStatement("SELECT * FROM banlist WHERE name = ? ORDER BY time DESC LIMIT 10");
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
	public EditBan loadFullRecord(String pName) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement("SELECT * FROM banlist WHERE name = ? ORDER BY time DESC LIMIT 1");
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
	public EditBan loadFullRecordFromId(int id) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
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

		String mysqlTable = plugin.getConfiguration().getString("mysql-table");

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
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
	public String resourceToString(String name) {
        InputStream input = UltraBan.class.getResourceAsStream("/default/" + name);
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
	
}
