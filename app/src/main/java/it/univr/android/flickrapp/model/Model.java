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
    private final LinkedList<ImgInfo> resultsAuthor = new LinkedList<>();

    private int image_sel;

    @Immutable
    public static class ImgInfo {
        private final static DateFormat format = new SimpleDateFormat("MMM d, yyyy, HH:mm:ss");
        private final String id;
        private final String title;
        private final String author_id;
        private final String author_name;
        private final String url_sq;
        private final String url_l;
        private final Bitmap img_thmb;
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

        public CommentImg[] getComments() {
            synchronized (commentList) {
                return commentList.toArray(new CommentImg[commentList.size()]);
            }
        }

        @Override
        public String toString() {
            return title + "\n" + author_id + "\n" + url_l;
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

        mvc.forEachView(View::onModelChanged);
    }

    public void clearResults() {
        if (mvc.controller.getSwitchedView())
            synchronized (this.results) {
                this.results.clear();
            }
        else
            synchronized (this.resultsAuthor) {
                this.resultsAuthor.clear();
            }

        mvc.forEachView(View::onModelChanged);
    }

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

        this.mvc.forEachView(View::onModelChanged);
    }

    public void clearComments(int pos) {
        if (mvc.controller.getSwitchedView())
            synchronized (this.results.get(pos).commentList) {
                this.results.get(pos).commentList.clear();
            }
        else
            synchronized (this.resultsAuthor.get(pos).commentList) {
                this.resultsAuthor.get(pos).commentList.clear();
            }

        mvc.forEachView(View::onModelChanged);
    }

    public void storeImgFhd(Bitmap img_fhd, int pos) {
        if (mvc.controller.getSwitchedView()) {
            if (this.results.get(pos).img_fhd == null)
                this.results.get(pos).img_fhd = img_fhd;
            else
                synchronized (this.results.get(pos).img_fhd) {
                    this.results.get(pos).img_fhd = img_fhd;
                }
        } else {
            if (this.resultsAuthor.get(pos).img_fhd == null)
                this.resultsAuthor.get(pos).img_fhd = img_fhd;
            else
                synchronized (this.resultsAuthor.get(pos).img_fhd) {
                    this.resultsAuthor.get(pos).img_fhd = img_fhd;
                }
        }
    }

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

    public ImgInfo getResult(int pos, boolean choice) {
        if (choice)
            synchronized (this.results)  {
                return this.results.get(pos);
            }
        else
            synchronized (this.resultsAuthor)  {
                return this.resultsAuthor.get(pos);
            }
    }

}