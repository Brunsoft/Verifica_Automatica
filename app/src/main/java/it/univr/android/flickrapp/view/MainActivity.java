package it.univr.android.flickrapp.view;

/**
 * @author  Luca Vicentini, Maddalena Zuccotto
 * @version 1.0 */

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

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

    /**
     * Metodo che controlla l'effettiva risposta dell'utente riguardo i permessi richiesti
     * requestCode: 1 -> WRITE_EXTERNAL_STORAGE
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, getResources().getText(R.string.storage_permission_granted), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, getResources().getText(R.string.storage_permission_denied), Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }
}