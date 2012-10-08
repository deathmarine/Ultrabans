/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import com.modcrafting.ultrabans.Ultrabans;

public class DataHandler {
	Ultrabans plugin;
	public DataHandler(Ultrabans instance) {
		plugin = instance;
	}
	public void createDefaultConfiguration(String name) {
		File actual = new File(plugin.getDataFolder(), name);
		if (!actual.exists()) {

			InputStream input =	this.getClass().getResourceAsStream("/" + name);
			if (input != null) {
				FileOutputStream output = null;

				try {
					output = new FileOutputStream(actual);
					byte[] buf = new byte[8192];
					int length = 0;
					while ((length = input.read(buf)) > 0) {
						output.write(buf, 0, length);
					}

					System.out.println(plugin.getDescription().getName()
							+ ": Default configuration file written: " + name);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (input != null)
							input.close();
					} catch (IOException e) {}

					try {
						if (output != null)
							output.close();
					} catch (IOException e) {}
				}
			}
		}
	}
	public boolean deletePlyrdat(String name){
		if(plugin.getServer().getOfflinePlayer(name)!=null&&!plugin.getServer().getOfflinePlayer(name).isOnline()){
			 return new File(plugin.getServer().getWorlds().get(0).getName()+"/players/",name+".dat").delete();
		}
		return false;
	}
}
