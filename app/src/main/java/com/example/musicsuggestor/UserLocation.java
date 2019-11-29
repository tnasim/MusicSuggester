package com.example.musicsuggestor;

import android.location.Location;

import java.util.ArrayList;

// Handles locations (both user-specified and physical).
public class UserLocation {
	// Constants - note that distances are in meters.
	public static final int NO_USER_LOCATION = 0;   // Indicates no user location assigned to this place
	public static final float SAME_LOCATION_DISTANCE = 40F; // Distance two locations can be within each other and be the same location

	public static Location currentLocation;
	// Class-level variables
	private static int nextLocationNumber = 0;      // Number of the next location
	private static ArrayList<UserLocation> locationList = new ArrayList<UserLocation>();    // List of locations
	private static ArrayList<String> nameList = new ArrayList<String>();    // List of strings

	// Constructor for location.  This is private because users should use location numbers instead.
	private UserLocation(String name, Location newLocation) {
		SetLocation(newLocation);
		locNumber = nextLocationNumber++;

		locationList.add(this);
		nameList.add(name);
	}

	public static Location getCurrentLocation() {
		return currentLocation;
	}

	public static void setCurrentLocation(Location location) {
		UserLocation.currentLocation = location;
	}

	// Assigns the specified location to the user location.
	public static void AddLocation(String name, Location newLocation) {
		UserLocation curLoc;    // Current location

		// If the location is new, add it to the list.
		if ((curLoc = GetUserLocation(name)) == null)
			curLoc = new UserLocation(name, newLocation);
		else
			curLoc.SetLocation(newLocation);
	}

	// Returns the location to use for the specified location.
	public static Location GetActualLocation(Location physicalLocation) {
		for (UserLocation curLoc : locationList) {
			if (curLoc.IsLocationInHere(physicalLocation))
				return curLoc.GetLocation();
		}

		return physicalLocation;
	}

	// Returns the number of the specified location.
	public static int GetUserLocationNumber(Location physicalLocation) {
		UserLocation userLoc = GetUserLocation(physicalLocation);   // Location of the user

		// Return the number (or default value if no location matches).
		if (userLoc == null)
			return NO_USER_LOCATION;
		return userLoc.GetLocationNumber();
	}

	public static int getUserMobilityStatus() {
		// TODO need to return values like stationary, slow and fast etc based on accelerometer data. Need to have a map of values.
		return 1;
	}

	public static int getCurrentTime() {
		// TODO return time information e.g. 'night', 'evening' etc. Should have a map of the values.
		return 2;
	}

	// Returns the user location of the specified location.
	private static UserLocation GetUserLocation(Location physicalLocation) {
		for (UserLocation curLoc : locationList) {
			if (curLoc.IsLocationInHere(physicalLocation))
				return curLoc;
		}

		return null;
	}

	// Returns the user location for the specified name.
	private static UserLocation GetUserLocation(String locationName) {
		for (UserLocation curLoc : locationList) {
			if (curLoc.GetName().equalsIgnoreCase(locationName))
				return curLoc;
		}

		// Location is not in the list.
		return null;
	}

	// Returns the list of user names.
	public static ArrayList<String> GetListOfNames() {
		return nameList;
	}

	// Sets the location for the user location.
	private void SetLocation(Location newLocation) {
		location = newLocation;
	}

	// Returns if the specified location is in the location.
	private boolean IsLocationInHere(Location toTest) {
		return UserLocation.getCurrentLocation().distanceTo(toTest) <= SAME_LOCATION_DISTANCE;
//		return GetLocation().distanceTo(toTest) <= SAME_LOCATION_DISTANCE;
	}

	// Returns the location number.
	private int GetLocationNumber() {
		return locNumber;
	}

	// Returns the name of the location.
	private String GetName() {
		return name;
	}

	// Returns the location of the user location.
	private Location GetLocation() {
		return location;
	}

	private int locNumber;  // Number of the location
	private String name;    // Name of the location
	private Location location;  // Physical location
}
