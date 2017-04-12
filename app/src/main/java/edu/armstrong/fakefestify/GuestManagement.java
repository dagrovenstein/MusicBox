package edu.armstrong.fakefestify;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Created by Daniel on 11/21/2016.
 */

public class GuestManagement extends GuestJoinParty {
    EditText artistET;
    EditText songET;
    TextView sessionNameTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guestmanagement);
        artistET = (EditText) findViewById(R.id.artistET);
        songET = (EditText) findViewById(R.id.songET);
        sessionNameTv = (TextView) findViewById(R.id.sessionNameTv);
        sessionNameTv.setText(sessionname);

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            sessionname = extras.getString("sessionname");
        }
        sessionname.replaceAll(" ", "_");
        sessionNameTv.setText(sessionname);
    }
    // add to playlist button click
    public void findTrackURIForArtistAndSongGuest(View v) {
        Toast addToast = Toast.makeText(getApplicationContext(), "Added to playlist!", Toast.LENGTH_SHORT);
        addToast.show();
        String artist = artistET.getText().toString().replaceAll(" ", "%20");
        String song = songET.getText().toString().replaceAll(" ", "%20");
        String s = "https://api.spotify.com/v1/search?q=track:" + song + "%20artist:" + artist + "&type=track";
        new NetworkGetTrackURIforArtistAndSong().execute(s);
    }

    //adds new trackuri to the party's playlist(table in database)
    public void addNewSongToPlaylist(String trackUri) {
        String s = "http://musicboxse.herokuapp.com/insert.php?sessionname=" + sessionname + "&trackuri="+trackUri+ "&votes=0";
        new DatabaseInsert().execute(s);
    }

    public void viewCurrentPlaylist(View v) {
        Intent guest = new Intent(this, GuestPlaylistViewTest.class);
        guest.putExtra("sessionname",sessionname);
        startActivity(guest);
    }

    private class DatabaseInsert extends AsyncTask<String, Void, String> {

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

    class NetworkGetTrackURIforArtistAndSong extends AsyncTask<String, Void, String> {

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
                JSONObject tracksObject = jsonObject.getJSONObject("tracks");
                JSONArray itemsObject = tracksObject.getJSONArray("items");
                JSONObject idObject = itemsObject.getJSONObject(0);
                String trackuri = idObject.getString("id"); // trackuri
                addNewSongToPlaylist(trackuri);
            } catch (Exception ex) {
                //error
            }
        }
    }
}