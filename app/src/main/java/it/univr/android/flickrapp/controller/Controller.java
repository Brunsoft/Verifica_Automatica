package it.univr.android.flickrapp.controller;

/**
 * @author  Luca Vicentini, Maddalena Zuccotto
 * @version 1.0 */

import android.content.Context;
import android.support.annotation.UiThread;

import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.view.View;

public class Controller {
    private MVC mvc;
    private boolean switchedView = true;

    /**
     * Metodo per settare l'mvc
     * @param   mvc oggetto mvc da settare
     */
    public void setMVC(MVC mvc) {
        this.mvc = mvc;
    }

    /**
     * Metodo per la ricerca dei risultati (immagini in bassa risoluzione e relativi dati)
     * @param   context contesto a cui si fa riferimento
     * @param   choice scelta del tipo di ricerca
     * @param   s stringa da cercare
     */
    @UiThread
    public void search(Context context, int choice, String s) {
        SearchService.search(context, choice, s);
    }

    /**
     * Metodo per la condivisione dell'immagine selezionata
     * @param   context contesto a cui si fa riferimento
     */
    @UiThread
    public void sharePictureSel(Context context) {
        SearchService.sharePictureSel(context);
    }

    /**
     * Metodo per la condivisione dell'immagine selezionata
     * @param   context contesto a cui si fa riferimento
     */
    @UiThread
    public void shareOwnPictureSel(Context context) {
        SearchService.shareOwnPictureSel(context);
    }

    /**
     * Metodo per la visualizzazione dell'immagine selezionata in Fhd
     * @param   context contesto a cui si fa riferimento
     */
    @UiThread
    public void viewPictureSel(Context context) {
        SearchService.viewPictureSel(context);
    }

    /**
     * Metodo per la visualizzazione dell'immagine selezionata in Fhd
     * @param   context contesto a cui si fa riferimento
     */
    @UiThread
    public void viewOwnPictureSel(Context context) {
        SearchService.viewOwnPictureSel(context);
    }

    /**
     * Metodo che notifica tutte le View dell'mvc per mostrare la SearchResultFragment
     */
    @UiThread
    public void showResults() {
        mvc.forEachView(View::showResults);
    }

    /**
     * Metodo che notifica tutte le View dell'mvc per mostrare la PictureFhdFragment
     */
    @UiThread
    public void showPictureFhd() {
        mvc.forEachView(View::showPictureFhd);
    }

    /**
     * Metodo che notifica tutte le View dell'mvc per mostrare la SearchResultAuthorFragment
     */
    @UiThread
    public void showResultsAuthor() { mvc.forEachView(View::showResultsAuthor); }

    /**
     * Metodo per la gestione delle View SearchResultFragment & SearchResultAuthorFragment
     * @param   switchedView se true punta a SearchResultFragment, false punta a SearchResultAuthorFragment
     */
    @UiThread
    public void setSwitchedView(boolean switchedView) {
        this.switchedView = switchedView;
    }

    /**
     * Metodo per ricavare il valore relativo a switchedView
     * @return  switchedView valore boolean che rappresenta la View corrente tra SearchResultFragment e SearchResultAuthorFragment
     */
    @UiThread
    public boolean getSwitchedView() {
        return switchedView;
    }

}