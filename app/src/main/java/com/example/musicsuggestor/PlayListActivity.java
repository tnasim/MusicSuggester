package com.example.musicsuggestor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;

// Handles play list screen.
public class PlayListActivity extends AppCompatActivity {
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_list);
	}

PlayList songList;
	// Handles adding of location and returning to main screen.
	public void addSongToList(View view) {
		AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.locationEditText);
		String newSongName = editText.getText().toString(); // Name of the new song

		// Assign the current location to the location name.
		if (songList.GetSong(newSongName) == null)
			songList.AddSong(new Song(newSongName));

		returnToMain(view);
	}

	// Handles when user wants to name the current location.
	public void returnToMain(View view) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
}
