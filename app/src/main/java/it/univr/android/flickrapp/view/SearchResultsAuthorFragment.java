package it.univr.android.flickrapp.view;

/**
 * @author  Luca Vicentini, Maddalena Zuccotto
 * @version 1.0 */

import android.support.annotation.UiThread;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;

import it.univr.android.flickrapp.R;

/**
 * SearchResultsAuthorFragment estende SearchResultsFragment quindi ne eredita tutti i metodi
 * verranno riscritti solamente quelli che vogliamo abbiano un comportamento differente
 */
public class SearchResultsAuthorFragment extends SearchResultsFragment {

    @Override @UiThread
    public void onResultsChanged() {
        // switchedView -> true siamo in SearchResultsFragment
        mvc.controller.setSwitchedView(false);
        if (count_results == -1)
            count_results = 0;
        results_adapter = new SearchAdapter(getActivity());
        results_list.setAdapter(results_adapter);

        if (count_results == mvc.model.getResults(mvc.controller.getSwitchedView()).length)
            progr_load_results.setVisibility(View.GONE);
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

}
