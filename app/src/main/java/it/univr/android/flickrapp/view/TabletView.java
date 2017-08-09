package it.univr.android.flickrapp.view;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import it.univr.android.flickrapp.FlickrApplication;
import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.R;

public class TabletView extends LinearLayout implements View {
    private MVC mvc;

    private FragmentManager getFragmentManager() {
        return ((Activity) getContext()).getFragmentManager();
    }

    private AbstractFragment getSearchFragment() {
        return (AbstractFragment) getFragmentManager().findFragmentById(R.id.search_fragment);
    }

    private AbstractFragment getSearchResultsFragment() {
        return (AbstractFragment) getFragmentManager().findFragmentById(R.id.results_fragment);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mvc = ((FlickrApplication) getContext().getApplicationContext()).getMVC();
        mvc.register(this);

        // at the beginning, show the SearchResultFragment
        if (getSearchResultsFragment() == null)
            getFragmentManager().beginTransaction()
                    .add(R.id.results_fragment, new SearchResultsFragment())
                    .commit();
    }

    @Override
    protected void onDetachedFromWindow() {
        mvc.unregister(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onModelChanged() {
        getSearchFragment().onModelChanged();
        getSearchResultsFragment().onModelChanged();
    }

    @Override
    public void showResults() { }

    @Override
    public void showResultsAuthor(){
        getFragmentManager().beginTransaction()
                .replace(R.id.results_fragment, new SearchResultsAuthorFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showPictureFhd() {
        getFragmentManager().beginTransaction()
                .replace(R.id.results_fragment, new PictureFhdFragment())
                .addToBackStack(null)
                .commit();
    }

    /**
     * These two constructors must exist to let the view be recreated at
     * configuration change or inflated from XML.
     */

    public TabletView(Context context) {
        super(context);
    }

    public TabletView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}