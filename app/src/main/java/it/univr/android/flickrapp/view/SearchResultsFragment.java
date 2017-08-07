package it.univr.android.flickrapp.view;

import android.app.ListFragment;
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
import android.widget.TextView;

import it.univr.android.flickrapp.FlickrApplication;
import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.R;
import it.univr.android.flickrapp.model.Model;
import it.univr.android.flickrapp.model.Model.ImgInfo;

public class SearchResultsFragment extends ListFragment implements AbstractFragment {
    protected MVC mvc;

    @Override @UiThread
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override @UiThread
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mvc = ((FlickrApplication) getActivity().getApplication()).getMVC();
        onModelChanged();
        registerForContextMenu(getListView());
    }

    @Override @UiThread
    public void onModelChanged() {
        mvc.controller.setSwitchedView(true);
        setListAdapter(new SearchAdapter());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        menu.setHeaderTitle(R.string.context_menu_title);
        String[] menuItems = getResources().getStringArray(R.array.context_menu);
        for (int i = 0; i < menuItems.length; i++) {
            String menu_item = menuItems[i];
            if ( i==1 )     // Other pic of.. name
                menu_item += " " + mvc.model.getResult(info.position).getAuthor_name();
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
                Log.d("SRF", "" + mvc.controller.getSwitchedView());
                String author_id = mvc.model.getResult(info.position, true).getAuthor_id();
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
            mvc.model.setImageSel(position);
            mvc.controller.viewPictureSel(getActivity());
            mvc.controller.showPictureFhd();
        }
    }
}