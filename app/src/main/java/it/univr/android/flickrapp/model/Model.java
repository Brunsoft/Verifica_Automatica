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
        private final String url_l;
        private Uri uri;
        private Bitmap img_thmb;
        private Bitmap img_fhd;
        private final LinkedList<CommentImg> commentList;

        public ImgInfo(String id, String title, String author_id, String author_name, String url_sq, String url_l, Bitmap bmp) {
            this.id = id;
            this.title = title;
            this.author_id = author_id;
            this.author_name = author_name;
            this.url_sq = url_sq;
            this.url_l = url_l;
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

        public Bitmap getPicLd(){
            return img_thmb;
        }

        public Uri getUri() { return uri; }

        public CommentImg[] getComments() {
            synchronized (commentList) {
                return commentList.toArray(new CommentImg[commentList.size()]);
            }
        }

        public void setImgFhd(Bitmap img_fhd){
            this.img_fhd = img_fhd;
        }

        public void setImgLd(Bitmap img_thmb){
            this.img_thmb = img_thmb;
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
    public void setImageSel(int image_sel){
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
            return this.image_sel;
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
     * @param   position posizione dell'immagine d'interesse nella lista corretta
     */
    public void setImageFhdSel(Bitmap img, int position){
        if (mvc.controller.getSwitchedView())
            synchronized (results) {
                results.get(position).setImgFhd(img);
            }
        else
            synchronized (resultsAuthor) {
                resultsAuthor.get(position).setImgFhd(img);
            }
        mvc.forEachView(View::onImgFhdDownloaded);
    }

    /**
     * Metodo per settare l'immagine Bitmap Ld dell'immagine selezionata
     * @param   img Bitmap da salvare
     * @param   position posizione dell'immagine d'interesse nella lista corretta
     */
    public void setImageLdSel(Bitmap img, int position){
        if (mvc.controller.getSwitchedView())
            synchronized (results) {
                results.get(position).setImgLd(img);
            }
        else
            synchronized (resultsAuthor) {
                resultsAuthor.get(position).setImgLd(img);
            }

        mvc.forEachView(View::onImgLdDownloaded);
    }

    /**
     * Metodo per settare l'Uri dell'immagine Fhd selezionata per lo share
     * @param   uri Uri da salvare
     * @param   position posizione dell'immagine d'interesse nella lista corretta
     */
    public void setUri(Uri uri, int position){
        if (mvc.controller.getSwitchedView())
            synchronized (results) {
                results.get(position).setUri(uri);
            }
        else
            synchronized (resultsAuthor) {
                resultsAuthor.get(position).setUri(uri);
            }
    }

    /**
     * Metodo per memorizzare la lista dei nuovi risultati
     * @param   results risultati della ricerca immagini da memorizzare nella lista corretta
     */
    public void storeResults(Iterable<ImgInfo> results) {
        if (mvc.controller.getSwitchedView())
            synchronized (this.results) {
                for (ImgInfo img: results)
                    this.results.add(img);
            }
        else
            synchronized (this.resultsAuthor) {
                for (ImgInfo img: results)
                    this.resultsAuthor.add(img);
            }

        mvc.forEachView(View::onResultsChanged);
    }

    /**
     * Metodo per svuotare la lista, corretta, dei risultati
     */
    public void clearResults() {
        if (mvc.controller.getSwitchedView())
            synchronized (this.results) {
                this.results.clear();
            }
        else
            synchronized (this.resultsAuthor) {
                this.resultsAuthor.clear();
            }
    }

    /**
     * Metodo per memorizzare la lista dei nuovi commenti
     * @param   commentList risultati della ricerca commenti da memorizzare nella lista corretta
     * @param   pos posizione dell'immagine a cui i commenti si riferiscono
     */
    public void storeComments(Iterable<CommentImg> commentList, int pos) {
        if (mvc.controller.getSwitchedView())
            synchronized (this.results.get(pos).commentList) {
                for (CommentImg comment: commentList)
                    this.results.get(pos).commentList.add(comment);
            }
        else
            synchronized (this.resultsAuthor.get(pos).commentList) {
                for (CommentImg comment: commentList)
                    this.resultsAuthor.get(pos).commentList.add(comment);
            }

        this.mvc.forEachView(View::onResultsChanged);
    }

    /**
     * Metodo per svuotare la lista, corretta, dei commenti
     * @param   pos posizione dell'immagine a cui si trovano i commenti interessati
     */
    public void clearComments(int pos) {
        if (mvc.controller.getSwitchedView())
            synchronized (this.results.get(pos).commentList) {
                this.results.get(pos).commentList.clear();
            }
        else
            synchronized (this.resultsAuthor.get(pos).commentList) {
                this.resultsAuthor.get(pos).commentList.clear();
            }
    }

    /**
     * Metodo per ottenere la lista, corretta, dei risultati
     * @return   ImgInfo[] Array contenente le informazioni desiderate
     */
    public ImgInfo[] getResults() {
        if (mvc.controller.getSwitchedView())
            synchronized (this.results) {
                return this.results.toArray(new ImgInfo[this.results.size()]);
            }
        else
            synchronized (this.resultsAuthor) {
                return this.resultsAuthor.toArray(new ImgInfo[this.resultsAuthor.size()]);
            }
    }

    /**
     * Metodo per ottenere la lista, corretta, dei risultati
     * @param   pos posizione dell'immagine a cui ci si riferisce, nella lista corretta
     * @return  ImgInfo oggetto contenente le informazioni desiderate
     */
    public ImgInfo getResult(int pos) {
        if (mvc.controller.getSwitchedView())
            synchronized (this.results)  {
                return this.results.get(pos);
            }
        else
            synchronized (this.resultsAuthor)  {
                return this.resultsAuthor.get(pos);
            }
    }

}