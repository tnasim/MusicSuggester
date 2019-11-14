package com.example.musicsuggestor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SuggestionActivity extends AppCompatActivity {
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggestion);
	}

	// Handles when user wants to name the current location.
	public void returnToMain(View view) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
}
