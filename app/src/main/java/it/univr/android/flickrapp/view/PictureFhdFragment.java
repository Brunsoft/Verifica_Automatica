package it.univr.android.flickrapp.view;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_share, menu);
    }

    @Override @UiThread
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_share:
                mvc.controller.sharePictureSel(getActivity());
                return true;
            case R.id.menu_info:
                mvc.controller.showInfo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override @UiThread
    public void onModelChanged() {
        ImgInfo imgInfo = mvc.model.getResult(mvc.model.getImageSel());
        img_comment.setAdapter(new PictureAdapter());
        getListViewSize(img_comment);
        img_fhd.setImageBitmap(imgInfo.getPicFhd());
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
            ((TextView) row.findViewById(R.id.comment_author)).setText(comment.getAuthorName());
            ((TextView) row.findViewById(R.id.comment)).setText(comment.getComment());
            return row;
        }
    }

    public static void getListViewSize(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        // set listAdapter in loop for getting final size
        int totalHeight = 0;
        for (int size = 0; size < listAdapter.getCount(); size++) {
            View listItem = listAdapter.getView(size, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        // setting listview item in adapter
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

}