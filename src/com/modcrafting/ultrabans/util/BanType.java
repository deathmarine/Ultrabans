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

public enum BanType {
	BAN(0),
	IPBAN(1),
	WARN(2),
	KICK(3),
	UNBAN(5),
	JAIL(6),
	MUTE(7),
	INFO(8),
	PERMA(9),
	TEMPBAN(10),
	TEMPIPBAN(11),
	TEMPJAIL(12);
	int id;
	private BanType(int i){
		id=i;
	}
	public int getId(){
		return id;
	}
	public static BanType fromID(int type){
		for(BanType tp:BanType.values())
			if(tp.getId()==type)
				return tp;
		return BAN;
	}
}
