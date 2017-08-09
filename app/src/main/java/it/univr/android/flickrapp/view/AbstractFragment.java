package it.univr.android.flickrapp.view;

import android.support.annotation.UiThread;

public interface AbstractFragment {

    @UiThread
    void onModelChanged();

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