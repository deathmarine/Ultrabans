package com.modcrafting.ultrabans;

import com.boxysystems.jgoogleanalytics.JGoogleAnalyticsTracker;
import com.modcrafting.ultrabans.live.Config;
import com.modcrafting.ultrabans.live.gui.Frame;
import com.modcrafting.ultrabans.tracker.Track;

public class Main {
	public static void main(String[] args){
		Config cfg = new Config();
		JGoogleAnalyticsTracker tracker = new JGoogleAnalyticsTracker(cfg.getName(),cfg.getVersion(),"UA-35400100-2");
		new Track(tracker);
		Track.track(cfg.getName()+cfg.getVersion());
		new Frame(cfg);
	}
}
