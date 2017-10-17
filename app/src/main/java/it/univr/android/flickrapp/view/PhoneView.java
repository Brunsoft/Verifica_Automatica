package it.univr.android.flickrapp.view;

/**
 * @author  Luca Vicentini, Maddalena Zuccotto
 * @version 1.0 */

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import it.univr.android.flickrapp.FlickrApplication;
import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.R;

/**
 * PhoneView è la classe che implementa la grafica per gli SmartPhone
 */
public class PhoneView extends FrameLayout implements View {
    private MVC mvc;

    private FragmentManager getFragmentManager() {
        return ((Activity) getContext()).getFragmentManager();
    }

    private AbstractFragment getFragment() {
        return (AbstractFragment) getFragmentManager().findFragmentById(R.id.phone_view);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mvc = ((FlickrApplication) getContext().getApplicationContext()).getMVC();
        mvc.register(this);

        // Inizialmente il getFragment sarà a null quindi verrà visualizzata la SearchFragment
        if (getFragment() == null)
            getFragmentManager().beginTransaction()
                    .add(R.id.phone_view, new SearchFragment())
                    .commit();
    }

    @Override
    protected void onDetachedFromWindow() {
        mvc.unregister(this);
        super.onDetachedFromWindow();
    }

    /**
     * Metodo chiamato dal Controller / Model quando la lista dei risultati cambia
     */
    @Override
    public void onResultsChanged() {
        getFragment().onResultsChanged();
    }

    /**
     * Metodo chiamato dal Controller quando la lista dei risultati è vuota, mostra "Nessun risultato trovato"
     */
    @Override
    public void onEmptyResult() { getFragment().onEmptyResult(); }

    /**
     * Metodo chiamato dal Controller quando la lista dei commenti è vuota, mostra "Nessun commento trovato"
     */
    @Override
    public void onEmptyComments() { getFragment().onEmptyComments(); }

    /**
     * Metodo chiamato dal Controller quando lo scaricamento dell'immagine Ld è completato
     */
    @Override
    public void onImgLdDownloaded() { getFragment().onImgLdDownloaded(); }

    /**
     * Metodo chiamato dal Controller quando lo scaricamento dell'immagine Fhd è completato
     */
    @Override
    public void onImgFhdDownloaded() { getFragment().onImgFhdDownloaded(); }

    /**
     * Metodo chiamato dal Controller quando il salvataggio dell'immagine Fhd è completato
     */
    @Override
    public void onImgFhdSaved() { getFragment().onImgFhdSaved(); }

    /**
     * Metodo utilizzato per visualizzare i risultati della ricerca in una nuova View
     */
    @Override
    public void showResults() {
        getFragmentManager().beginTransaction()
                .replace(R.id.phone_view, new SearchResultsFragment())
                .addToBackStack(null)
                .commit();
    }

    /**
     * Metodo utilizzato per visualizzare i risultati della ricerca, per autore, in una nuova View
     */
    @Override
    public void showResultsAuthor() {
        getFragmentManager().beginTransaction()
                .replace(R.id.phone_view, new SearchResultsAuthorFragment())
                .addToBackStack(null)
                .commit();
    }

    /**
     * Metodo utilizzato per visualizzare l'immagine selezionata, in Fhd, in una nuova View
     */
    @Override
    public void showPictureFhd() {
        getFragmentManager().beginTransaction()
                .replace(R.id.phone_view, new PictureFhdFragment())
                .addToBackStack(null)
                .commit();
    }

    public PhoneView(Context context) {
        super(context);
    }

    public PhoneView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}