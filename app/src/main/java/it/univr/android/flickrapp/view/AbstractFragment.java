package it.univr.android.flickrapp.view;

/**
 * @author  Luca Vicentini, Maddalena Zuccotto
 * @version 1.0 */

import android.support.annotation.UiThread;

/*
 * AbstractFragment interfaccia usata per l'implementazione delle fragment
 */
public interface AbstractFragment {

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