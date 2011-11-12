package com.modcrafting.ultrabans.util;

import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Rep {
	UltraBan plugin;
	//DISABLED...
	public static boolean useRepBan(){ //config boolean
		return true;
	}
	
	public static void onBan(Player player){
		String victim = player.getName();
		if(useRepBan()){
			int brep = RepData.pullRep(victim);
			if(brep == 100) brep = 85;
			if(brep < 100) brep = brep*3/5;
			RepData.addRep(victim, brep);
		}
	}
	public void onKick(Player player){
		String victim = player.getName();
		if(useRepBan()){
			int krep = plugin.repdb.pullRep(victim);
			if(krep == 100) krep = 90;
			if(krep < 100) krep = krep*3/5;
			RepData.addRep(victim, krep);
		}
	}
	public void onWarn(Player player){
		String victim = player.getName();
		if(useRepBan()){
			int wrep = plugin.repdb.pullRep(victim);
			if(wrep == 100) wrep = 95;
			if(wrep < 100) wrep = wrep*3/5;
			RepData.addRep(victim, wrep);
		}
	}
	public void onJail(Player player){
		String victim = player.getName();
		if(useRepBan()){
			int jrep = plugin.repdb.pullRep(victim);
			if(jrep == 100) jrep = 95;
			if(jrep < 100) jrep = jrep*3/5;
			RepData.addRep(victim, jrep);
		}
	}
}
