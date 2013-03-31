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
package com.modcrafting.ultrabans.util;

public class BanInfo {

	public int id;
	public String name;
	public String reason;
	public String admin;
	public long time;
	public long endTime;
	public int type;
	
	public BanInfo(int id, String name, String reason, String admin, long time, long endTime, int type){
		this.id = id;
		this.name = name;
		this.reason = reason;
		this.admin = admin;
		this.time = time;
		this.endTime = endTime;
		this.type = type;
	}
		
}
