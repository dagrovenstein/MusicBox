package edu.armstrong.fakefestify;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Host extends AppCompatActivity {
    Button confirmBtn;
    Button startPartyBtn;
    EditText editText;
    String sessionname;
    TextView errorTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host);

        confirmBtn = (Button) findViewById(R.id.confirmBtn);
        startPartyBtn = (Button) findViewById(R.id.startPartyBtn);
        editText = (EditText) findViewById(R.id.editText);
        errorTV = (TextView) findViewById(R.id.errorTV);

        startPartyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sessionname == null) {
                    errorTV.setText("Error: Please enter a party name.");
                } else {
                    String s = "http://musicboxse.herokuapp.com/createTable.php?sessionname=" + sessionname;
                    new CreateSession().execute(s); // after session creates goes to new activity HostPlaylist
                    Intent hostPlaylist = new Intent(Host.this, HostManagement.class);
                    hostPlaylist.putExtra("sessionname", sessionname);
                    startActivity(hostPlaylist);
                }
            }
        });
    }

    // Takes focus away from editText
    public void onConfirmClick(View view) {
        //sessionname = editText.getText().toString();
        confirmBtn.requestFocusFromTouch(); //dummy focus pretty much to take focus away from editText
        hideTheKeyboard();
        sessionname = editText.getText().toString();
        sessionname = sessionname.replaceAll(" ", "_");
    }

    // Hide the keyboard - DANIEL
    private void hideTheKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

private class CreateSession extends AsyncTask<String, Void, String> {

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
}
