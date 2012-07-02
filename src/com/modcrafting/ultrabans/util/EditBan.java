/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.util;

public class EditBan {

	public int id;
	public String name;
	public String reason;
	public String admin;
	public long time;
	public long endTime;
	public int type;
	
	public EditBan(int id, String name, String reason, String admin, long time, long endTime, int type){
		this.id = id;
		this.name = name;
		this.reason = reason;
		this.admin = admin;
		this.time = time;
		this.endTime = endTime;
		this.type = type;
	}
		
}
