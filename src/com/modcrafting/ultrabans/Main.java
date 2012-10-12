package com.modcrafting.ultrabans;

import com.boxysystems.jgoogleanalytics.JGoogleAnalyticsTracker;
import com.modcrafting.ultrabans.gui.Frame;
import com.modcrafting.ultrabans.tracker.Track;

public class Main {
	public static void main(String[] args){
		JGoogleAnalyticsTracker tracker = new JGoogleAnalyticsTracker("Ultrabans Live","0.1 Beta","UA-35400100-1");
		new Track(tracker);
		Track.track("Ultrabans Live 0.1 Beta Loaded");
		new Frame();
	}
}
