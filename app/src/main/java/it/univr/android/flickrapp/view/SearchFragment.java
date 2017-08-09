package it.univr.android.flickrapp.view;

import android.Manifest;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import it.univr.android.flickrapp.FlickrApplication;
import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.R;
import it.univr.android.flickrapp.model.Model;

public class SearchFragment extends Fragment implements AbstractFragment {
    private final static String TAG = SearchFragment.class.getName();
    private MVC mvc;
    private EditText insertString;
    private Button search_str;
    private Button search_last;
    private Button search_top;
    private TextView message;

    @Override @UiThread
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Model.device.equals("phone_view"))
            setHasOptionsMenu(true);
        else
            setHasOptionsMenu(false);
    }

    @Nullable @Override @UiThread
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        checkDataPermission();
        checkNetworkAvailable();

        insertString = (EditText) view.findViewById(R.id.insert_string);
        search_str = (Button) view.findViewById(R.id.search_string);
        search_last = (Button) view.findViewById(R.id.search_last);
        search_top = (Button) view.findViewById(R.id.search_top);
        message = (TextView) view.findViewById(R.id.title);
        search_str.setOnClickListener(__ -> search(0));
        search_last.setOnClickListener(__ -> search(1));
        search_top.setOnClickListener(__ -> search(2));

        return view;
    }

    @Override @UiThread
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mvc = ((FlickrApplication) getActivity().getApplication()).getMVC();
        onResultsChanged();
    }

    @Override @UiThread
    public void onResultsChanged() { }

    @Override @UiThread
    public void onEmptyResult() { }

    @Override @UiThread
    public void onEmptyComments() { }

    @Override @UiThread
    public void onImgLdDownloaded() { }

    @Override @UiThread
    public void onImgFhdDownloaded() { }

    @Override @UiThread
    public void onImgFhdSaved() { }

    @UiThread private void search(int choice) {
        String s = "";
        switch (choice) {
            case 0:
                try {
                    s = new String(insertString.getText().toString());
                    if (s.isEmpty())
                        throw new IllegalArgumentException();
                } catch (IllegalArgumentException e) {
                    message.setText(R.string.error_empty_field);
                    Log.e(TAG, "Inserimento non valido");
                    return;
                }
                break;
        }

        mvc.controller.search(getActivity(), choice, s);
        mvc.controller.showResults();
    }

    @Override @UiThread
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_share, menu);
        menu.removeItem(R.id.menu_item_share);
    }

    @Override @UiThread
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_info:
                Dialog d = new Dialog(getActivity());
                d.setTitle(getResources().getText(R.string.info_button));
                d.setContentView(R.layout.dialog_info);
                d.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (!(netInfo != null && netInfo.isConnected()))
            Toast.makeText(getActivity(), getResources().getText(R.string.network_warning), Toast.LENGTH_SHORT).show();
    }

    private void checkDataPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {}
                else
                    Toast.makeText(getActivity(), getResources().getText(R.string.storage_permission), Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }
}