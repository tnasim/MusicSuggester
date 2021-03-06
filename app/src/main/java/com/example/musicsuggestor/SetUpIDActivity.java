package com.example.musicsuggestor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;

// Handles setting up location names.
public class SetUpIDActivity extends AppCompatActivity {

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_up_id);

		curLocation = UserLocation.getCurrentLocation();

		// Load the list of locations.
	}

	// Handles adding of location and returning to main screen.
	public void addLocationToList(View view) {
		AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.locationEditText);
		String newLocationName = editText.getText().toString(); // Name of the new location

		// Assign the current location to the location name.
		UserLocation.AddLocation(newLocationName, curLocation);

		returnToMain(view);
	}

	// Handles when user wants to name the current location.
	public void returnToMain(View view) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	private Location curLocation;   // Current location
}
