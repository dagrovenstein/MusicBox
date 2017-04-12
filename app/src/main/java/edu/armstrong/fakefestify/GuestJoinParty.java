package edu.armstrong.fakefestify;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Daniel on 11/21/2016.
 */

public class GuestJoinParty extends AppCompatActivity {

    EditText partyNameET;
    String sessionname; // need later when put partyname in php script.

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guestjoinparty);

        partyNameET = (EditText) findViewById(R.id.editText2);
    }

    public void joinPartyClick(View v) {
        sessionname = partyNameET.getText().toString();
        sessionname = sessionname.replaceAll(" ", "_");
        Intent guest = new Intent(this, GuestManagement.class);
        guest.putExtra("sessionname",sessionname);
        startActivity(guest);
    }
}
