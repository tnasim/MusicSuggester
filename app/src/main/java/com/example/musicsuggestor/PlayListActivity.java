package com.example.musicsuggestor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Spinner;
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
	public static SongCategory DEFAULT_SONG_CATEGORY = SongCategory.study;
	public static SongCategory currentSongCategory = DEFAULT_SONG_CATEGORY;

	public static MediaPlayer mediaPlayer;

	public static int resumePosition = 0;

	ImageButton playButton;
	ImageButton pauseButton;

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

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_list);

		playButton = findViewById(R.id.playButton);
		pauseButton = findViewById(R.id.pauseButton);

		loadSavedPlaylist();

		spinner = (Spinner) findViewById(R.id.songs_spinner);

		/*String[] songs = new String[3];
		songs[0] = "song1";
		songs[1] = "song2";
		songs[2] = "song3";*/

		if(songList.getSize() == 0) {
			Log.d("DEBUG", "====== song list empty ========");
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String> (this,
				android.R.layout.simple_spinner_item, songList.GetListOfNames()); //songList.GetListOfNames()

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner.setAdapter(adapter);

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
					Log.d("DEBUG", "Adding song: " + name);
					songList.AddSong(new Song(name, number));

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
				pw.append(song.GetName() + "," + song.GetSongNumber());
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

	public void predictUserStatus(final View view) {
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
//					Toast.makeText(getApplicationContext(),"Selected movement: ",Toast.LENGTH_SHORT).show();
					PlayListActivity.currentSongCategory = SongCategory.valueOf(predictedMovement);

					PlayListActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(PlayListActivity.this, "Selected movement: " + predictedMovement,Toast.LENGTH_SHORT).show();
						}
					});

					conn.disconnect();
				} catch (Exception e) {
					e.printStackTrace();
					PlayListActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(PlayListActivity.this, "Problem connecting to the server.",Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		});

		thread.start();
	}

	public static SongStatus getSongStatusBasedOnMovementType(String movementType) {
		return mapMovementSongStatus.get(movementType);
	}

	private static PlayList songList;
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

	public void playOrResume(View view) {
		Log.d("DEBUG", "SEEK Position: " + resumePosition);
		if(mediaPlayer == null) {
			SongStatus songStatus = getSongStatusBasedOnMovementType(PlayListActivity.currentSongCategory.toString());

			currentVolume = songStatus.getVolume();  //*** Get new volume from machine learning
			currentSpeed = songStatus.getSpeed();   //*** Get new speed from machine learning

			String audioPath;
			File rootFolder = Environment.getExternalStorageDirectory();
			String categoryFolder = rootFolder.getPath() + "/songs/" + PlayListActivity.currentSongCategory.getValue() + "/";

			File catFolder = new File(categoryFolder);
			if(catFolder.exists()) {
				File[] files = catFolder.listFiles();
				Random rand = new Random();
				File file = files[rand.nextInt(files.length)];
				audioPath = file.getPath();
				mediaPlayer = MediaPlayer.create(this, Uri.parse(audioPath));
				mediaPlayer.setVolume(currentVolume, currentVolume);
				mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(currentSpeed));
				mediaPlayer.setLooping(true);
				mediaPlayer.start();
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

		pauseButton.setVisibility(view.VISIBLE);
		pauseButton.bringToFront();
		playButton.setVisibility(view.INVISIBLE);
	}

	public void stop(View view) {
		if (mediaPlayer == null) return;
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
			resumePosition = 0;
			playButton.setVisibility(view.VISIBLE);
			playButton.bringToFront();
			pauseButton.setVisibility(view.INVISIBLE);
		}
	}

	public void pause(View view) {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			resumePosition = mediaPlayer.getCurrentPosition();
			playButton.setVisibility(view.VISIBLE);
			playButton.bringToFront();
			pauseButton.setVisibility(view.INVISIBLE);
		}
	}

	public void resetPlayer(View view) {
		stop(view);
		mediaPlayer = null;
		playOrResume(view);

	}

	// Handles when user wants to name the current location.
	public void returnToMain(View view) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	public static PlayList getSongList() {
		return songList;
	}
}
