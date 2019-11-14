package com.example.musicsuggestor;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import static androidx.core.content.ContextCompat.getSystemService;

// Handles listening for location updates by the app.
public class AppLocationListener implements LocationListener {
	// Handles when the location changes.
	@Override public void onLocationChanged(Location loc) {
	}

	// Handles when the provider is disabled.
	@Override public void onProviderDisabled(String provider) {}

	// Handles when the provider is enabled.
	@Override public void onProviderEnabled(String provider) {}

	// Handles when the status changes.
	@Override public void onStatusChanged(String provider, int status, Bundle extras) {}
}
