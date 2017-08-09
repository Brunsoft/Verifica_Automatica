package it.univr.android.flickrapp.view;

import android.support.annotation.UiThread;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import it.univr.android.flickrapp.R;

public class SearchResultsAuthorFragment extends SearchResultsFragment {

    @Override @UiThread
    public void onResultsChanged() {
        results_adapter = new SearchAdapter(getActivity());
        results_list.setAdapter(results_adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(R.string.context_menu_title);
        String[] menuItems = getResources().getStringArray(R.array.context_menu);
        for (int i = 0; i < menuItems.length; i++)
            if (i == 0)
                menu.add(Menu.NONE, i, i, menuItems[i]);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch (item.getItemId()){
            case 0:
                Log.d("SRAF", "Scelta SHARE");
                mvc.model.setImageSel(info.position);
                mvc.controller.sharePictureSel(getActivity());
                break;
        }
        return super.onContextItemSelected(item);
    }
}
