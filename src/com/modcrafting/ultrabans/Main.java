package com.modcrafting.ultrabans;

import com.boxysystems.jgoogleanalytics.FocusPoint;
import com.boxysystems.jgoogleanalytics.JGoogleAnalyticsTracker;
import com.modcrafting.ultrabans.gui.Frame;

public class Main {
	public static void main(String[] args){
		new Frame();
		JGoogleAnalyticsTracker tracker = new JGoogleAnalyticsTracker("Ultrabans Live","0.1 Beta","UA-35400100-1");
		FocusPoint focusPoint=new FocusPoint("Ultrabans Live 0.1 Beta Loaded");
		tracker.trackAsynchronously(focusPoint);
	}
}
