package it.univr.android.flickrapp.view;

/**
 * @author  Luca Vicentini, Maddalena Zuccotto
 * @version 1.0 */

import android.app.Activity;
import android.os.Bundle;

import it.univr.android.flickrapp.R;
import it.univr.android.flickrapp.model.Model;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setto il model device in base al Layout della View (Tablet o Phone)
        Model.device = findViewById(R.id.view_layout).getTag().toString();
    }
}