package com.modcrafting.ultrabans.util;

public enum BanType {
	BAN(0),
	IPBAN(1),
	WARN(2),
	KICK(3),
	UNBAN(5),
	JAIL(6),
	MUTE(7),
	PERMA(9);
	int id;
	private BanType(int i){
		id=i;
	}
	public int getId(){
		return id;
	}
}
