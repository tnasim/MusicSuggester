package com.example.musicsuggestor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
	public static final String EXTRA_MESSAGE = "com.example.musicsuggestor.LOCATION";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the location handling.
//		LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//		LocationListener locationListener = new AppLocationListener();
//		if (checkPermission("ACCESS_FINE_LOCATION", 0, 0) == PackageManager.PERMISSION_GRANTED)
//			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);

		// Set up the motion handling.
		SensorManager sensorManager;
		Sensor sensor;
		TriggerEventListener triggerEventListener;

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		triggerEventListener = new TriggerEventListener() {
			// Handles when accelerometer is triggered.
			@Override public void onTrigger(TriggerEvent event) {

			}
		};

		sensorManager.requestTriggerSensor(triggerEventListener, sensor);

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
// Change this to use the current location as the message.???
//		intent.putExtra(EXTRA_MESSAGE, "location");
		startActivity(intent);
	}
}
