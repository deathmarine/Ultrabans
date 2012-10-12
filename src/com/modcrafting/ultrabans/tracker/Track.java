package com.modcrafting.ultrabans.tracker;

import com.boxysystems.jgoogleanalytics.FocusPoint;
import com.boxysystems.jgoogleanalytics.JGoogleAnalyticsTracker;

public class Track {
	static JGoogleAnalyticsTracker tracker;
	public Track(JGoogleAnalyticsTracker t) {
		tracker=t;
	}
	public static void track(String point){
		if(tracker!=null){
			FocusPoint p = new FocusPoint(point);
			tracker.trackSynchronously(p);
		}
	}
}
