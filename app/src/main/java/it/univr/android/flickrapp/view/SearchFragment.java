package it.univr.android.flickrapp.view;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;
import android.view.LayoutInflater;
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
    public void onModelChanged() {
        search_str.setEnabled(true);
        search_last.setEnabled(true);
        search_top.setEnabled(true);
    }

    @UiThread private void search(int choice) {
        String s = "";
        switch (choice) {
            case 0:
                try {
                    s = new String(insertString.getText().toString());
                    if (s.isEmpty())
                        throw new IllegalArgumentException();
                    else
                        search_str.setEnabled(false);
                } catch (IllegalArgumentException e) {
                    message.setText(R.string.error_empty_field);
                    Log.e(TAG, "Inserimento non valido");
                    return;
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