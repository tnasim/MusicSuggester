package com.example.musicsuggestor;

import android.location.Location;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

// Handles locations (both user-specified and physical).
public class UserLocation {
	// Constants - note that distances are in meters.
	public static final int NO_USER_LOCATION = 0;   // Indicates no user location assigned to this place
	public static final float SAME_LOCATION_DISTANCE = 40F; // Distance two locations can be within each other and be the same location
	public static final float LOCATION_UPDATE_RANGE = 10F;

	public static Location currentLocation;
	// Class-level variables
	private static ArrayList<UserLocation> locationList = new ArrayList<UserLocation>();    // List of locations
	private static ArrayList<String> nameList = new ArrayList<String>();    // List of strings

	public static Location LOC_BYENG = new Location("dummyprovider");
	public static Location LOC_HOME = new Location("dummyprovider");
	public static Location LOC_GROCERY = new Location("dummyprovider");
	public static Location LOC_GYM = new Location("dummyprovider");
	public static Location LOC_LIBRARY = new Location("dummyprovider");
	public static Location LOC_STREET = new Location("dummyprovider");
	public static Location LOC_RESTAURANT = new Location("dummyprovider");
	public static Location LOC_PARK = new Location("dummyprovider");

	public static Location DEFAULT_LOCATION;

	static {
		LOC_BYENG.setLatitude(33.4237075);
		LOC_BYENG.setLongitude(-111.9396098);
		locationList.add(new UserLocation(2, "school", LOC_BYENG));

		LOC_HOME.setLatitude(33.4242870);
		LOC_HOME.setLongitude(-111.9398719);
		locationList.add(new UserLocation(1, "home", LOC_HOME));

		LOC_GROCERY.setLatitude(33.4232928);
		LOC_GROCERY.setLongitude(-111.9398639);
		locationList.add(new UserLocation(3, "grocery", LOC_GROCERY));

		LOC_GYM.setLatitude(33.4237075);
		LOC_GYM.setLongitude(-111.9396098);
		locationList.add(new UserLocation(4, "gym", LOC_GYM));

		LOC_LIBRARY.setLatitude(33.4232010);
		LOC_LIBRARY.setLongitude(-111.9404778);
		locationList.add(new UserLocation(5, "library", LOC_LIBRARY));

		LOC_STREET.setLatitude(33.4232310);
		LOC_STREET.setLongitude(-111.9409663);
		locationList.add(new UserLocation(6, "street", LOC_STREET));

		LOC_RESTAURANT.setLatitude(33.4239187);
		LOC_RESTAURANT.setLongitude(-111.9398870);
		locationList.add(new UserLocation(7, "restaurant", LOC_RESTAURANT));

		LOC_PARK.setLatitude(33.4237626);
		LOC_PARK.setLongitude(-111.9382056);
		locationList.add(new UserLocation(8, "park", LOC_PARK));

		DEFAULT_LOCATION = LOC_BYENG;
	}

	// Constructor for location.  This is private because users should use location numbers instead.
	private UserLocation(int number, String name, Location newLocation) {
		SetLocation(newLocation);
		locNumber = number;
		this.name = name;

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
			curLoc = new UserLocation(2, name, newLocation);
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

	// Returns the user location of the specified location.
	public static UserLocation GetUserLocation(Location physicalLocation) {
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
	public int GetLocationNumber() {
		return locNumber;
	}

	// Returns the name of the location.
	public String GetName() {
		return name;
	}

	// Returns the location of the user location.
	private Location GetLocation() {
		return location;
	}

	public static int getCurrentTimeNumber() {
		Calendar cal = Calendar.getInstance();
		Date time = cal.getTime();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		if(hour <= 5) {
			return 6; // midnight
		} else if( hour <= 11) {
			return 1; // morning
		} else if (hour <= 13) {
			return 2; // noon
		} else if(hour <= 16) {
			return 3; // afternoon
		} else if(hour <= 20) {
			return 4; // evening
		} else if(hour <= 24) {
			return 5; // night
		}

		return 4;
	}

	private int locNumber;  // Number of the location
	private String name;    // Name of the location
	private Location location;  // Physical location
}
