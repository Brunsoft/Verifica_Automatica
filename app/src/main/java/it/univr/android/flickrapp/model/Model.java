package it.univr.android.flickrapp.model;

import android.graphics.Bitmap;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.view.View;

@ThreadSafe
public class Model {
    private MVC mvc;

    @GuardedBy("Itself")
    private final LinkedList<ImgInfo> results = new LinkedList<>();
    private int image_sel;

    @Immutable
    public static class ImgInfo {
        private final static DateFormat format = new SimpleDateFormat("MMM d, yyyy, HH:mm:ss");
        private final String id;
        private final String title;
        private final String author;
        private final String url_sq;
        private final String url_l;
        private final Bitmap img_thmb;
        private Bitmap img_fhd;
        private final LinkedList<CommentImg> commentList;

        public ImgInfo(String id, String title, String author, String url_sq, String url_l, Bitmap bmp) {
            this.id = id;
            this.title = title;
            this.author = author;
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

        public String getAuthor() {
            return author;
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

        public CommentImg[] getComments() {
            synchronized (commentList) {
                return commentList.toArray(new CommentImg[commentList.size()]);
            }
        }

        @Override
        public String toString() {
            return title + "\n" + author + "\n" + url_l;
        }

    }

    public void setImageSel(int image_sel){
        this.image_sel = image_sel;
    }

    public int getImageSel(){
        return this.image_sel;
    }

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

    public void setMVC(MVC mvc) {
        this.mvc = mvc;
    }

    public void storeResults(Iterable<ImgInfo> results) {
        synchronized (this.results) {

            for (ImgInfo img: results)
                this.results.add(img);
        }

        mvc.forEachView(View::onModelChanged);
    }

    public void clearResults() {
        synchronized (this.results) {
            this.results.clear();
        }

        mvc.forEachView(View::onModelChanged);
    }

    public void storeComments(Iterable<CommentImg> commentList, Bitmap img_fhd, int pos) {
        synchronized (this.results.get(pos).commentList) {
            for (CommentImg comment: commentList)
                this.results.get(pos).commentList.add(comment);
        }
        if (this.results.get(pos).img_fhd == null)
            this.results.get(pos).img_fhd = img_fhd;
        else
            synchronized (this.results.get(pos).img_fhd){
                this.results.get(pos).img_fhd = img_fhd;
            }

        this.mvc.forEachView(View::onModelChanged);
    }

    public void clearComments(int pos) {
        synchronized (this.results.get(pos).commentList) {
            this.results.get(pos).commentList.clear();
        }

        mvc.forEachView(View::onModelChanged);
    }

    public ImgInfo[] getResults() {
        synchronized (this.results) {
            return this.results.toArray(new ImgInfo[this.results.size()]);
        }
    }

    public ImgInfo getResult(int pos) {
        synchronized (this.results)  {
            return this.results.get(pos);
        }
    }

}