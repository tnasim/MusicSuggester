package com.example.musicsuggestor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

// Handles the user adjusting how the song plays.
public class AdjustmentActivity extends AppCompatActivity {
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adjustment);


		// Handle changes in the speed.
		speedSeekBar = (SeekBar)findViewById(R.id.seekBarSpeed);
		speedSeekBar.setMax(0);
		speedSeekBar.setMax(200);
		speedSeekBar.setProgress((int)(PlayListActivity.currentSpeed*100) );
		speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			// Handles when the value changes.
			@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)  {
				speedPct = progress;
			}

			// Handles when the user touches the tracker.
			@Override public void onStartTrackingTouch(SeekBar seekBar) {}

			// Handles when the user lifts up on tracking.
			@Override public void onStopTrackingTouch(SeekBar seekBar) {
				SetSongSpeed(speedPct);
			}
		});

		// Handle changes in the volume.
		volumeSeekBar = (SeekBar)findViewById(R.id.seekBarVolume);
		volumeSeekBar.setMax(0);
		volumeSeekBar.setMax(100);
		volumeSeekBar.setProgress((int)(PlayListActivity.currentSpeed*100) );
		volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			// Handles when the value changes.
			@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)  {
				volumePct = progress;
			}

			// Handles when the user touches the tracker.
			@Override public void onStartTrackingTouch(SeekBar seekBar) {}

			// Handles when the user lifts up on tracking.
			@Override public void onStopTrackingTouch(SeekBar seekBar) {
				SetSongVolume(volumePct);
			}
		});
	}

	// Sets the speed for the current song.
	public void SetSongSpeed(int speedPct) {
		PlayListActivity.currentSpeed = (float)(speedPct/100.0);
		PlayListActivity.updateSpeed();
		Log.d("DEBUG", "============== NEW Speed: " + PlayListActivity.currentSpeed);
	}

	// Sets the volume for the current song.
	public void SetSongVolume(int volumePct) {
		PlayListActivity.currentVolume = (float)(volumePct/100.0);
		PlayListActivity.updateVolume();
		Log.d("DEBUG", "============== NEW Volume: " + PlayListActivity.currentVolume);
	}

	// Handles when user wants to name the current location.
	public void returnToMain(View view) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	SeekBar speedSeekBar;   // Seek bar for the speed
	SeekBar volumeSeekBar;  // Seek bar for the volume
	private int speedPct;   // Pct for speed
	private int volumePct;  // Pct for volume
}
