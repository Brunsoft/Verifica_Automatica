package it.univr.android.flickrapp.view;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.univr.android.flickrapp.BuildConfig;
import it.univr.android.flickrapp.R;

public class InfoFragment extends Fragment implements AbstractFragment {
    private TextView app_name;
    private TextView app_version;
    private TextView app_copyright;
    private TextView app_date;
    private TextView app_authors;

    @Override @UiThread
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable @Override @UiThread
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        app_name = (TextView) view.findViewById(R.id.app_name);
        app_version = (TextView) view.findViewById(R.id.app_version);
        app_copyright = (TextView) view.findViewById(R.id.app_copyright);
        app_date = (TextView) view.findViewById(R.id.app_date);
        app_authors = (TextView) view.findViewById(R.id.app_authors);

        app_name.setText(BuildConfig.APPLICATION_ID);
        app_version.setText(BuildConfig.VERSION_NAME);
        app_copyright.setText("Copyright 2017");
        app_date.setText("04.08.2017 16:42");
        app_authors.setText("Luca Vicentini & Maddalena Zuccotto");
        return view;
    }

    @Override @UiThread
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onModelChanged();
    }

    @Override @UiThread
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_share, menu);
    }

    @Override @UiThread
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_info){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override @UiThread
    public void onModelChanged() {}

}