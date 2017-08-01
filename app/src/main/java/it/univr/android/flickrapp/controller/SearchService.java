package it.univr.android.flickrapp.controller;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import it.univr.android.flickrapp.FlickrApplication;
import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.model.Model;

public class SearchService extends IntentService { // extends ExecutorIntentService
    private final static String TAG = SearchService.class.getName();
    private final static String ACTION_SEARCH_STR = "str";
    private final static String ACTION_SEARCH_LAST = "last";       // choice = true
    private final static String ACTION_SEARCH_TOP = "top";         // choice = false
    private final static String ACTION_SELECTION = "pic-sel";
    private final static String PARAM_S = "s";
    private final static String API_KEY = "388f5641e6dc1ecac49678a156f375df";

    public SearchService() {
        super("search_str service");
    }

    /*
    Used by the ExecutorIntentService only

    @Override
    protected ExecutorService mkExecutorService() {
        return Executors.newSingleThreadExecutor();
    }
    */

    @UiThread
    static void search(Context context, int choice, String s) {
        Log.d(TAG, "Choice: " + choice);
        Intent intent = new Intent(context, SearchService.class);
        switch (choice){
            case 0:
                intent.setAction(ACTION_SEARCH_STR);
                intent.putExtra(PARAM_S, s);
                break;
            case 1:
                intent.setAction(ACTION_SEARCH_LAST);
                break;
            case 2:
                intent.setAction(ACTION_SEARCH_TOP);
                break;
        }
        context.startService(intent);
    }

    @UiThread
    static void viewPictureSel(Context context) {

        Intent intent = new Intent(context, SearchService.class);
        intent.setAction(ACTION_SELECTION);
        context.startService(intent);
    }

    @WorkerThread
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
        String query = "";
        MVC mvc = ((FlickrApplication) getApplication()).getMVC();
        switch (intent.getAction()) {
            case ACTION_SEARCH_STR:
                query = String.format("https://api.flickr.com/services/rest?method=flickr.photos.search&api_key=%s&text=%s&extras=owner_name,url_sq,url_l,description,tags&per_page=20",
                        API_KEY,
                        (String) intent.getSerializableExtra(PARAM_S));
                break;
            case ACTION_SEARCH_LAST:
                query = String.format("https://api.flickr.com/services/rest?method=flickr.photos.getRecent&api_key=%s&extras=owner_name,url_sq,url_l,description,tags&per_page=20",
                        API_KEY);
                break;
            case ACTION_SEARCH_TOP:
                query = String.format("https://api.flickr.com/services/rest?method=flickr.interestingness.getList&api_key=%s&extras=owner_name,url_sq,url_l,description,tags&per_page=20",
                        API_KEY);
                break;
            case ACTION_SELECTION:
                Model.ImgInfo result = mvc.model.getResult(mvc.model.getImageSel());
                mvc.model.clearComments(mvc.model.getImageSel());
                mvc.model.storeComments(commentsSearch(result.getId()), getPic(result.getUrl_l()), mvc.model.getImageSel());
                break;
        }
        if (intent.getAction().equalsIgnoreCase(ACTION_SEARCH_STR) || intent.getAction().equalsIgnoreCase(ACTION_SEARCH_LAST) || intent.getAction().equalsIgnoreCase(ACTION_SEARCH_TOP))
        {
            mvc.model.clearResults();
            Iterable<Model.ImgInfo> results = pictureSearch(query);
            mvc.model.storeResults(results);
        }
    }

    @WorkerThread
    private Iterable<Model.ImgInfo> pictureSearch(String query) {
        Log.d(TAG, query);
        try {
            URL url = new URL(query);
            URLConnection conn = url.openConnection();
            String answer = "";

            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line;
                while ((line = in.readLine()) != null) {
                    answer += line + "\n";
                    Log.d(TAG, line);
                }
            }
            finally {
                if (in != null)
                    in.close();
            }

            return parsePic(answer);
        }
        catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private Iterable<Model.ImgInfo> parsePic(String xml) {
        List<Model.ImgInfo> infos = new LinkedList<>();

        int nextPhoto = -1;
        do {
            nextPhoto = xml.indexOf("<photo id", nextPhoto + 1);
            if (nextPhoto >= 0) {
                //Log.d(TAG, "nextPhoto = " + nextPhoto);
                int idPos = xml.indexOf("id=", nextPhoto) + 4;
                int titlePos = xml.indexOf("title=", nextPhoto) + 7;
                int authorPos = xml.indexOf("owner=", nextPhoto) + 7;
                int url_sqPos = xml.indexOf("url_sq=", nextPhoto) + 8;
                int url_lPos = xml.indexOf("url_l=", nextPhoto) + 7;
                String photoId = xml.substring(idPos, xml.indexOf("\"", idPos + 1));

                Log.d(TAG, "photoId = " + photoId);

                String title = xml.substring(titlePos, xml.indexOf("\"", titlePos + 1));
                String author = xml.substring(authorPos, xml.indexOf("\"", authorPos + 1));
                String url_sq = xml.substring(url_sqPos, xml.indexOf("\"", url_sqPos + 1));
                String url_l = xml.substring(url_lPos, xml.indexOf("\"", url_lPos + 1));
                infos.add(new Model.ImgInfo(photoId, title, author, url_sq, url_l, getPic(url_sq)));
            }
        }
        while (nextPhoto != -1);

        return infos;
    }

    private Iterable<Model.CommentImg> parseCom(String xml) {
        List<Model.CommentImg> comments = new LinkedList<>();

        int nextComment = -1;
        do {
            nextComment = xml.indexOf("<comment id", nextComment + 1);
            if (nextComment >= 0) {
                Log.d(TAG, "nextComment = " + nextComment);
                int authornamePos = xml.indexOf("authorname=", nextComment) + 12;
                int commentPos = xml.indexOf("\">", nextComment) + 2;
                String authorname = xml.substring(authornamePos, xml.indexOf("\"", authornamePos + 1));
                String comment = xml.substring(commentPos, xml.indexOf("<", commentPos + 1));
                comments.add(new Model.CommentImg(authorname,comment));
                Log.d(TAG, "authorname = " + authorname + "\n" + "comment = " + comment);
            }
        }
        while (nextComment != -1);

        return comments;
    }

    @WorkerThread
    private Iterable<Model.CommentImg> commentsSearch(String photoId) {
        String query = String.format("https://api.flickr.com/services/rest?method=flickr.photos.comments.getList&api_key=%s&photo_id=%s",
                API_KEY,
                photoId);
        Log.d(TAG, query);
        try {
            URL url = new URL(query);
            URLConnection conn = url.openConnection();
            String answer = "";

            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line;
                while ((line = in.readLine()) != null) {
                    answer += line + "\n";
                    Log.d(TAG, line);
                }
            }
            finally {
                if (in != null)
                    in.close();
            }

            return parseCom(answer);
        }
        catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private static Bitmap getPic(String url) {
        Bitmap bm = null;
        try {
            Log.d(TAG, url);
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return bm;
    }

}