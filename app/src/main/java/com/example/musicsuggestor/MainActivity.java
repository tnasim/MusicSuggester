package com.example.musicsuggestor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
	public static final String EXTRA_MESSAGE = "com.example.musicsuggestor.LOCATION";
	public static final String SERVER_URL = "http://10.218.104.158:5000/";

	private ProgressDialog progressDialog;

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

		btn_download_songs = findViewById(R.id.btnDownloadSongs);
		btn_download_songs.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] songList = getResources().getStringArray(R.array.songUriList);
				progressDialog = new ProgressDialog(MainActivity.this);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progressDialog.setMessage("Downloading ...");
				progressDialog.setCancelable(false);
				progressDialog.setMax(songList.length);
				progressDialog.show();

				/*for(String line: songList) {
					String[] splitted = line.split("\\|");
					String songUri = splitted[2];
					String songName = splitted[1];
					String songCategory = splitted[0];
					Song song = new Song()
				}*/
				String line = songList[0]; // The first line
				String[] splitted = line.split("\\|");
				String songUri = splitted[2];

				new DownloadFile(songList, 0).execute(songUri);
			}
		});

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
								URL url = new URL(MainActivity.SERVER_URL+"api/predict_song");
								HttpURLConnection conn = (HttpURLConnection) url.openConnection();
								conn.setRequestMethod("POST");
								conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
								conn.setRequestProperty("Accept","application/json");
								conn.setDoOutput(true);
								conn.setDoInput(true);

								JSONObject jsonParam = new JSONObject();
								jsonParam.put("location", 1);
								jsonParam.put("time", UserLocation.getCurrentTime());
								jsonParam.put("movement", UserLocation.getUserMobilityStatus());

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

								PlayListActivity.currentSpeed = songStatus.getSpeed();
								PlayListActivity.currentVolume = songStatus.getVolume();
								PlayListActivity.currentSongCategory = SongCategory.valueOf(predictedMovement);

								conn.disconnect();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

					// TODO Handle updates based on new location/speed.
				}

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

	// Handles when user wants to set the mood.
	public void sendToMoods(View view) {
		Intent intent = new Intent(this, MoodSettingActivity.class);
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

	private class DownloadFile extends AsyncTask<String, String, String> {

//		private ProgressDialog progressDialog;
		private String fileName;
		private String folder;
		private String category;
		private int currentIndex;
		private boolean isDownloaded;
		private String[] songs;

		public DownloadFile(String[] songList, int currentIndex) {

			this.currentIndex = currentIndex;
			progressDialog.setProgress(currentIndex+1);

			songs = new String[songList.length];
			System.arraycopy(songList, 0, songs, 0, songList.length);

			String[] splitted = songList[currentIndex].split("\\|");
			this.fileName = splitted[1];
			this.category = splitted[0];
			String filePath = Environment.getExternalStorageDirectory() + File.separator + "songs" + File.separator + this.category + File.separator + fileName + ".mp3";

			SongCategory cat;
			switch (Integer.parseInt(category)) {
				case 1:
					cat = SongCategory.rest;
					break;
				case 2:
					cat = SongCategory.walk;
					break;
				case 3:
					cat = SongCategory.workout;
					break;
				case 4:
					cat = SongCategory.study;
					break;
				case 5:
					cat = SongCategory.driving;
					default:
						cat = SongCategory.rest;
			}

			PlayListActivity.addSong(new Song(fileName, this.currentIndex+1, filePath, cat));
		}

		/**
		 * Before starting background thread
		 * Show Progress Bar Dialog
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		/**
		 * Downloading file in background thread
		 */
		@Override
		protected String doInBackground(String... f_url) {
			int count;
			try {
				URL url = new URL(f_url[0]);
				URLConnection connection = url.openConnection();
				connection.connect();
				int lengthOfFile = connection.getContentLength();


				// input stream to read file - with 8k buffer
				InputStream input = new BufferedInputStream(url.openStream(), 8192);

				//External directory path to save file
				folder = Environment.getExternalStorageDirectory() + File.separator + "songs" + File.separator + this.category + File.separator;

				//Create androiddeft folder if it does not exist
				File directory = new File(folder);

				if (!directory.exists()) {
					directory.mkdirs();
				}

				// Output stream to write file
				OutputStream output = new FileOutputStream(folder + fileName + ".mp3");

				byte data[] = new byte[1024];

				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;
					// publishing the progress....
					// After this onProgressUpdate will be called
					publishProgress("" + (int) ((total * 100) / lengthOfFile));
					Log.d("DEBUG", "Progress: " + (int) ((total * 100) / lengthOfFile));

					// writing data to file
					output.write(data, 0, count);
				}

				// flushing output
				output.flush();

				// closing streams
				output.close();
				input.close();
				return "Downloaded at: " + folder + fileName;

			} catch (Exception e) {
				Log.e("Error: ", e.getMessage());
			}

			return "Something went wrong";
		}

		/**
		 * Updating progress bar
		 */
		protected void onProgressUpdate(String... progress) {
			// setting progress percentage
//			progressDialog.setProgress(Integer.parseInt(progress[0]));
		}


		@Override
		protected void onPostExecute(String message) {
			// dismiss the dialog after the file was downloaded
//			this.progressDialog.dismiss();
			if(this.currentIndex < songs.length-1) {
				String line = songs[currentIndex + 1]; // take the next song
				String[] splitted = line.split("\\|");
				String songUri = splitted[2];
				new DownloadFile(songs, currentIndex + 1).execute(songUri);
			} else {
				PlayListActivity.savePlaylist();
				progressDialog.dismiss();
			}

			Toast.makeText(getApplicationContext(),
					message, Toast.LENGTH_LONG).show();
		}
	}

	LocationManager locManager;     // Manages locations
	LocationListener locListener;   // Listener for location changes
	Location curLocation;           // Current location

	Button btn_download_songs;
}