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
	
	public String toCode(){
		return toCode(id);
	}
	
	public static BanType fromID(int type){
		for(BanType tp:BanType.values())
			if(tp.getId()==type)
				return tp;
		return BAN;
	}

	public static String toCode(int num){
		switch(num){
			case 0: return "B";
			case 1: return "IP";
			case 2: return "W";
			case 3: return "K";
			case 4: return "F";
			case 5: return "UN";
			case 6: return "J";
			case 7: return "M";
			case 9: return "PB";
			default: return "?";
		}
	}
}
