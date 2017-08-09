package it.univr.android.flickrapp.view;

import android.app.Activity;
import android.os.Bundle;

import it.univr.android.flickrapp.R;
import it.univr.android.flickrapp.model.Model;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // stores if the device type is a tablet or a phone
        Model.device = findViewById(R.id.view_layout).getTag().toString();
    }
}