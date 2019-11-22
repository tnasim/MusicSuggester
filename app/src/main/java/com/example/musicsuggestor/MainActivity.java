package com.example.musicsuggestor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
	public static final String EXTRA_MESSAGE = "com.example.musicsuggestor.LOCATION";

	// Handles when the app is created.
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the location handling (if allowed).
		if (checkPermission("ACCESS_FINE_LOCATION", 0, 0) == PackageManager.PERMISSION_GRANTED) {
			locListener = new LocationListener() {
				// Handles when the location changes.
				@Override
				public void onLocationChanged(Location loc) {
					int userSpeed = (int)curLocation.getSpeed();    // Get the speed in meters/second

					// Get the actual location.
					curLocation = UserLocation.GetActualLocation(loc);

					// Handle updates based on new location/speed.
/***
 mediaPlayer (and possibly playbackParams) needs to be set up elsewhere, but this would be how to change the volume and speed.
 Note that setPlaybackParams requires API level of at least 23.
 This code will need called in the appropriate location in AdjustmentActivity as well.
					int newVolume = 1;  //*** Get new volume from machine learning
					int newSpeed = 1;   //*** Get new speed from machine learning
					MediaPlayer mediaPlayer = new MediaPlayer();
					mediaPlayer.setVolume(newVolume, newVolume);
					mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(newSpeed));
*/				}

				// Handles when the provider is disabled.
				@Override
				public void onProviderDisabled(String provider) {
				}

				// Handles when the provider is enabled.
				@Override
				public void onProviderEnabled(String provider) {
				}

				// Handles when the status changes.
				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) {
				}
			};

			// Criteria needed to determine the best provider.
			Criteria c = new Criteria();
			c.setAccuracy(Criteria.ACCURACY_COARSE);
			c.setAltitudeRequired(false);
			c.setBearingRequired(false);
			c.setSpeedRequired(false);
			c.setCostAllowed(true);
			c.setPowerRequirement(Criteria.POWER_HIGH);
			String bestProvider = locManager.getBestProvider(c, true);

			// Set the current location and the listener for location/speed updates.
			curLocation = UserLocation.GetActualLocation(locManager.getLastKnownLocation(bestProvider));
			locManager.requestLocationUpdates(bestProvider, 1000, UserLocation.SAME_LOCATION_DISTANCE / 10, locListener);
		};
	}

	// Handles when user wants to go to the play list.
	public void sendToPlayList(View view) {
		Intent intent = new Intent(this, PlayListActivity.class);
		startActivity(intent);
	}

	// Handles when user wants to get a song suggestion.
	public void sendToSongSuggester(View view) {
		Intent intent = new Intent(this, SuggestionActivity.class);
		startActivity(intent);
	}

	// Handles when user wants to go to make adjustments to the current song.
	public void sendToAdjustments(View view) {
		Intent intent = new Intent(this, AdjustmentActivity.class);
		startActivity(intent);
	}

	// Handles when user wants to name the current location.
	public void sendToLocations(View view) {
		Intent intent = new Intent(this, SetUpIDActivity.class);

		// Add the current location to the intent.
		intent.putExtra("latitude", curLocation.getLatitude());
		intent.putExtra("longitude", curLocation.getLongitude());
		startActivity(intent);
	}

	LocationManager locManager;     // Manages locations
	LocationListener locListener;   // Listener for location changes
	Location curLocation;           // Current location
}