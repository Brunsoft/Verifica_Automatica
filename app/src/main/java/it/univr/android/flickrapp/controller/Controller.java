package it.univr.android.flickrapp.controller;

import android.content.Context;
import android.support.annotation.UiThread;

import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.view.View;

public class Controller {
    private final static String TAG = Controller.class.getName();
    private MVC mvc;
    private boolean switchedView = true;

    public void setMVC(MVC mvc) {
        this.mvc = mvc;
    }

    @UiThread
    public void search(Context context, int choice, String s) {
        SearchService.search(context, choice, s);
    }

    @UiThread
    public void sharePictureSel(Context context) {
        SearchService.sharePictureSel(context);
    }

    @UiThread
    public void viewPictureSel(Context context) {
        SearchService.viewPictureSel(context);
    }

    @UiThread
    public void showResults() {
        mvc.forEachView(View::showResults);
    }

    @UiThread
    public void showPictureFhd() {
        mvc.forEachView(View::showPictureFhd);
    }

    @UiThread
    public void showResultsAuthor() { mvc.forEachView(View::showResultsAuthor); }

    @UiThread
    public void showInfo() { mvc.forEachView(View::showInfo); }

    @UiThread
    public void setSwitchedView(boolean switchedView) {
        synchronized (this){
            this.switchedView = switchedView;
        }
    }

    @UiThread
    public boolean getSwitchedView() {
        synchronized (this) {
            return switchedView;
        }
    }

}