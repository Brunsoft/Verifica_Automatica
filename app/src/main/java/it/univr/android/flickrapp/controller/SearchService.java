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
    private final static String ACTION_SELECTION = "pic_sel";
    private final static String ACTION_SELECTION_OWN = "pic_sel_own";
    private final static String ACTION_SHARE = "pic-share";
    private final static String ACTION_SHARE_OWN = "pic_share_own";
    private final static String PARAM_S = "search_str";
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
     * Metodo che crea un intent per lanciare un servizio che andrà a scaricare l'immagine selezionata, dell'autore selezionato, in Fhd e i relativi commenti
     * @param   context contesto a cui si fa riferimento
     */
    @UiThread
    static void viewOwnPictureSel(Context context) {
        Intent intent = new Intent(context, SearchService.class);
        intent.setAction(ACTION_SELECTION_OWN);
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
     * Metodo che crea un intent per lanciare un servizio che andrà a salvare in memoria l'immagine da condividere, dell'autore selezionato
     * @param   context contesto a cui si fa riferimento
     */
    @UiThread
    static void shareOwnPictureSel(Context context) {
        Intent intent = new Intent(context, SearchService.class);
        intent.setAction(ACTION_SHARE_OWN);
        context.startService(intent);
    }

    /**
     * Metodo che gestisce l'intent
     * @param   intent intent a cui si fa riferimento
     */
    @WorkerThread
    protected void onHandleIntent(Intent intent) {
        String query = "";
        boolean choice;
        MVC mvc = ((FlickrApplication) getApplication()).getMVC();
        if (intent.getAction().equalsIgnoreCase(ACTION_SEARCH_OWN) || intent.getAction().equalsIgnoreCase(ACTION_SELECTION_OWN) || intent.getAction().equalsIgnoreCase(ACTION_SHARE_OWN))
            choice = false;
        else
            choice = true;

        switch (intent.getAction()) {
            case ACTION_SEARCH_STR:
                query = String.format("https://api.flickr.com/services/rest?method=flickr.photos.search&api_key=%s&text=%s&extras=owner_name,url_sq,description,tags&per_page=50", API_KEY, (String) intent.getSerializableExtra(PARAM_S));
                break;

            case ACTION_SEARCH_LAST:
                query = String.format("https://api.flickr.com/services/rest?method=flickr.photos.getRecent&api_key=%s&extras=owner_name,url_sq,description,tags&per_page=50", API_KEY);
                break;

            case ACTION_SEARCH_TOP:
                query = String.format("https://api.flickr.com/services/rest?method=flickr.interestingness.getList&api_key=%s&extras=owner_name,url_sq,description,tags&per_page=50", API_KEY);
                break;

            case ACTION_SEARCH_OWN:
                query = String.format("https://api.flickr.com/services/rest?method=flickr.people.getPhotos&api_key=%s&user_id=%s&extras=owner_name,url_sq,description,tags&per_page=50", API_KEY, (String) intent.getSerializableExtra(PARAM_S));
                break;

            case ACTION_SELECTION:
            case ACTION_SELECTION_OWN:
                try {
                    // Se non è ancora stata scaricata l'immagine Fhd, procedo con il suo scaricamento
                    if (mvc.model.getResult(mvc.model.getImageSel(), choice).getPicFhd() == null) {
                        // Se non è ancora stata scaricata l'immagine, nemmeno l'url_l è disponibile quindi lo ricavo
                        mvc.model.getResult(mvc.model.getImageSel(), choice).setUrl_l(getPhotoSize(mvc.model.getResult(mvc.model.getImageSel(), choice).getId()));
                        downloadImageFhd(mvc.model.getImageSel(), mvc.model.getResult(mvc.model.getImageSel(), choice).getId(), choice);
                    }

                    mvc.model.clearComments(mvc.model.getImageSel(), choice);
                    Iterable<Model.CommentImg> comments = commentsSearch(mvc.model.getResult(mvc.model.getImageSel(), choice).getId());
                    mvc.model.storeComments(comments, mvc.model.getImageSel(), choice);

                    // Se non è stato trovato alcun commento, lo notifico a tutte le View
                    if (!comments.iterator().hasNext())
                        mvc.forEachView(View::onEmptyComments);
                }
                catch (Exception e) {
                    Log.e(TAG, e.toString());
                    return;
                }

                break;

            case ACTION_SHARE:
            case ACTION_SHARE_OWN:
                try {
                    if (mvc.model.getResult(mvc.model.getImageSel(), choice).getUrl_l() == null)
                        mvc.model.getResult(mvc.model.getImageSel(), choice).setUrl_l(getPhotoSize(mvc.model.getResult(mvc.model.getImageSel(), choice).getId()));

                    saveImageFhd( mvc.model.getImageSel(), mvc.model.getResult(mvc.model.getImageSel(), choice ).getId(), choice);
                }
                catch (Exception e) {
                    Log.e(TAG, e.toString());
                    return;
                }
                break;
        }
        if (intent.getAction().equalsIgnoreCase(ACTION_SEARCH_STR) || intent.getAction().equalsIgnoreCase(ACTION_SEARCH_LAST) || intent.getAction().equalsIgnoreCase(ACTION_SEARCH_TOP) || intent.getAction().equalsIgnoreCase(ACTION_SEARCH_OWN)) {
            mvc.model.clearResults(choice);
            Iterable<Model.ImgInfo> results = pictureSearch(query);

            // Se non è stato trovato alcun risultato, lo notifico a tutte le View
            if (!results.iterator().hasNext()) {
                mvc.forEachView(View::onEmptyResult);

                return;
            }

            mvc.model.storeResults(results, choice);
            downloadImageLd(results, choice);
        }
    }

    /**
     * Metodo che richiama il metodo downloadImage della Classe ImageService per lo scaricamento delle immagini LD
     * @param   results lista contenente le informazioni delle immagini da scaricare (useremo solo l'url)
     * @param   choice se true salvo nella lista result altrimenti nella lista resultAuthor
     */
    private void downloadImageLd(Iterable<Model.ImgInfo> results, boolean choice){
        int i = 0;
        for (Model.ImgInfo img : results) {
            ImageService.downloadImage(SearchService.this, true, img.getId(), i, choice);
            i++;
        }
    }

    /**
     * Metodo che richiama il metodo downloadImage della Classe ImageService per lo scaricamento dell'immagine Fhd
     * @param   pos posizione nell'array nel Model, qui troveremo le informazioni dell'immagine da scaricare (useremo solo l'url)
     * @param   choice se true salvo nella lista result altrimenti nella lista resultAuthor
     */
    private void downloadImageFhd(int pos, String photo_id, boolean choice){
        ImageService.downloadImage(SearchService.this, false, photo_id, pos, choice);
    }

    /**
     * Metodo che richiama il metodo saveImage della Classe ImageService per il salvataggio dell'immagine Fhd
     * @param   pos posizione nell'array nel Model, qui troveremo le informazioni dell'immagine da scaricare
     * @param   choice se true salvo nella lista result altrimenti nella lista resultAuthor
     */
    private void saveImageFhd(int pos, String photo_id, boolean choice){
        ImageService.saveImage(SearchService.this, photo_id, pos, choice);
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

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null)
                answer += line + "\n";

            in.close();

            return parsePic(answer);
        }
        catch (Exception e) {
            Log.e(TAG, e.toString());
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

                String photoId = xml.substring(idPos, xml.indexOf("\"", idPos + 1));
                String title = xml.substring(titlePos, xml.indexOf("\"", titlePos + 1));
                String author_name = xml.substring(authorNamePos, xml.indexOf("\"", authorNamePos + 1));
                String author_id = xml.substring(authorIdPos, xml.indexOf("\"", authorIdPos + 1));
                String url_sq = xml.substring(url_sqPos, xml.indexOf("\"", url_sqPos + 1));

                infos.add(new Model.ImgInfo(photoId, title, author_id, author_name, url_sq, null));
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

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null)
                answer += line + "\n";

            in.close();

            return parseCom(answer);
        }
        catch (Exception e) {
            Log.e(TAG, e.toString());
            return Collections.emptyList();
        }
    }

    /**
     * Metodo a cui viene delegata la ricerca degli url, con diversa definizione, all'immagine selezionata
     * @param   photoId id della foto di cui vogliamo ottenere gli url
     * @return  String stringa contenente l'url desiderato con definizione massima 1024
     */
    @WorkerThread
    private String getPhotoSize(String photoId) {
        String query = String.format("https://api.flickr.com/services/rest?method=flickr.photos.getSizes&api_key=%s&photo_id=%s",
                API_KEY,
                photoId);
        Log.d(TAG, query);
        try {
            URL url = new URL(query);
            URLConnection conn = url.openConnection();
            String answer = "";

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null)
                answer += line + "\n";

            in.close();

            return parseSizes(answer);
        }
        catch (Exception e) {
            Log.e(TAG, e.toString());
            return "";
        }
    }

    /**
     * Metodo a cui viene delegata l'interpretazione della stringa risultato
     * @param   xml Stringa risultato della ricerca dimensioni dell'immagine da caricare
     * @return  String stringa contenente l'url desiderato con definizione massima 1024
     */
    @WorkerThread
    private String parseSizes(String xml) {

        String best_url = "https://s.yimg.com/pw/images/en-us/photo_unavailable.png";
        int best_width = -1;
        int best_height = -1;
        int nextSize = -1;

        // Controllo se ci sono stati errori durante la ricerca delle Pics
        int rspPos = xml.indexOf("<rsp stat=", 0) + 11;
        String rsp = xml.substring(rspPos, xml.indexOf("\"", rspPos + 1));

        // Log.d(TAG, rspPos + " -> " + rsp + "\n" + xml);
        // In caso di errori ritorno un link ad una immagine d'errore
        if (rsp.equalsIgnoreCase("fail"))
            return best_url;

        do {
            nextSize = xml.indexOf("<size label", nextSize + 1);
            if (nextSize >= 0) {
                int widthPos = xml.indexOf("width=", nextSize) + 7;
                int heightPos = xml.indexOf("height=", nextSize) + 8;
                int urlPos = xml.indexOf("source=", nextSize) + 8;

                int width = Integer.parseInt(xml.substring(widthPos, xml.indexOf("\"", widthPos + 1)));
                int height = Integer.parseInt(xml.substring(heightPos, xml.indexOf("\"", heightPos + 1)));
                String url = xml.substring(urlPos, xml.indexOf("\"", urlPos + 1));

                if ((best_width < width && best_height < height) && (width <= 1024 && height <= 1024)){
                    best_width = width;
                    best_height = height;
                    best_url = url;
                }
            }
        }
        while (nextSize != -1);
        return best_url;
    }
}