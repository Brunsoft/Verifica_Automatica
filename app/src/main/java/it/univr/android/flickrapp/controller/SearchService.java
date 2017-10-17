package it.univr.android.flickrapp.controller;

/**
 * @author  Luca Vicentini, Maddalena Zuccotto
 * @version 1.0 */

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import it.univr.android.flickrapp.FlickrApplication;
import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.model.Model;
import it.univr.android.flickrapp.view.View;

/**
 * SearchService è una classe per gestire lo scaricamento delle informazioni relative alle foto e i commenti
 * Lancia ImageService quando necessario
 */
public class SearchService extends IntentService {
    private final static String TAG = SearchService.class.getName();
    private final static String ACTION_SEARCH_STR = "str";         // choice = 0
    private final static String ACTION_SEARCH_LAST = "last";       // choice = 1
    private final static String ACTION_SEARCH_TOP = "top";         // choice = 2
    private final static String ACTION_SEARCH_OWN = "own";         // choice = 3
    private final static String ACTION_SELECTION = "pic-sel";
    private final static String ACTION_SHARE = "pic-share";
    private final static String PARAM_S = "s";
    private final static String API_KEY = "388f5641e6dc1ecac49678a156f375df";

    /**
     * Costruttore della classe corrente
     */
    public SearchService() {
        super("search_str service");
    }

    /**
     * Metodo che crea un intent per lanciare un servizio che andrà a scaricare una lista di immagini inerenti alla ricerca efettuata
     * @param   context contesto a cui si fa riferimento
     * @param   choice scelta del tipo di ricerca
     * @param   s Stringa di ricerca
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
            case 3:
                intent.setAction(ACTION_SEARCH_OWN);
                intent.putExtra(PARAM_S, s);
                break;
        }
        context.startService(intent);
    }

    /**
     * Metodo che crea un intent per lanciare un servizio che andrà a scaricare l'immagine selezionata in Fhd e i relativi commenti
     * @param   context contesto a cui si fa riferimento
     */
    @UiThread
    static void viewPictureSel(Context context) {
        Intent intent = new Intent(context, SearchService.class);
        intent.setAction(ACTION_SELECTION);
        context.startService(intent);
    }

    /**
     * Metodo che crea un intent per lanciare un servizio che andrà a salvare in memoria l'immagine da condividere
     * @param   context contesto a cui si fa riferimento
     */
    @UiThread
    static void sharePictureSel(Context context) {
        Intent intent = new Intent(context, SearchService.class);
        intent.setAction(ACTION_SHARE);
        context.startService(intent);
    }

    /**
     * Metodo che gestisce l'intent
     * @param   intent intent a cui si fa riferimento
     */
    @WorkerThread
    protected void onHandleIntent(Intent intent) {
        String query = "";
        MVC mvc = ((FlickrApplication) getApplication()).getMVC();
        switch (intent.getAction()) {
            case ACTION_SEARCH_STR:
                query = String.format("https://api.flickr.com/services/rest?method=flickr.photos.search&api_key=%s&text=%s&extras=owner_name,url_sq,url_l,description,tags&per_page=50",
                        API_KEY,
                        (String) intent.getSerializableExtra(PARAM_S));
                break;

            case ACTION_SEARCH_LAST:
                query = String.format("https://api.flickr.com/services/rest?method=flickr.photos.getRecent&api_key=%s&extras=owner_name,url_sq,url_l,description,tags&per_page=50",
                        API_KEY);
                break;

            case ACTION_SEARCH_TOP:
                query = String.format("https://api.flickr.com/services/rest?method=flickr.interestingness.getList&api_key=%s&extras=owner_name,url_sq,url_l,description,tags&per_page=50",
                        API_KEY);
                break;

            case ACTION_SEARCH_OWN:
                query = String.format("https://api.flickr.com/services/rest?method=flickr.people.getPhotos&api_key=%s&user_id=%s&extras=owner_name,url_sq,url_l,description,tags&per_page=50",
                        API_KEY,
                        (String) intent.getSerializableExtra(PARAM_S));
                break;

            case ACTION_SELECTION:
                // Se non è ancora stata scaricata l'immagine Fhd, procedo con il suo scaricamento
                if (mvc.model.getResult(mvc.model.getImageSel()).getPicFhd() == null)
                    downloadImageFhd( mvc.model.getImageSel() );

                mvc.model.clearComments( mvc.model.getImageSel() );
                Iterable<Model.CommentImg> comments = commentsSearch( mvc.model.getResult( mvc.model.getImageSel() ).getId() );
                mvc.model.storeComments( comments, mvc.model.getImageSel() );

                // Se non è stato trovato alcun commento, lo notifico a tutte le View
                if (!comments.iterator().hasNext())
                    mvc.forEachView(View::onEmptyComments);

                break;

            case ACTION_SHARE:
                saveImageFhd( mvc.model.getImageSel() );
                break;
        }
        if (intent.getAction().equalsIgnoreCase(ACTION_SEARCH_STR) ||
                intent.getAction().equalsIgnoreCase(ACTION_SEARCH_LAST) ||
                intent.getAction().equalsIgnoreCase(ACTION_SEARCH_TOP) ||
                intent.getAction().equalsIgnoreCase(ACTION_SEARCH_OWN))
        {
            mvc.model.clearResults();
            Iterable<Model.ImgInfo> results = pictureSearch(query);

            // Se non è stato trovato alcun risultato, lo notifico a tutte le View
            if (!results.iterator().hasNext()) {
                mvc.forEachView(View::onEmptyResult);
                mvc.forEachView(View::onImgLdDownloaded);
            }
            mvc.model.storeResults(results);

            downloadImageLd(results);
        }
    }

    /**
     * Metodo che richiama il metodo downloadImage della Classe ImageService per lo scaricamento delle immagini LD
     * @param   results lista contenente le informazioni delle immagini da scaricare (useremo solo l'url)
     */
    private void downloadImageLd(Iterable<Model.ImgInfo> results){
        int i = 0;
        for (Model.ImgInfo img : results) {
            ImageService.downloadImage(SearchService.this, true, i);
            i++;
        }
    }

    /**
     * Metodo che richiama il metodo downloadImage della Classe ImageService per lo scaricamento dell'immagine Fhd
     * @param   pos posizione nell'array nel Model, qui troveremo le informazioni dell'immagine da scaricare (useremo solo l'url)
     */
    private void downloadImageFhd(int pos){
        ImageService.downloadImage(SearchService.this, false, pos);
    }

    /**
     * Metodo che richiama il metodo saveImage della Classe ImageService per il salvataggio dell'immagine Fhd
     * @param   pos posizione nell'array nel Model, qui troveremo le informazioni dell'immagine da scaricare
     */
    private void saveImageFhd(int pos){
        ImageService.saveImage(SearchService.this, pos);
    }

    /**
     * Metodo a cui viene delegata la ricerca delle immagini tramite query url
     * @param   query Url costruito utilizzato le API di Flickr per ottenere le immagini desiderate
     * @return  Iterable<Model.ImgInfo> lista dei risultati
     */
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

    /**
     * Metodo a cui viene delegata l'interpretazione della stringa risultato
     * @param   xml Stringa risultato della ricerca immagini
     * @return  Iterable<Model.ImgInfo> lista dei risultati
     */
    @WorkerThread
    private Iterable<Model.ImgInfo> parsePic(String xml) {
        List<Model.ImgInfo> infos = new LinkedList<>();
        int count = 0;
        int nextPhoto = -1;
        do {
            nextPhoto = xml.indexOf("<photo id", nextPhoto + 1);
            if (nextPhoto >= 0) {
                int idPos = xml.indexOf("id=", nextPhoto) + 4;
                int titlePos = xml.indexOf("title=", nextPhoto) + 7;
                int authorNamePos = xml.indexOf("ownername=", nextPhoto) + 11;
                int authorIdPos = xml.indexOf("owner=", nextPhoto) + 7;
                int url_sqPos = xml.indexOf("url_sq=", nextPhoto) + 8;
                int url_lPos = xml.indexOf("url_l=", nextPhoto) + 7;

                String photoId = xml.substring(idPos, xml.indexOf("\"", idPos + 1));
                String title = xml.substring(titlePos, xml.indexOf("\"", titlePos + 1));
                String author_name = xml.substring(authorNamePos, xml.indexOf("\"", authorNamePos + 1));
                String author_id = xml.substring(authorIdPos, xml.indexOf("\"", authorIdPos + 1));
                String url_sq = xml.substring(url_sqPos, xml.indexOf("\"", url_sqPos + 1));
                String url_l = xml.substring(url_lPos, xml.indexOf("\"", url_lPos + 1));

                infos.add(new Model.ImgInfo(photoId, title, author_id, author_name, url_sq, url_l, null));
                ++count;
            }
        }
        while (nextPhoto != -1);
        Log.d(TAG, "Risultati trovati: " + count);
        return infos;
    }

    /**
     * Metodo a cui viene delegata l'interpretazione della stringa risultato
     * @param   xml Stringa risultato della ricerca commenti
     * @return  Iterable<Model.CommentImg> lista dei commenti
     */
    @WorkerThread
    private Iterable<Model.CommentImg> parseCom(String xml) {
        List<Model.CommentImg> comments = new LinkedList<>();

        int count = 0;
        int nextComment = -1;
        do {
            nextComment = xml.indexOf("<comment id", nextComment + 1);
            if (nextComment >= 0) {
                int authornamePos = xml.indexOf("authorname=", nextComment) + 12;
                int commentPos = xml.indexOf("\">", nextComment) + 2;
                String authorname = xml.substring(authornamePos, xml.indexOf("\"", authornamePos + 1));
                String comment = xml.substring(commentPos, xml.indexOf("<", commentPos + 1));
                comments.add(new Model.CommentImg(authorname,comment));
                ++count;
            }
        }
        while (nextComment != -1);
        Log.d(TAG, "Commenti trovati: " + count);
        return comments;
    }

    /**
     * Metodo a cui viene delegata la ricerca dei commenti tramite id della foto
     * @param   photoId id della foto di cui vogliamo ottenere i commenti
     * @return  Iterable<Model.CommentImg> lista dei commenti
     */
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

}