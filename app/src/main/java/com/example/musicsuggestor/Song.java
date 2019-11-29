package com.example.musicsuggestor;

// Stores a single song.
public class Song {
	// Class-level variables
	private static int nextSongNumber = PlayList.NO_SONG_NUMBER + 1;    // Number to assign the next song

	// Constructor for Song.
	public Song() {
		number = nextSongNumber;
	}
	public Song(String newName, int newNumber) {
		this();
		name = newName;
		number = newNumber;
	}

	// Returns the name of the song.
	public String GetName() {
		return name;
	}

	// Returns the number of the song.
	public int GetSongNumber() {
		return number;
	}

	private String name;    // Name of the song
	private int number;     // Number of the song
}
