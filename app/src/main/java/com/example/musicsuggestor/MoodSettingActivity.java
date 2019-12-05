package com.example.musicsuggestor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.HashMap;
import java.util.Map;

public class MoodSettingActivity extends AppCompatActivity {

	Spinner spinner;
	String[] moodList;

	public static final Map<String, SongStatus> mapMoodSongStatus = new HashMap<String, SongStatus>() {{
		put("Angry", new SongStatus(1, 1.0f, .5f));
		put("Apathetic", new SongStatus(3, 1.25f, .75f));
		put("Frustrated", new SongStatus(4, 1.5f, 1.0f));
		put("Happy", new SongStatus(1, 1.0f, 1.0f));
		put("Sad", new SongStatus(4, 1.0f, 1.0f));
		put("Stressed", new SongStatus(4, 1.0f, .75f));
	}};

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mood_setting);

		moodList = getResources().getStringArray(R.array.moodList);

		spinner = (Spinner) findViewById(R.id.moodSpinner);

		ArrayAdapter<String> adapter = new ArrayAdapter<String> (this,
				android.R.layout.simple_spinner_item, moodList); //songList.GetListOfNames()

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner.setAdapter(adapter);


		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String mood = parent.getItemAtPosition(position).toString();
				Log.d("DEBUG", "======================== OnItemSelectedListener : " + mood);

				SongStatus songStatus  = MoodSettingActivity.mapMoodSongStatus.get(mood);
				PlayListActivity.currentVolume = songStatus.getVolume();
				PlayListActivity.currentSpeed = songStatus.getSpeed();
				PlayListActivity.updateSpeed();
				PlayListActivity.updateVolume();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				//Another interface callback
			}
		});
	}

	// Handles when user wants to name the current location.
	public void returnToMain(View view) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
}
