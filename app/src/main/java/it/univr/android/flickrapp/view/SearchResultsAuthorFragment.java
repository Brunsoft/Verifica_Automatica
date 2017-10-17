package it.univr.android.flickrapp.view;

/**
 * @author  Luca Vicentini, Maddalena Zuccotto
 * @version 1.0 */

import android.support.annotation.UiThread;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import it.univr.android.flickrapp.R;

/**
 * SearchResultsAuthorFragment estende SearchResultsFragment quindi ne eredita tutti i metodi
 * verranno riscritti solamente quelli che vogliamo abbiano un comportamento differente
 */
public class SearchResultsAuthorFragment extends SearchResultsFragment {

    @Override @UiThread
    public void onResultsChanged() {
        results_adapter = new SearchAdapter(getActivity());
        results_list.setAdapter(results_adapter);
    }

    /**
     * Differisce dal metodo ereditato solamente perché non viene più concessa la possibilità di cercare per autore.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(R.string.context_menu_title);
        String[] menuItems = getResources().getStringArray(R.array.context_menu);
        for (int i = 0; i < menuItems.length; i++)
            if (i == 0)
                menu.add(Menu.NONE, i, i, menuItems[i]);
    }

    /**
     * Differisce dal metodo ereditato solamente perché viene concessa la sola possibilità di condividere l'immagine
     */
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
