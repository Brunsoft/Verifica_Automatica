package it.univr.android.flickrapp.view;

/**
 * @author  Luca Vicentini, Maddalena Zuccotto
 * @version 1.0 */

import android.support.annotation.UiThread;

/*
 * View interfaccia usata per l'implementazione delle Activity
 */
public interface View {

    @UiThread
    void showResults();

    @UiThread
    void showResultsAuthor();

    @UiThread
    void showPictureFhd();

    @UiThread
    void onResultsChanged();

    @UiThread
    void onEmptyResult();

    @UiThread
    void onEmptyComments();

    @UiThread
    void onImgLdDownloaded();

    @UiThread
    void onImgFhdDownloaded();

    @UiThread
    void onImgFhdSaved();

}