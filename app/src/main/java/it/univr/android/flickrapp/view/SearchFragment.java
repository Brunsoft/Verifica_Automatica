package it.univr.android.flickrapp.view;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
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

import it.univr.android.flickrapp.FlickrApplication;
import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.R;

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
        setHasOptionsMenu(true);
    }

    @Nullable @Override @UiThread
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

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
        onModelChanged();
    }

    @Override @UiThread
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_share, menu);
        menu.removeItem(R.id.menu_item_share);
    }

    @Override @UiThread
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_info){
            mvc.controller.showInfo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override @UiThread
    public void onModelChanged() {
        search_str.setEnabled(true);
    }

    @UiThread private void search(int choice) {
        String s = "";
        switch (choice) {
            case 0:
                search_str.setEnabled(false);
                try {
                    s = new String(insertString.getText().toString());
                    if (s.isEmpty())
                        throw new IllegalArgumentException();
                } catch (IllegalArgumentException e) {
                    message.setText("Inserimento non valido");
                    Log.e(TAG, "Inserimento non valido");
                }
                break;
            case 1:
                search_last.setEnabled(false);
                break;
            case 2:
                search_top.setEnabled(false);
                break;
        }
        mvc.controller.search(getActivity(), choice, s);
        mvc.controller.showResults();
    }

}