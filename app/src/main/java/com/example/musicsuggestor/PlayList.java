package com.example.musicsuggestor;

import java.util.ArrayList;

// Handles a play list.
public class PlayList {
	// Constants
	public static int NO_SONG_NUMBER = 0;  // Number indicating song does not exist

	// Constructor for PlayList.
	public PlayList() {
		songList = new ArrayList<Song>();
	}

	// Adds the specific song to the list.  Returns if it succeeded.
	public boolean AddSong(Song toAdd) {
		// Don't add if the song is already in the play list.
		if (songList.contains(toAdd))
			return false;

		songList.add(toAdd);
		return true;
	}

	// Removes the specified song from the list.
	public void RemoveSong(Song toRemove) {
		songList.remove(toRemove);
	}

	// Returns the song identified by the user.
	public Song GetSong(String songName) {
		for (Song curSong : songList) {
			if (curSong.GetName().equalsIgnoreCase(songName))
				return curSong;
		}

		// No song matches.
		return null;
	}
	public Song GetSong(int songNumber) {
		for (Song curSong : songList) {
			if (curSong.GetSongNumber() == songNumber)
				return curSong;
		}

		// No song matches.
		return null;
	}

	// Returns the number of the specified song.
	public int GetSongNumber(String songName) {
		Song song = GetSong(songName);  // Song retrieved

		// Return the number (or default value if no location matches).
		if (song == null)
			return NO_SONG_NUMBER;
		return song.GetSongNumber();
	}

	// Returns the list of song names.
	public ArrayList<String> GetListOfNames() {
		ArrayList<String> nameList = new ArrayList<String>();   // List of names

		for (Song song : songList)
			nameList.add(song.GetName());
		return nameList;
	}

	public ArrayList<Song> getSongList() {
		return songList;
	}

	public int getSize() {
		return songList.size();
	}

	private ArrayList<Song> songList;   // List of songs
}
