package it.univr.android.flickrapp.model;

/**
 * @author  Luca Vicentini, Maddalena Zuccotto
 * @version 1.0 */

import android.graphics.Bitmap;
import android.net.Uri;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import java.util.LinkedList;

import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.view.View;

@ThreadSafe
public class Model {
    private final static String TAG = Model.class.getName();
    private MVC mvc;

    /**
     * Stringa che permette di identificare il tipo di device su cui stiamo eseguendo l'App
     * Utilizzato principalmente per togliere il menu dalla fragment Search del tablet.
     */
    public static String device = "";

    /**
     * LinkedList contenente i risulatti di ricerca generica (SearchResultFragment)
     */
    @GuardedBy("Itself")
    private final LinkedList<ImgInfo> results = new LinkedList<>();

    /**
     * LinkedList contenente i risulatti di ricerca generica (SearchResultAuthorFragment)
     */
    @GuardedBy("Itself")
    private final LinkedList<ImgInfo> resultsAuthor = new LinkedList<>();

    @GuardedBy("this")
    private int image_sel;

    /**
     * Classe contenente le informazioni della singola immagine
     */
    @Immutable
    public static class ImgInfo {
        private final String id;
        private final String title;
        private final String author_id;
        private final String author_name;
        private final String url_sq;
        private String url_l;
        private Uri uri;
        private Bitmap img_thmb;
        private Bitmap img_fhd;
        private final LinkedList<CommentImg> commentList;

        public ImgInfo(String id, String title, String author_id, String author_name, String url_sq, Bitmap bmp) {
            this.id = id;
            this.title = title;
            this.author_id = author_id;
            this.author_name = author_name;
            this.url_sq = url_sq;
            this.img_thmb = bmp;
            this.commentList = new LinkedList<>();
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor_id() {
            return author_id;
        }

        public String getAuthor_name() {
            return author_name;
        }

        public String getUrl_sq(){
            return url_sq;
        }

        public String getUrl_l(){
            return url_l;
        }

        public Bitmap getThmb(){
            return img_thmb;
        }

        public Bitmap getPicFhd(){
            return img_fhd;
        }

        public Uri getUri() { return uri; }

        public CommentImg[] getComments() { return commentList.toArray(new CommentImg[commentList.size()]); }

        public void setImgFhd(Bitmap img_fhd){
            this.img_fhd = img_fhd;
        }

        public void setImgLd(Bitmap img_thmb){
            this.img_thmb = img_thmb;
        }

        public void setUrl_l(String url_l){
            this.url_l = url_l;
        }

        public void setUri(Uri uri){
            this.uri = uri;
        }

        @Override
        public String toString() {
            return title + "\n" + author_id + "\n" + url_l;
        }

    }

    /**
     * Classe contenente il commento e relativo autore
     */
    @Immutable
    public static class CommentImg {
        private final String authorName;
        private final String comment;

        public CommentImg(String authorName, String comment){
            this.authorName = authorName;
            this.comment = comment;
        }

        public String getAuthorName() {
            return this.authorName;
        }

        public String getComment() {
            return this.comment;
        }

    }

    /**
     * Metodo per settare il valore di image_sel
     * @param   image_sel immagine attualmente selezionata e pronta per la visualizzazione Fhd o Share
     */
    public void setImageSel(int image_sel) {
        synchronized (this) {
            this.image_sel = image_sel;
        }
    }

    /**
     * Metodo per ricavare il valore di image_sel
     * @return   image_sel immagine attualmente selezionata e pronta per la visualizzazione Fhd o Share
     */
    public int getImageSel(){
        synchronized (this) {
            return image_sel;
        }
    }

    /**
     * Metodo per settare l'mvc
     * @param   mvc oggetto mvc da settare
     */
    public void setMVC(MVC mvc) {
        this.mvc = mvc;
    }

    /**
     * Metodo per settare l'immagine Bitmap Fhd dell'immagine selezionata
     * @param   img Bitmap da salvare
     * @param   photo_id id dell'immagine alla quale ci si riferisce
     * @param   position posizione dell'immagine d'interesse nella lista corretta
     * @param   choice se true salvo nella lista result altrimenti nella lista resultAuthor
     */
    public void setImageFhdSel(Bitmap img, String photo_id, int position, boolean choice){
        if (choice)
            synchronized (results) {
                if (results.get(position).getId().equalsIgnoreCase(photo_id))
                    results.get(position).setImgFhd(img);
            }
        else
            synchronized (resultsAuthor) {
                if (resultsAuthor.get(position).getId().equalsIgnoreCase(photo_id))
                    resultsAuthor.get(position).setImgFhd(img);
            }
        mvc.forEachView(View::onImgFhdDownloaded);
    }

    /**
     * Metodo per settare l'immagine Bitmap Ld dell'immagine selezionata
     * @param   img Bitmap da salvare
     * @param   photo_id id dell'immagine alla quale ci si riferisce
     * @param   position posizione dell'immagine d'interesse nella lista corretta
     * @param   choice se true salvo nella lista result altrimenti nella lista resultAuthor
     */
    public void setImageLdSel(Bitmap img, String photo_id, int position, boolean choice){
        if (choice)
            synchronized (results) {
                if (results.get(position).getId().equalsIgnoreCase(photo_id))
                    results.get(position).setImgLd(img);
            }
        else
            synchronized (resultsAuthor) {
                if (resultsAuthor.get(position).getId().equalsIgnoreCase(photo_id))
                    resultsAuthor.get(position).setImgLd(img);
            }

        mvc.forEachView(View::onImgLdDownloaded);
    }

    /**
     * Metodo per settare l'Uri dell'immagine Fhd selezionata per lo share
     * @param   uri Uri da salvare
     * @param   photo_id id dell'immagine alla quale ci si riferisce
     * @param   position posizione dell'immagine d'interesse nella lista corretta
     * @param   choice se true salvo nella lista result altrimenti nella lista resultAuthor
     */
    public void setUri(Uri uri, String photo_id, int position, boolean choice){
        if (choice)
            synchronized (results) {
                if (results.get(position).getId().equalsIgnoreCase(photo_id))
                    results.get(position).setUri(uri);
            }
        else
            synchronized (resultsAuthor) {
                if (resultsAuthor.get(position).getId().equalsIgnoreCase(photo_id))
                    resultsAuthor.get(position).setUri(uri);
            }
    }

    /**
     * Metodo per memorizzare la lista dei nuovi risultati
     * @param   results risultati della ricerca immagini da memorizzare nella lista corretta
     * @param   choice se true salvo nella lista result altrimenti nella lista resultAuthor
     */
    public void storeResults(Iterable<ImgInfo> results, boolean  choice) {
        if (choice)
            synchronized (this.results) {
                for (ImgInfo img: results)
                    this.results.add(img);
            }
        else
            synchronized (resultsAuthor) {
                for (ImgInfo img: results)
                    resultsAuthor.add(img);
            }

        mvc.forEachView(View::onResultsChanged);
    }

    /**
     * Metodo per svuotare la lista, corretta, dei risultati
     * @param   choice se true salvo nella lista result altrimenti nella lista resultAuthor
     */
    public void clearResults(boolean choice) {
        if (choice)
            synchronized (results) {
                results.clear();
            }
        else
            synchronized (resultsAuthor) {
                resultsAuthor.clear();
            }
    }

    /**
     * Metodo per memorizzare la lista dei nuovi commenti
     * @param   commentList risultati della ricerca commenti da memorizzare nella lista corretta
     * @param   pos posizione dell'immagine a cui i commenti si riferiscono
     * @param   choice se true salvo nella lista result altrimenti nella lista resultAuthor
     */
    public void storeComments(Iterable<CommentImg> commentList, int pos, boolean choice) {
        if (choice)
            synchronized (results) {
                for (CommentImg comment: commentList)
                    results.get(pos).commentList.add(comment);
            }
        else
            synchronized (resultsAuthor) {
                for (CommentImg comment: commentList)
                    resultsAuthor.get(pos).commentList.add(comment);
            }

        this.mvc.forEachView(View::onResultsChanged);
    }

    /**
     * Metodo per svuotare la lista, corretta, dei commenti
     * @param   pos posizione dell'immagine a cui si trovano i commenti interessati
     * @param   choice se true salvo nella lista result altrimenti nella lista resultAuthor
     */
    public void clearComments(int pos, boolean choice) {
        if (choice)
            synchronized (results) {
                results.get(pos).commentList.clear();
            }
        else
            synchronized (resultsAuthor) {
                resultsAuthor.get(pos).commentList.clear();
            }
    }

    /**
     * Metodo per ottenere la lista, corretta, dei risultati
     * @param   choice se true salvo nella lista result altrimenti nella lista resultAuthor
     * @return   ImgInfo[] Array contenente le informazioni desiderate
     */
    public ImgInfo[] getResults(boolean choice) {
        if (choice)
            synchronized (results) {
                return results.toArray(new ImgInfo[this.results.size()]);
            }
        else
            synchronized (resultsAuthor) {
                return resultsAuthor.toArray(new ImgInfo[this.resultsAuthor.size()]);
            }
    }

    /**
     * Metodo per ottenere la lista, corretta, dei risultati
     * @param   pos posizione dell'immagine a cui ci si riferisce, nella lista corretta
     * @param   choice se true salvo nella lista result altrimenti nella lista resultAuthor
     * @return  ImgInfo oggetto contenente le informazioni desiderate
     */
    public ImgInfo getResult(int pos, boolean choice) {
        if (choice)
            synchronized (results) {
                return results.get(pos);
            }
        else
            synchronized (resultsAuthor) {
                return resultsAuthor.get(pos);
            }
    }

    /**
     * Metodo per riuscire a capire se le WorkerThreads hanno terminato lo scaricamento delle img_ld
     * @param   choice se true salvo nella lista result altrimenti nella lista resultAuthor
     * @return  boolean true se tutte le img_ld sono state scaricate, false alt.
     */
    public boolean downloadLdCompleted(boolean choice){
        if (choice)
            synchronized (results){
                int i = 0;
                for (ImgInfo img : results) {
                    if (img.getThmb() != null)
                        i++;
                }
                if (i == results.size())
                    return true;
            }
        else
            synchronized (resultsAuthor){
                int i = 0;
                for (ImgInfo img : resultsAuthor) {
                    if (img.getThmb() != null)
                        i++;
                }
                if (i == resultsAuthor.size())
                    return true;
            }

        return false;
    }

}