package it.univr.android.flickrapp.view;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;
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
    private TextView no_comments;
    private ProgressDialog progr_load;
    private ProgressDialog progr_share;

    @Override @UiThread
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable @Override @UiThread
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picture_fhd, container, false);
        img_fhd = (ImageView)view.findViewById(R.id.picture_fhd);
        img_comment = (ListView)view.findViewById(R.id.picture_comments);
        no_comments = (TextView)view.findViewById(R.id.no_comments);

        progr_load = new ProgressDialog(getActivity());
        progr_share = new ProgressDialog(getActivity());

        return view;
    }

    @Override @UiThread
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mvc = ((FlickrApplication) getActivity().getApplication()).getMVC();

        ImgInfo imgInfo = mvc.model.getResult(mvc.model.getImageSel());
        img_comment.setAdapter(new PictureAdapter());
        getListViewSize(img_comment);

        if (mvc.model.getResult(mvc.model.getImageSel()).getPicFhd() == null) {
            progr_load = ProgressDialog.show(getActivity(), getResources().getText(R.string.wait_title), getResources().getText(R.string.wait_mess), true);
            progr_load.setCancelable(false);
        }else
            img_fhd.setImageBitmap(mvc.model.getResult(mvc.model.getImageSel()).getPicFhd());

        onResultsChanged();
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
                progr_share = ProgressDialog.show(getActivity(), getResources().getText(R.string.wait_title), getResources().getText(R.string.wait_mess), true);
                progr_share.setCancelable(false);
                mvc.controller.sharePictureSel(getActivity());
                return true;
            case R.id.menu_info:
                Dialog d = new Dialog(getActivity());
                d.setTitle(getResources().getText(R.string.info_button));
                d.setContentView(R.layout.dialog_info);
                d.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override @UiThread
    public void onResultsChanged() {
        img_comment.setAdapter(new PictureAdapter());
        getListViewSize(img_comment);
    }

    @Override @UiThread
    public void onEmptyResult() { }

    @Override @UiThread
    public void onEmptyComments() {
        no_comments.setText(R.string.empty_comments);
    }

    @Override @UiThread
    public void onImgLdDownloaded() { }

    @Override @UiThread
    public void onImgFhdDownloaded() {
        progr_load.dismiss();
        img_fhd.setImageBitmap(mvc.model.getResult(mvc.model.getImageSel()).getPicFhd());
    }

    @Override @UiThread
    public void onImgFhdSaved() {
        progr_share.dismiss();
        Uri uri = mvc.model.getResult(mvc.model.getImageSel()).getUri();
        Log.d("IMG Uri: ", uri.toString());

        Intent intent = new Intent().setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT, getResources().getText(R.string.share_mess));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, getResources().getText(R.string.share_title)));
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