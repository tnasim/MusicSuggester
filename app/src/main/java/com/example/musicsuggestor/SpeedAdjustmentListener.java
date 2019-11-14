package com.example.musicsuggestor;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

// Handles listening for changes to the speed.
public class SpeedAdjustmentListener implements OnSeekBarChangeListener {
	// Handles when the value changes.
	@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)  {
/***Progress is the percentage of the way between the minimum and maximum values.***/
	}

	// Handles when the user touches the tracker.
	@Override public void onStartTrackingTouch(SeekBar seekBar) {}

	// Handles when the user lifts up on tracking.
	@Override public void onStopTrackingTouch(SeekBar seekBar) {
/***Send machine learning the results of the change.***/
	}
}
