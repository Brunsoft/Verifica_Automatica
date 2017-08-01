package it.univr.android.flickrapp.view;

import android.support.annotation.UiThread;

public interface View {


    @UiThread
    void showResults();

    @UiThread
    void showPictureFhd();

    @UiThread
    void onModelChanged();
}