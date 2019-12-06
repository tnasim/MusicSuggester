package com.example.musicsuggestor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

// Handles play list screen.
public class PlayListActivity extends AppCompatActivity {

	Spinner spinner;
	public static SongCategory DEFAULT_SONG_CATEGORY = SongCategory.rest;
	public static SongCategory currentSongCategory = DEFAULT_SONG_CATEGORY;

	public static MediaPlayer mediaPlayer;

	public static boolean flagFirstTime = true;

	public static int resumePosition = 0;

	ImageButton playButton;
	ImageButton pauseButton;

	LocationManager locManager;     // Manages locations
	LocationListener locListener;   // Listener for location changes
	Location curLocation;

	public static int currentMovementStatus = 1; // 1 == stationary, 2 = slow, 3 = fast
	public static int currentLocationNumber = 2;
	public static int currentTimeNumber = 4;

	public static long lastUpdate = System.currentTimeMillis();
	public static int lastLocation = 2;

	/**
	 * Initially we have 4 categories and 5 types of user movement-status.
	 * Ideally these values should be pulled out from a database and should be personalized for each user.
	 */
	public static final Map<String, SongStatus> mapMovementSongStatus = new HashMap<String, SongStatus>() {{
		put("rest", new SongStatus(1, 1.0f, .5f));
		put("walk", new SongStatus(3, 1.25f, .75f));
		put("workout", new SongStatus(4, 1.5f, 1.0f));
		put("study", new SongStatus(1, 1.0f, .6f));
		put("driving", new SongStatus(4, 1.0f, 1.0f));
	}};

	public static float currentVolume = getSongStatusBasedOnMovementType(PlayListActivity.currentSongCategory.toString()).getVolume();
	public static float currentSpeed = getSongStatusBasedOnMovementType(PlayListActivity.currentSongCategory.toString()).getSpeed();

	private TextView statusTextView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_list);

		playButton = findViewById(R.id.playButton);
		pauseButton = findViewById(R.id.pauseButton);

		statusTextView = (TextView) findViewById(R.id.statusText);
		statusTextView.setVisibility(View.VISIBLE);
		statusTextView.setBackgroundColor(Color.DKGRAY);
		statusTextView.setTextColor(Color.WHITE);

		UserLocation.setCurrentLocation(UserLocation.DEFAULT_LOCATION);

		if(mediaPlayer!=null) {
			if(mediaPlayer.isPlaying()) {
				pauseButton.setVisibility(View.VISIBLE);
				pauseButton.bringToFront();
				playButton.setVisibility(View.INVISIBLE);
			} else {
				playButton.setVisibility(View.VISIBLE);
				playButton.bringToFront();
				pauseButton.setVisibility(View.INVISIBLE);
			}
		}

		loadSavedPlaylist();

		spinner = (Spinner) findViewById(R.id.songs_spinner);

		if(songList.getSize() == 0) {
			Log.d("DEBUG", "====== song list empty ========");
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String> (this,
				android.R.layout.simple_spinner_item, songList.GetListOfNames()); //songList.GetListOfNames()

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner.setAdapter(adapter);

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if(flagFirstTime) {
					flagFirstTime = false;
				} else {
					String songName = parent.getItemAtPosition(position).toString();
					Log.d("DEBUG", "======================== Song selected : " + songName);
					playOrResumeSongByName(songName);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});



		if (checkPermission("ACCESS_FINE_LOCATION", 0, 0) == PackageManager.PERMISSION_GRANTED) {
			locListener = new LocationListener() {
				// Handles when the location changes.
				@Override
				public void onLocationChanged(Location loc) {
					final long currentTime = System.currentTimeMillis();
					long timeDiffInSeconds = (((currentTime - lastUpdate)/1000));
					final float changeInLocation = curLocation.distanceTo(loc);
					final float userSpeed = changeInLocation/timeDiffInSeconds; // meter per second: e.g. walking speed average 1.4 m/s

					if(userSpeed < 0.5) {
						currentMovementStatus = 1;
					} else if (userSpeed < 1.5) {
						currentMovementStatus = 2;
					} else {
						currentMovementStatus = 3;
					}

					// Get the actual location.
					curLocation = UserLocation.GetActualLocation(loc);
					UserLocation.setCurrentLocation(curLocation);
					Log.d("DEBUG", "Time Difference: " + timeDiffInSeconds);
//					statusTextView.setText("timeDiff(mins): " + timeDiffInSeconds/60.0f + "; changeInLocation: " + changeInLocation + "m; " + "loc1: " + lastLocation + "; loc2: " + currentLocationNumber);
					if (timeDiffInSeconds/60.0f > 0.5f && changeInLocation > UserLocation.LOCATION_UPDATE_RANGE) { // If location change happens within at least half minute.

						final UserLocation userLoc = UserLocation.GetUserLocation(curLocation);
						currentLocationNumber = userLoc.GetLocationNumber();

//						statusTextView.setText("Speed: " + userSpeed + "; distChange: " + changeInLocation + "m;" + "l1: " + lastLocation + "; l2: " + currentLocationNumber);

						if(currentLocationNumber != lastLocation) { // check if location changed

							statusTextView.setText("Updating loc: " + userLoc.GetName());

							currentTimeNumber = UserLocation.getCurrentTimeNumber();

							final String predictedMovement = MainActivity.getPredictedMovement(currentLocationNumber, currentTimeNumber, currentMovementStatus);

							SongStatus songStatus = getSongStatusBasedOnMovementType(predictedMovement);

							currentSpeed = songStatus.getSpeed();
							currentVolume = songStatus.getVolume();
							currentSongCategory = SongCategory.valueOf(predictedMovement);

							try {

								resetMusicPlayer();

							} catch (Exception e) {
								Log.e("Error", "Unable to resetMusicPlayer");
							}
							lastLocation = currentLocationNumber;
							lastUpdate = currentTime;
						}
					}

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

			locManager =
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
				Log.d("DEBUG", "============= User's Last Known Location: " + locManager.getLastKnownLocation("gps"));
				curLocation = UserLocation.GetActualLocation(locManager.getLastKnownLocation(bestProvider));
				UserLocation.setCurrentLocation(curLocation);
			}
			locManager.requestLocationUpdates(bestProvider, 1000, UserLocation.SAME_LOCATION_DISTANCE / 10, locListener);
		}

	}

	public void updateSpinner() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String> (this,
				android.R.layout.simple_spinner_item, songList.GetListOfNames()); //songList.GetListOfNames()

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner.setAdapter(adapter);
	}

	public static void updateSpeed() {
		if(mediaPlayer != null) {
			mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(currentSpeed));
		}
	}

	public static void updateVolume() {
		if(mediaPlayer != null) {
			mediaPlayer.setVolume(currentVolume, currentVolume);
		}
	}

	public static void addSong(Song song) {
		if(songList == null) {
			songList = new PlayList();
		}
		songList.AddSong(song);
	}

	/**
	 * Load the playlist from file
	 */
	public static void loadSavedPlaylist() {
		FileInputStream is;
		BufferedReader reader;

		Log.d("DEBUG", "Loading saved playlist.");

		File rootFolder = Environment.getExternalStorageDirectory();
		final File file = new File(rootFolder, "playlist.txt");

		songList = new PlayList();

		/* Load songs from file only if the list is empty now */
		if(songList.getSize() == 0) {
			try {
				if (!file.exists()) {
					file.createNewFile();
				}
				is = new FileInputStream(file);
				reader = new BufferedReader(new InputStreamReader(is));
				String line = reader.readLine();
				while (line != null) {
					String[] values = line.split(",");
					String name = values[0];
					int number = Integer.parseInt(values[1]);
					String path = values[2];
					SongCategory category = SongCategory.valueOf(values[3]);
					Log.d("DEBUG", "Adding song: " + name);
					songList.AddSong(new Song(name, number, path, category));

					line = reader.readLine();
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("Error", "Playlist file not found" + e);
			}
		}
	}

	/**
	 * Save palylist into a file
	 */
	public static void savePlaylist() {
		File root = Environment.getExternalStorageDirectory();
		Log.d("DEBUG","Saving playlist...");
		File dir = new File (root.getAbsolutePath());
		dir.mkdirs();
		File file = new File(dir, "playlist.txt");
		if(file.exists()) {
			file.delete();
		}

		try {
			file = new File(dir, "playlist.txt");
			file.createNewFile();
			FileOutputStream f = new FileOutputStream(file, true);
			PrintWriter pw = new PrintWriter(f);
			for(Song song: songList.getSongList()) {
				Log.d("DEBUG","song: " + song.GetName());
				pw.append(song.GetName() + "," + song.GetSongNumber() + "," + song.getFileLocation() + "," + song.getCategory());
				pw.append('\n');
			}
			pw.flush();
			pw.close();
			f.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.i("ERROR", "Playlist file not found." + e.getMessage());
		} catch (IOException e) {
			Log.e("Error", "Error while handling playlist file.");
			e.printStackTrace();
		}
		Log.d("DEBUG", "Playlist saved.");
	}

	public static SongStatus getSongStatusBasedOnMovementType(String movementType) {
		return mapMovementSongStatus.get(movementType);
	}

	public static PlayList songList;
	// Handles adding of location and returning to main screen.
	public void addSongToList(View view) {

		AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.songEditText);
		String newSongName = editText.getText().toString(); // Name of the new song

		if (songList.GetSong(newSongName) == null)
			songList.AddSong(new Song(newSongName, songList.getSize()+1));

		Log.d("DEBUG", "New song added");
		Log.d("DEBUG", "=========== Current songs: ============");
		for(String songname: songList.GetListOfNames()) {
			Log.d("DEBUG", songname);
		}
		Log.d("DEBUG", "=========== ============== ============");

		savePlaylist();
		updateSpinner();

		returnToMain(view);
	}

	public void removeSongFromList(View view) {

		AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.songEditText);
		final String songToRemove = editText.getText().toString(); // Name of the new song


		if(songList.removeSongByName(songToRemove) ) {
			Log.d("DEBUG", "Song deleted: " + songToRemove);

			Log.d("DEBUG", "=========== Current songs: ============");
			for (String songname : songList.GetListOfNames()) {
				Log.d("DEBUG", songname);
			}
			Log.d("DEBUG", "=========== ============== ============");

			savePlaylist();
			updateSpinner();
			returnToMain(view);
		} else {
			PlayListActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(PlayListActivity.this, "Unable to remove: " + songToRemove,Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	private void playOrResumeSong() {
		Log.d("DEBUG", "SEEK Position: " + resumePosition);
		if(mediaPlayer == null) {
			SongStatus songStatus = getSongStatusBasedOnMovementType(PlayListActivity.currentSongCategory.toString());

			currentVolume = songStatus.getVolume();  //*** Get new volume from machine learning
			currentSpeed = songStatus.getSpeed();   //*** Get new speed from machine learning

			String audioPath;
			File rootFolder = Environment.getExternalStorageDirectory();
			String categoryFolder = rootFolder.getPath() + "/songs/" + PlayListActivity.currentSongCategory.getValue() + "/";

			Log.d("DEBUG", "============= categoryFolder: " + categoryFolder);
			File catFolder = new File(categoryFolder);
			if(catFolder.exists()) {
				File[] files = catFolder.listFiles();
				Random rand = new Random();
				File file = files[rand.nextInt(files.length)];
				audioPath = file.getPath();
				mediaPlayer = MediaPlayer.create(this, Uri.parse(audioPath));
				if(mediaPlayer != null) {
					mediaPlayer.setVolume(currentVolume, currentVolume);
					mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(currentSpeed));
					mediaPlayer.setLooping(true);
					mediaPlayer.start();
					String songName = "";
					try {
						songName = file.getName().replaceFirst("[.][^.]+$", "");;
						selectSpinnerItemByValue(spinner, songName);
						statusTextView.setText("Playing: " + songName);
					} catch (Exception e) {
						Log.e("ERROR", "Error selecting spinner item. " + songName);
					}
				}
			} else {
				PlayListActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(PlayListActivity.this, "No song available in INTERNAL_STORAGE/songs/ folder",Toast.LENGTH_SHORT).show();
					}
				});
				return;
			}
		} else if (!mediaPlayer.isPlaying()) {
			Log.d("DEBUG", "Setting Media Player seek position to : " + resumePosition);
			if(resumePosition == 0) {
				try {
					mediaPlayer.prepare();
				} catch (IOException ioex) {
					Log.e("ERROR", "Error while preparing song: " + ioex);
				}
			}
			mediaPlayer.seekTo(resumePosition);
		}

		mediaPlayer.start();

		pauseButton.setVisibility(View.VISIBLE);
		pauseButton.bringToFront();
		playButton.setVisibility(View.INVISIBLE);
	}

	private void playOrResumeSongByName(final String songName) {
		Log.d("DEBUG", "SEEK Position: " + resumePosition);
		stopSong();
		mediaPlayer = null;

		Song songToPlay = songList.GetSong(songName);
		Log.d("DEBUG", "============== Song to play: " + songToPlay.getCategory());

		SongStatus songStatus = getSongStatusBasedOnMovementType(songToPlay.getCategory().toString());

		currentVolume = songStatus.getVolume();  //*** Get new volume from machine learning
		currentSpeed = songStatus.getSpeed();   //*** Get new speed from machine learning

		String audioPath;
		File rootFolder = Environment.getExternalStorageDirectory();
		String categoryFolder = rootFolder.getPath() + "/songs/" + (songToPlay.getCategory().getValue() ) + File.separator;

		Log.d("DEBUG", "============= categoryFolder: " + categoryFolder);
		File catFolder = new File(categoryFolder);
		if(catFolder.exists()) {

			Log.d("DEBUG", "============= Playing: " + categoryFolder  + songName + ".mp3");
			File file = new File(categoryFolder + File.separator + songName + ".mp3");
			if(file.exists()) {
				audioPath = file.getPath();
				mediaPlayer = MediaPlayer.create(this, Uri.parse(audioPath));
				if (mediaPlayer != null) {
					mediaPlayer.setVolume(currentVolume, currentVolume);
					mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(currentSpeed));
					mediaPlayer.setLooping(true);
					mediaPlayer.start();
					statusTextView.setText("Playing: " + songName);
				}
			} else {
				PlayListActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(PlayListActivity.this, songName + "  Not Found.",Toast.LENGTH_SHORT).show();
					}
				});
			}
		} else {
			PlayListActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(PlayListActivity.this, "No song available in INTERNAL_STORAGE/songs/ folder",Toast.LENGTH_SHORT).show();
				}
			});
			return;
		}

		if(mediaPlayer != null) {
			mediaPlayer.start();
			pauseButton.setVisibility(View.VISIBLE);
			pauseButton.bringToFront();
			playButton.setVisibility(View.INVISIBLE);
		}
	}

	public void playOrResume(View view) {
		playOrResumeSong();
	}

	private void stopSong() {
		if (mediaPlayer == null) return;
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
			resumePosition = 0;
			playButton.setVisibility(View.VISIBLE);
			playButton.bringToFront();
			pauseButton.setVisibility(View.INVISIBLE);
		}
	}

	public void stop(View view) {
		stopSong();
	}

	private void pauseSong() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			resumePosition = mediaPlayer.getCurrentPosition();
			playButton.setVisibility(View.VISIBLE);
			playButton.bringToFront();
			pauseButton.setVisibility(View.INVISIBLE);
		}
	}

	public void pause(View view) {
		pauseSong();
	}

	private void resetMusicPlayer() {
		stopSong();
		mediaPlayer = null;
		playOrResumeSong();
	}

	public void resetPlayer(View view) {
		resetMusicPlayer();

	}

	// Handles when user wants to name the current location.
	public void returnToMain(View view) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	public static PlayList getSongList() {
		return songList;
	}

	public static void selectSpinnerItemByValue(Spinner spnr, String value) {
		ArrayAdapter<String> adapter = (ArrayAdapter<String>) spnr.getAdapter();
		for (int position = 0; position < adapter.getCount(); position++) {
			String currentValue = adapter.getItem(position);
			if(currentValue.equalsIgnoreCase(value)) {
				spnr.setSelection(position);
				return;
			}
		}
	}
}
