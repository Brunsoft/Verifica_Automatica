package it.univr.android.flickrapp;

import android.app.Application;

import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.controller.Controller;
import it.univr.android.flickrapp.model.Model;

public class FlickrApplication extends Application {
    private MVC mvc;

    @Override
    public void onCreate() {
        super.onCreate();

        mvc = new MVC(new Model(), new Controller());
    }

    public MVC getMVC() {
        return mvc;
    }
}
