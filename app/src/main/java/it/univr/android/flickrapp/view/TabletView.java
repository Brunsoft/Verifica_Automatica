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
    public void onResultsChanged() {
        getSearchFragment().onResultsChanged();
        getSearchResultsFragment().onResultsChanged();
    }

    @Override
    public void onImgLdDownloaded() {
        getSearchFragment().onImgLdDownloaded();
        getSearchResultsFragment().onImgLdDownloaded();
    }

    @Override
    public void onEmptyResult() {
        getSearchFragment().onEmptyResult();
        getSearchResultsFragment().onEmptyResult();
    }

    @Override
    public void onEmptyComments() {
        getSearchFragment().onEmptyComments();
        getSearchResultsFragment().onEmptyComments();
    }

    @Override
    public void onImgFhdDownloaded() {
        getSearchFragment().onImgFhdDownloaded();
        getSearchResultsFragment().onImgFhdDownloaded();
    }

    @Override
    public void onImgFhdSaved() {
        getSearchFragment().onImgFhdSaved();
        getSearchResultsFragment().onImgFhdSaved();
    }

    @Override
    public void showResults() {
        getFragmentManager().popBackStack("showResults", getFragmentManager().POP_BACK_STACK_INCLUSIVE);
        getFragmentManager().beginTransaction()
                .replace(R.id.results_fragment, new SearchResultsFragment())
                .addToBackStack("showResults")
                .commit();
    }

    @Override
    public void showResultsAuthor(){
        getFragmentManager().beginTransaction()
                .replace(R.id.results_fragment, new SearchResultsAuthorFragment())
                .addToBackStack("showResultsAuthor")
                .commit();
    }

    @Override
    public void showPictureFhd() {
        getFragmentManager().beginTransaction()
                .replace(R.id.results_fragment, new PictureFhdFragment())
                .addToBackStack("showPictureFhd")
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