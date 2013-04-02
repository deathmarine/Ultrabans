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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import com.modcrafting.ultrabans.Ultrabans;

public class SQLite extends Database{
	String dbname;
	Connection connection;
	public SQLite(Ultrabans instance){
		super(instance);
		dbname = plugin.getConfig().getString("SQLite.Filename", "banlist");
		bantable = "banlist";
		iptable = "banlistip";
	}
	public String SQLiteCreateBansTable = "CREATE TABLE IF NOT EXISTS banlist (" +
			"`name` TEXT," +
			"`reason` TEXT," + 
			"`admin` TEXT," + 
			"`time` INTEGER," + 
			"`temptime` INTEGER ," + 
			"`id` INTEGER PRIMARY KEY," + 
			"`type` INTEGER DEFAULT '0'" + 
			");";
	public String SQLiteCreateBanipTable = "CREATE TABLE IF NOT EXISTS banlistip (" +
			"`name` TEXT," + 
			"`lastip` TEXT," + 
			"PRIMARY KEY (`name`)" + 
			");";
	public Connection getSQLConnection() {
		File dataFolder = new File(plugin.getDataFolder(), dbname+".db");
		if (!dataFolder.exists()){
			try {
				dataFolder.createNewFile();
			} catch (IOException e) {
				plugin.getLogger().log(Level.SEVERE, "File write error: "+dbname+".db");
			}
		}
		try {
			if(connection!=null&&!connection.isClosed()){
				return connection;
			}
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
    		return connection;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
	    } catch (ClassNotFoundException ex) {
	    	plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
	    }
		return null;
    }
	
	public void load() {
		Connection conn = getSQLConnection();
		Statement s;
		try {
			s = conn.createStatement();
			s.executeUpdate(SQLiteCreateBansTable);
			s.executeUpdate(SQLiteCreateBanipTable);
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		initialize();
	}
}
