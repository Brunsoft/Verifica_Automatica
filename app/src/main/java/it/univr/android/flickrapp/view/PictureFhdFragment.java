package it.univr.android.flickrapp.view;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import it.univr.android.flickrapp.FlickrApplication;
import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.R;
import it.univr.android.flickrapp.model.Model.CommentImg;
import it.univr.android.flickrapp.model.Model.ImgInfo;

public class PictureFhdFragment extends Fragment implements AbstractFragment {
    private MVC mvc;
    private ImageView img_fhd;
    private ListView img_comment;

    @Override @UiThread
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable @Override @UiThread
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mvc = ((FlickrApplication) getActivity().getApplication()).getMVC();
        View view = inflater.inflate(R.layout.fragment_picture_fhd, container, false);
        img_fhd = (ImageView)view.findViewById(R.id.picture_fhd);
        img_comment = (ListView)view.findViewById(R.id.picture_comments);
        return view;
    }

    @Override @UiThread
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onModelChanged();
    }

    @Override @UiThread
    public void onModelChanged() {
        ImgInfo imgInfo = mvc.model.getResult(mvc.model.getImageSel());
        img_fhd.setImageBitmap(imgInfo.getPicFhd());
        img_comment.setAdapter(new PictureAdapter());
    }

    private class PictureAdapter extends ArrayAdapter<CommentImg> {
        private final CommentImg[] comments = mvc.model.getResult(mvc.model.getImageSel()).getComments();

        private PictureAdapter() {
            super(getActivity(), R.layout.fragment_comment_item, mvc.model.getResult(mvc.model.getImageSel()).getComments());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.fragment_comment_item, parent, false);
            }

            CommentImg comment = comments[position];
            ((TextView) row.findViewById(R.id.author)).setText(comment.getAuthorName());
            ((TextView) row.findViewById(R.id.comment)).setText(comment.getComment());
            return row;
        }
    }

}