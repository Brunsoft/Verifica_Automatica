package it.univr.android.flickrapp;

/**
 * @author  Luca Vicentini, Maddalena Zuccotto
 * @version 1.0 */

import android.os.Looper;
import android.os.Handler;

import net.jcip.annotations.ThreadSafe;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import it.univr.android.flickrapp.controller.Controller;
import it.univr.android.flickrapp.model.Model;
import it.univr.android.flickrapp.view.View;

/**
 * MVC è la classe che gestisce il paradigma MVC (Model - View - Controller)
 */
@ThreadSafe
public class MVC {
    public final Model model;
    public final Controller controller;
    private final List<View> views = new CopyOnWriteArrayList<>();

    public MVC(Model model, Controller controller) {
        this.model = model;
        this.controller = controller;

        model.setMVC(this);
        controller.setMVC(this);
    }

    public void register(View view) {
        views.add(view);
    }

    public void unregister(View view) {
        views.remove(view);
    }

    public interface ViewTask {
        void process(View view);
    }

    public void forEachView(ViewTask task) {

        new Handler(Looper.getMainLooper()).post(() -> {
            for (View view: views)
                task.process(view);
        });
    }
}