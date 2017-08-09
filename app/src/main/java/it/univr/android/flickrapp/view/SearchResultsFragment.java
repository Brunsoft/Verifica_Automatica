package it.univr.android.flickrapp.view;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import it.univr.android.flickrapp.FlickrApplication;
import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.R;
import it.univr.android.flickrapp.model.Model;
import it.univr.android.flickrapp.model.Model.ImgInfo;

public class SearchResultsFragment extends Fragment implements AbstractFragment {
    protected MVC mvc;
    protected TextView empty_results;
    protected ListView results_list;

    @Override @UiThread
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable @Override @UiThread
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mvc = ((FlickrApplication) getActivity().getApplication()).getMVC();
        View view = inflater.inflate(R.layout.fragment_results, container, false);
        empty_results = (TextView)view.findViewById(R.id.empty_results);
        results_list = (ListView)view.findViewById(R.id.results_list);
        registerForContextMenu(results_list);
        return view;
    }

    @Override @UiThread
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onModelChanged();
    }

    @Override @UiThread
    public void onModelChanged() {
        mvc.controller.setSwitchedView(true);
        results_list.setAdapter(new SearchAdapter());
        if (mvc.model.getEnptyResult())
            empty_results.setText(R.string.empty_results);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(R.string.context_menu_title);
        String[] menuItems = getResources().getStringArray(R.array.context_menu);
        for (int i = 0; i < menuItems.length; i++) {
            String menu_item = menuItems[i];
            if ( i==1 )     // Other pic of.. name
                menu_item += " " + mvc.model.getResult(
                        ((AdapterView.AdapterContextMenuInfo) menuInfo).position).getAuthor_name();
            menu.add(Menu.NONE, i, i, menu_item);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch (item.getItemId()){
            case 0:
                Log.d("SRF", "Scelta SHARE");
                mvc.model.setImageSel(info.position);
                mvc.controller.sharePictureSel(getActivity());
                break;
            case 1:
                Log.d("SRF", "Scelta search");
                String author_id = mvc.model.getResult(info.position).getAuthor_id();
                mvc.controller.setSwitchedView(false);
                mvc.controller.search(getActivity(), 3, author_id);
                mvc.controller.showResultsAuthor();
                break;
        }
        return super.onContextItemSelected(item);
    }


    protected class SearchAdapter extends ArrayAdapter<ImgInfo> {

        private final Model.ImgInfo[] imgInfos = mvc.model.getResults();

        SearchAdapter() {
            super(getActivity(), R.layout.fragment_result_item, mvc.model.getResults());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.fragment_result_item, parent, false);
                row.setLongClickable(true);
            }

            Model.ImgInfo imgInfo = imgInfos[position];
            ((ImageView) row.findViewById(R.id.icon)).setImageBitmap(imgInfo.getThmb());
            ((TextView) row.findViewById(R.id.title)).setText(imgInfo.getTitle());
            ((TextView) row.findViewById(R.id.author_name)).setText(imgInfo.getAuthor_name());
            row.setOnClickListener(__->viewImageSel(position));
            return row;
        }

        private void viewImageSel(int position){
            mvc.model.setEmptyComment(position, false);
            mvc.model.setImageSel(position);
            mvc.controller.viewPictureSel(getActivity());
            mvc.controller.showPictureFhd();
        }
    }
}