# cse535-2019F-A2
<h1>CSE 535 - Fall 2019 - Project - Team 3</h1>
<b>Music Suggestor App</b>

A music player Android application that can suggest users with appropriate songs based users current location (e.g. school, home, restaurant etc.), users mood.
There are two parts of the project, and Android Application 'MusicSuggestor' and a Server that provides with predicted song category based on user's current information.

<h2>Details</h2>
The server mainly provides with api that receives 'application/json' data and responds with json data as well.

<h3>How to start the server</h3>
<ol>
	<li>Install docker for your OS: <br/>
		https://www.docker.com/
	</li>
	<li>For Linux users, add your user to the docker group: <br/>
		https://docs.docker.com/install/linux/linux-postinstall/
	</li>
	<li>Clone this repository in your machine: <br/>
		<pre>git clone https://github.com/MarkSFisher/MusicSuggestor.git</pre>
	</li>
	<li>Go to the downoad directory: <br/>
		<pre>cd MusicSuggestor/server</pre>
	</li>
	<li>Build the docker image (notice the dot, '.', at the end): <br/>
		<pre>docker build -t myserver .</pre>
	</li>
	<li>Run the docker image just created: <br/>
		<pre>docker run -ti --rm -p 5000:5000 myserver python3 app.py</pre>
		<li>Use the below line for the windows version to Run the docker image just created: <br/>
		<pre>winpty docker run -ti --rm -p 5000:5000 myserver python3 app.py</pre>
	</li>
	<li>Check if the server is running on browser: <br/>
		http://{your-server-url}:5000/ <br/>
		example: http://127.0.0.1:5000/
	</li>
</ol>

<h3>Running The Android App</h3>
<ol>
	<li>
		Open the Music Suggestor 'app' in Android Studio and build APK file.
	</li>
	<li>
		Install the APK in an Android device.
	</li>
	<li>
		Open the app when installed. For some versions, the application might display an exception and close. Re-start the app and <b>allow all the permissions requested.</b>
	</li>
	<li>
		Click the <b>"DOWNLOAD SAMPLE SONGS"</b> button when you are running the app for the first time.
	</li>
	<li>
		Click <b>"GO TO PLAY LIST"</b> button to go to the playlist when the songs are downloaded.
	</li>
	<li>
		Click on the play button or <b>"PLAY RANDOM SONG"</b> button to play a song.
	</li>
	<li>
		Song and/or song speed and/or song volume should be automatically changed based on user's location change.<br/>(For the class-project, we statically saved only a few locations of each category in the app. The locations are within 500 meter radius around this address: Brickyard on Mill, 699 S Mill Ave, Tempe, AZ 85281.)
	</li>
	<li>
		You can also set user mood in the Home screen. Click button <b>"SET CURRENT MODE"</b>, it will take you to another screen where you can select a mood from a dropdown list. (A future work direction would be to automatically detect usre's mode from activities/surroundings)
	</li>
</ol>
<h4>Required Android Version</h4>
We have tested it under <b>Android OS Version 7</b> or above





