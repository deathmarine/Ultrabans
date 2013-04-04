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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;
import com.modcrafting.ultrabans.Ultrabans;

public class SQL extends Database{
	private String database;
	private String username;
	private String password;
	public SQL(Ultrabans instance){
		super(instance);
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		database = config.getString("MySQL.Database","jdbc:mysql://localhost:3306/minecraft");
		username = config.getString("MySQL.User","root");
		password = config.getString("MySQL.Password","root");
		bantable = config.getString("MySQL.Table","banlist");
		iptable = config.getString("MySQL.IPTable","banlistip");
	}
	
	public Connection getSQLConnection() {
		try {
			if(connection!=null&&!connection.isClosed()){
				return connection;
			}
			Properties info = new Properties();
			info.put("autoReconnect", "true");
			info.put("user", username);
			info.put("password", password);
			info.put("useUnicode", "true");
			info.put("characterEncoding", "utf8");
			connection = DriverManager.getConnection(database,info);
			return connection;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
		return null;	
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
			") ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;";
	public String SQLCreateBanipTable = "CREATE TABLE IF NOT EXISTS %table% (" +
			"`name` varchar(32) NOT NULL," + 
			"`lastip` tinytext NOT NULL," + 
			"PRIMARY KEY (`name`)" + 
			") ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;";
	
	public void load() {
		Connection conn = getSQLConnection();
		try {
			PreparedStatement s = conn.prepareStatement(SQLCreateBansTable.replaceAll("%table%",bantable));
			s.execute();
			s = conn.prepareStatement(SQLCreateBansTable.replaceAll("%table%",iptable));
			s.execute();
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		initialize();
	}
}