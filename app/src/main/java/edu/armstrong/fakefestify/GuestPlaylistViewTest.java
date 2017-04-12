package edu.armstrong.fakefestify;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GuestPlaylistViewTest extends Activity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "beb8d9905bc044d883623aa2ce50890f";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "http://localhost:8888/callback";

    private Player mPlayer;
    TextView artistTv;
    TextView songTv;
    ImageView albumImageView;
    TextView votesTv;
    String trackUri = "";
    String votes = "";
    String sessionname;

    int currentTrackURI = 0;

    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guestplaylistviewtest);

        artistTv = (TextView) findViewById(R.id.artistTv);
        songTv = (TextView) findViewById(R.id.songTv);
        albumImageView = (ImageView) findViewById(R.id.albumCoverImg);
        votesTv = (TextView) findViewById(R.id.votesTv);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming", "playlist-modify-private", "playlist-modify-public", "user-library-modify"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    //get next trackuri and pull up new album cover and artist song....
    public void nextSongGuest(View v) {
        artistTv.setText("Artist: ");
        songTv.setText("Song: ");
        votesTv.setText("Votes: ");
        onLoggedIn();
    }

    public void upvoteEvent(View view) {
        Toast upvoteToast = Toast.makeText(getApplicationContext(), "Vote submitted!", Toast.LENGTH_SHORT);
        upvoteToast.show();
        String s = "http://musicboxse.herokuapp.com/voteIncrement.php?sessionname=" + sessionname + "&trackuri=" + trackUri;
        new UpvoteTask().execute(s);
    }

    public void downvoteEvent(View view) {
        Toast downVoteToast = Toast.makeText(getApplicationContext(), "Vote submitted!", Toast.LENGTH_SHORT);
        downVoteToast.show();
        String s = "http://musicboxse.herokuapp.com/voteDecrement.php?sessionname=" + sessionname + "&trackuri=" + trackUri;
        new DownvoteTask().execute(s);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);


        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(GuestPlaylistViewTest.this);
                        mPlayer.addNotificationCallback(GuestPlaylistViewTest.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //need to dispose of the Player when we are done with it.
        // That is typically done in the onDestroy callback of your Activity or Fragment, which should look like this:
        Spotify.destroyPlayer(this);
        super.onDestroy();

        //Because the Player uses native resources, and because multiple Fragments can each have a reference to the
        // singleton instance of the Player, it is very important that you release your reference when destroyed.
        //Important! If you do not call Spotify.destroyPlayer when you are done with the Player, then your app will leak resources.
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sessionname = extras.getString("sessionname"); //gets sessionname from intent
        }
        //get trackUri and vote count
        retrieveDataFromDB();
        // get artist and song name
        //getArtistAndSongName();
        // get album cover
        //getAlbumCover();

//When the player is logged in we ask the player to play the Spotify track with the URI spotify:track:2TpxZ7JUBn3uw46aR7qd6V.
        //  mPlayer.playUri(null, "spotify:track:3kb72STxc2959ZqsTwu52i", 0, 0); //cage the elephant
        // mPlayer.playUri(null, "spotify:track:1lNf67naeCkezRXFQS8vfR", 0, 0);
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(int i) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    // get data from table the host created
    public void retrieveDataFromDB() {
        //String s = "http://192.168.1.116/musicboxapp/retrieveDataFromTable.php";
        String s = "http://musicboxse.herokuapp.com/retrieveDataFromTable.php?sessionname=" + sessionname; //heroku serever
        new DataRetrievalFromTable().execute(s);
    }


    public void getArtistAndSongName() {
        String s = "https://api.spotify.com/v1/tracks/";
        s += trackUri;

        new NetworkGetArtistAndSongName().execute(s);
    }

    //Play the current trackuri taken from datbase
    public void playCurrentTrackUri() {
        mPlayer.playUri(null, "spotify:track:" + trackUri, 0, 0);
    }

    //get album cover of current track based on url in its JSON track file
    public void getAlbumCover(String url) {
        String albumUrl = url;
        playCurrentTrackUri();
        new ImageLoadTask(albumUrl, albumImageView).execute();
    }

    class NetworkGetArtistAndSongName extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                if (stream == null)
                    return null;
                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                return buffer.toString();
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            String s = result;
            try { // gets artist and song name
                JSONObject jsonObject = new JSONObject(s);
                JSONArray artistsArray = jsonObject.getJSONArray("artists");
                JSONObject artistsObject = artistsArray.getJSONObject(0);
                String artistName = artistsObject.getString("name");
                artistTv.append(artistName);

                String songName = jsonObject.getString("name");
                songTv.append(songName);

                JSONObject albumObject = jsonObject.getJSONObject("album");
                JSONArray imagesObject = albumObject.getJSONArray("images");
                JSONObject urlObject = imagesObject.getJSONObject(1);
                String albumCoverurlString = urlObject.getString("url");
                getAlbumCover(albumCoverurlString);

            } catch (Exception ex) {
                //error
            }
        }
    }

    //get album cover
    class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }

    }

    class DataRetrievalFromTable extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                if (stream == null)
                    return null;
                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                return buffer.toString();
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            String s = result;
            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONArray resultArray = jsonObject.getJSONArray("result");
                JSONObject zeroObject = resultArray.getJSONObject(currentTrackURI);
                currentTrackURI++;//INCREMEMNT 0 TO GO TO NEXT TRACKURI in TABLE **************************************
                trackUri = zeroObject.getString("trackuri");
               // trackUriTv.append(trackUri);

                votes = zeroObject.getString("votes");
                votesTv.append(votes);

                //update trackUri then get Artist and Song name from the tackUri
                getArtistAndSongName();

            } catch (Exception ex) {
                //error
            }
        }
    }

    // Thread to upvote current track
    private class UpvoteTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                String result = urls[0];
                URL url = new URL(result);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                readStream(in);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //set new vote value
            String currentVotes = votesTv.getText().toString().substring(7,votesTv.getText().toString().length());
            int newVote = Integer.valueOf(currentVotes);
            votesTv.setText("Votes: " + (newVote + 1));
        }

        private String readStream(InputStream is) {
            try {
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                int i = is.read();
                while (i != -1) {
                    bo.write(i);
                    i = is.read();
                }
                return bo.toString();
            } catch (IOException e) {
                return "";
            }
        }
    }

    // Thread to downvote current track
    private class DownvoteTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                String result = urls[0];
                URL url = new URL(result);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                readStream(in);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //set new vote value
            String currentVotes = votesTv.getText().toString().substring(7,votesTv.getText().toString().length());
            int newVote = Integer.valueOf(currentVotes);
            votesTv.setText("Votes: " + (newVote - 1));
        }

        private String readStream(InputStream is) {
            try {
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                int i = is.read();
                while (i != -1) {
                    bo.write(i);
                    i = is.read();
                }
                return bo.toString();
            } catch (IOException e) {
                return "";
            }
        }
    }
}