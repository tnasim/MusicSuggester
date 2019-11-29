package com.example.musicsuggestor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
	public static final String EXTRA_MESSAGE = "com.example.musicsuggestor.LOCATION";

	// Handles when the app is created.
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		PlayListActivity.loadSavedPlaylist();

		if (ContextCompat.checkSelfPermission(MainActivity.this,
				Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {

			ActivityCompat.requestPermissions(MainActivity.this,
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
					1);
		}

		if (ContextCompat.checkSelfPermission(MainActivity.this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {

			ActivityCompat.requestPermissions(MainActivity.this,
					new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
					1);
		}

		if (ContextCompat.checkSelfPermission(MainActivity.this,
				Manifest.permission.READ_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {

			ActivityCompat.requestPermissions(MainActivity.this,
					new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
					1);
		}

		// Set up the location handling (if allowed).
		if (checkPermission("ACCESS_FINE_LOCATION", 0, 0) == PackageManager.PERMISSION_GRANTED) {
			locListener = new LocationListener() {
				// Handles when the location changes.
				@Override
				public void onLocationChanged(Location loc) {
					int userSpeed = (int)curLocation.getSpeed();    // Get the speed in meters/second

					// Get the actual location.
					curLocation = UserLocation.GetActualLocation(loc);
					UserLocation.setCurrentLocation(curLocation);

					// TODO reuse the code from PlayListActivity.java
					Thread thread = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								URL url = new URL("http://10.218.104.158:5000/api/predict_song");
								HttpURLConnection conn = (HttpURLConnection) url.openConnection();
								conn.setRequestMethod("POST");
								conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
								conn.setRequestProperty("Accept","application/json");
								conn.setDoOutput(true);
								conn.setDoInput(true);

								JSONObject jsonParam = new JSONObject();
								jsonParam.put("location", 1);
								jsonParam.put("time", 1);
								jsonParam.put("movement", 1);

								Log.i("JSON", jsonParam.toString());
								DataOutputStream os = new DataOutputStream(conn.getOutputStream());
								//os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
								os.writeBytes(jsonParam.toString());

								os.flush();
								os.close();

								Log.i("STATUS", String.valueOf(conn.getResponseCode()));
								Log.i("MSG" , conn.getResponseMessage());

								InputStream stream = conn.getInputStream();

								BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));

								StringBuffer stringBuffer = new StringBuffer();
								String line;
								while ((line = bufferedReader.readLine()) != null)
								{
									stringBuffer.append(line);
								}

								JSONObject jsonData =  new JSONObject(stringBuffer.toString());
								final String predictedMovement = jsonData.getString("result");
								Log.i("INFO", "Result: " + predictedMovement);

								SongStatus songStatus = PlayListActivity.getSongStatusBasedOnMovementType(predictedMovement);

								// TODO Select a song from the suggested category (songStatus.getCategory() ) and set speed and volume suggested based on the predictedMovement.

								conn.disconnect();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

					// TODO Handle updates based on new location/speed.
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

			LocationManager locManager =
					(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

			final boolean gpsEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			if (!gpsEnabled) {
				Log.d("DEBUG", "==== GPS Provider not available");
			}

			String bestProvider = locManager.getBestProvider(c, true);
			Log.d("DEBUG", "========== bestProvider: " + bestProvider);

			// If no suitable provider is found, null is returned.
			if (bestProvider == null) {
				Log.d("DEBUG", "==== location provider not found");
				curLocation = UserLocation.GetActualLocation(locManager.getLastKnownLocation("gps"));
			} else {

				// Set the current location and the listener for location/speed updates.
//				Log.d("DEBUG", "============= User's Last Known Location: " + locManager.getLastKnownLocation("gps"));
				curLocation = UserLocation.GetActualLocation(locManager.getLastKnownLocation(bestProvider));
				UserLocation.setCurrentLocation(curLocation);
			}
			locManager.requestLocationUpdates(bestProvider, 1000, UserLocation.SAME_LOCATION_DISTANCE / 10, locListener);
		}
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