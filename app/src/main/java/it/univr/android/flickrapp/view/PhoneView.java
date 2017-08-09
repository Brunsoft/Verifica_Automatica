package it.univr.android.flickrapp.view;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import it.univr.android.flickrapp.FlickrApplication;
import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.R;

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

        // at the beginning, show the SearchFragment
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

    @Override
    public void onModelChanged() {
        getFragment().onModelChanged();
    }

    @Override
    public void onImgLdDownloaded() { getFragment().onImgLdDownloaded(); }

    @Override
    public void onImgFhdDownloaded() { getFragment().onImgFhdDownloaded(); }

    @Override
    public void onImgFhdSaved() { getFragment().onImgFhdSaved(); }

    @Override
    public void showResults() {
        getFragmentManager().beginTransaction()
                .replace(R.id.phone_view, new SearchResultsFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showResultsAuthor() {
        getFragmentManager().beginTransaction()
                .replace(R.id.phone_view, new SearchResultsAuthorFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showPictureFhd() {
        getFragmentManager().beginTransaction()
                .replace(R.id.phone_view, new PictureFhdFragment())
                .addToBackStack(null)
                .commit();
    }

    /**
     * These two constructors must exist to let the view be recreated at
     * configuration change or inflated from XML.
     */

    public PhoneView(Context context) {
        super(context);
    }

    public PhoneView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}