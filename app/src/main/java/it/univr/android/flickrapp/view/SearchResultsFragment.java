package it.univr.android.flickrapp.view;

import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.*;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import it.univr.android.flickrapp.FlickrApplication;
import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.R;
import it.univr.android.flickrapp.model.Model;
import it.univr.android.flickrapp.model.Model.ImgInfo;

public class SearchResultsFragment extends ListFragment implements AbstractFragment {
    private MVC mvc;

    @Override @UiThread
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mvc = ((FlickrApplication) getActivity().getApplication()).getMVC();
        onModelChanged();
    }

    @Override @UiThread
    public void onModelChanged() {
        setListAdapter(new SearchAdapter());
    }

    private class SearchAdapter extends ArrayAdapter<ImgInfo> {
        private final Model.ImgInfo[] imgInfos = mvc.model.getResults();

        private SearchAdapter() {
            super(getActivity(), R.layout.fragment_result_item, mvc.model.getResults());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.fragment_result_item, parent, false);
            }

            Model.ImgInfo imgInfo = imgInfos[position];
            ((ImageView) row.findViewById(R.id.icon)).setImageBitmap(imgInfo.getThmb());
            ((TextView) row.findViewById(R.id.title)).setText(imgInfo.getTitle());
            return row;
        }

    }
}