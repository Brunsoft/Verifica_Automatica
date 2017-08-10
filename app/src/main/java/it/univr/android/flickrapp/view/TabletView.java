package it.univr.android.flickrapp.view;

/**
 * @author  Luca Vicentini, Maddalena Zuccotto
 * @version 1.0 */

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import it.univr.android.flickrapp.FlickrApplication;
import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.R;

/*
 * TabletView è la classe che implementa la grafica per i Tablet
 */
public class TabletView extends LinearLayout implements View {
    private MVC mvc;

    private FragmentManager getFragmentManager() {
        return ((Activity) getContext()).getFragmentManager();
    }

    private AbstractFragment getSearchFragment() {
        return (AbstractFragment) getFragmentManager().findFragmentById(R.id.search_fragment);
    }

    private AbstractFragment getSearchResultsFragment() {
        return (AbstractFragment) getFragmentManager().findFragmentById(R.id.results_fragment);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mvc = ((FlickrApplication) getContext().getApplicationContext()).getMVC();
        mvc.register(this);

        // Inizialmente il getSearchResultsFragment sarà a null quindi verrà visualizzata la SearchResultsFragment
        if (getSearchResultsFragment() == null)
            getFragmentManager().beginTransaction()
                    .add(R.id.results_fragment, new SearchResultsFragment())
                    .commit();
    }

    @Override
    protected void onDetachedFromWindow() {
        mvc.unregister(this);
        super.onDetachedFromWindow();
    }

    /*
     * Metodo chiamato dal Controller / Model quando la lista dei risultati cambia
     */
    @Override
    public void onResultsChanged() {
        getSearchFragment().onResultsChanged();
        getSearchResultsFragment().onResultsChanged();
    }

    /*
     * Metodo chiamato dal Controller quando lo scaricamento dell'immagine Ld è completato
     */
    @Override
    public void onImgLdDownloaded() {
        getSearchFragment().onImgLdDownloaded();
        getSearchResultsFragment().onImgLdDownloaded();
    }

    /*
     * Metodo chiamato dal Controller quando la lista dei risultati è vuota, mostra "Nessun risultato trovato"
     */
    @Override
    public void onEmptyResult() {
        getSearchFragment().onEmptyResult();
        getSearchResultsFragment().onEmptyResult();
    }

    /*
     * Metodo chiamato dal Controller quando la lista dei commenti è vuota, mostra "Nessun commento trovato"
     */
    @Override
    public void onEmptyComments() {
        getSearchFragment().onEmptyComments();
        getSearchResultsFragment().onEmptyComments();
    }

    /*
     * Metodo chiamato dal Controller quando lo scaricamento dell'immagine Fhd è completato
     */
    @Override
    public void onImgFhdDownloaded() {
        getSearchFragment().onImgFhdDownloaded();
        getSearchResultsFragment().onImgFhdDownloaded();
    }

    /*
     * Metodo chiamato dal Controller quando il salvataggio dell'immagine Fhd è completato
     */
    @Override
    public void onImgFhdSaved() {
        getSearchFragment().onImgFhdSaved();
        getSearchResultsFragment().onImgFhdSaved();
    }

    /*
     * Metodo utilizzato per visualizzare i risultati della ricerca in una nuova View
     */
    @Override
    public void showResults() {
        /* Per evitare errori di visualizzazione, ad una nuova invocazione di showResults(), torniamo alla
         * visualizzazione di risulati relativi alla ricerca precedente (fragment identificata da "showResults")
         */
        getFragmentManager().popBackStack("showResults", getFragmentManager().POP_BACK_STACK_INCLUSIVE);
        getFragmentManager().beginTransaction()
                .replace(R.id.results_fragment, new SearchResultsFragment())
                .addToBackStack("showResults")
                .commit();
    }

    /*
     * Metodo utilizzato per visualizzare i risultati della ricerca, per autore, in una nuova View
     */
    @Override
    public void showResultsAuthor(){
        getFragmentManager().beginTransaction()
                .replace(R.id.results_fragment, new SearchResultsAuthorFragment())
                .addToBackStack("showResultsAuthor")
                .commit();
    }

    /*
     * Metodo utilizzato per visualizzare l'immagine selezionata, in Fhd, in una nuova View
     */
    @Override
    public void showPictureFhd() {
        getFragmentManager().beginTransaction()
                .replace(R.id.results_fragment, new PictureFhdFragment())
                .addToBackStack("showPictureFhd")
                .commit();
    }

    public TabletView(Context context) {
        super(context);
    }

    public TabletView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}