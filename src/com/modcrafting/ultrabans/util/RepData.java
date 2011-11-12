package com.modcrafting.ultrabans.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;
import com.modcrafting.ultrabans.UltraBan;
//DISABLED...
public class RepData {
	static UltraBan plugin;
	protected String SQLCreateRepTable = "CREATE TABLE IF NOT EXISTS `reptable` (" +
			"`name` varchar(32) NOT NULL," +
			"`time` varchar(32) NOT NULL," + 
			"`rep` int(11) NOT NULL," + 
			"PRIMARY KEY (`name`) USING BTREE" + 
			") ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC;";
	public static Connection getSQLConnection() {

    	YamlConfiguration Config = (YamlConfiguration) plugin.getConfig();
		//Configuration Config = new Configuration(new File("plugins/UltraBan/config.yml"));
		//Config.load();
		String mysqlDatabase = Config.getString("mysql-database","jdbc:mysql://localhost:3306/minecraft");
		String mysqlUser = Config.getString("mysql-user","root");
		String mysqlPassword = Config.getString("mysql-password","root");

		try {

			return DriverManager.getConnection(mysqlDatabase + "?autoReconnect=true&user=" + mysqlUser + "&password=" + mysqlPassword);
		} catch (SQLException ex) {
			
			UltraBan.log.log(Level.SEVERE, "Unable to retreive connection", ex);
		}
		return null;
	}
	public void initialize(UltraBan plugin){
		this.plugin = plugin;
		Connection conn = getSQLConnection();
		
		if (conn == null) {
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Could not establish SQL connection. Disabling UltraBan");
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Adjust Settings in Config or set MySql: False");
			plugin.getServer().getPluginManager().disablePlugin(plugin);
			return;
		} else {
			Statement st = null;
			
			try {
				conn.setAutoCommit(false);
        		st = conn.createStatement();
        		st.execute(this.SQLCreateRepTable);
        		conn.commit();
				
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
			} finally {
				try {
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
		    			
	public static void addRep(String player, int rep){
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("REPLACE INTO reptable (name,time,rep) VALUES(?,?,?)");
			ps.setString(1, player);
			ps.setLong(2, System.currentTimeMillis()/1000);
			ps.setInt(3, rep);
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
	public static void addtoRep(String player){
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("REPLACE INTO reptable (name,time,rep) VALUES(?,?,?)");
			ps.setString(1, player);
			ps.setLong(2, System.currentTimeMillis()/1000);
			if(!initialRep(player)) ps.setInt(3, 100);
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
	public static int pullRep(String victim){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM reptable WHERE name = ? ORDER BY time DESC LIMIT 1");
			ps.setString(1, victim);
			rs = ps.executeQuery();
			while (rs.next()){
				int getRep = rs.getInt("rep");
				return getRep;
				}
			
		} catch (SQLException ex) {
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Couldn't execute MySQL statement: ", ex);
			return 0;
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				UltraBan.log.log(Level.SEVERE, "[UltraBan] Failed to close MySQL connection: ", ex);
				return 0;
			}
		}
		return 0;
	}
	public static boolean initialRep(String player){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT " + player + " FROM reptable ;");
			rs = ps.executeQuery();
			if (rs.getString("name") == player ) return true;
			
			
		} catch (SQLException ex) {
			UltraBan.log.log(Level.SEVERE, "[UltraBan] Player not found!");
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
		return true;
	}
}

