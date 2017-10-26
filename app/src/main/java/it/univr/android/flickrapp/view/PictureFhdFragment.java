package it.univr.android.flickrapp.view;

/**
 * @author  Luca Vicentini, Maddalena Zuccotto
 * @version 1.0 */

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import it.univr.android.flickrapp.FlickrApplication;
import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.R;
import it.univr.android.flickrapp.model.Model.CommentImg;

/*
 * PictureFhdFragment è la classe che permette di visualizzare l'immagine selezionata in
 * SearchResultsFragment o SearchResultsAuthorFragment in Fhd e i relativi commenti
 */
public class PictureFhdFragment extends Fragment implements AbstractFragment {
    private final static String TAG = PictureFhdFragment.class.getName();
    private MVC mvc;
    private ImageView img_fhd;
    private ListView img_comment;
    private TextView no_comments;
    private ScrollView scroll_view;
    private ArrayAdapter<CommentImg> comments_adapter;
    private boolean empty_comments;
    private ProgressBar progr_load_content;

    private ProgressDialog progr_share;     // mostra il progresso del processo di condivisione dell'img Fhd

    @Override @UiThread
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null)
            empty_comments = savedInstanceState.getBoolean(TAG + "empty_comments");
        else
            empty_comments = false;
    }

    @Nullable @Override @UiThread
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        // controllo della disponibilità di rete
        checkNetworkAvailable();

        View view = inflater.inflate(R.layout.fragment_picture_fhd, container, false);
        img_fhd = (ImageView)view.findViewById(R.id.picture_fhd);
        img_comment = (ListView)view.findViewById(R.id.picture_comments);
        no_comments = (TextView)view.findViewById(R.id.no_comments);
        scroll_view = (ScrollView)view.findViewById(R.id.scroll_result);
        progr_load_content = (ProgressBar)view.findViewById(R.id.progr_bar_picture_fhd);

        progr_share = new ProgressDialog(getActivity());

        return view;
    }

    @Override @UiThread
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mvc = ((FlickrApplication) getActivity().getApplication()).getMVC();

        progr_load_content.setVisibility(View.VISIBLE);

        // Immagine Scaricata
        if (mvc.model.getResult(mvc.model.getImageSel(), mvc.controller.getSwitchedView()).getPicFhd() != null)
            onImgFhdDownloaded();

        // Commenti Trovati
        if (mvc.model.getResult(mvc.model.getImageSel(), mvc.controller.getSwitchedView()).getComments().length != 0)
            onResultsChanged();

        // Nessun Commento Trovato
        if (empty_comments)
            onEmptyComments();

    }

    @Override @UiThread
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(TAG + "empty_comments", empty_comments);
    }

    /**
     * Metodo invocato automaticamente per la creazione del Menu.
     */
    @Override @UiThread
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_share, menu);
    }

    /**
     * Metodo invocato automaticamente alla selezione di una delle voci di menu.
     * @param   item Voce del menu selezionata
     */
    @Override @UiThread
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_share:
                // Alla selezione di "share" viene avviata l'attività per la condivisione (sharePictureSel)

                // controllo permessi di lettura/scrittura in memoria e condivido
                checkDataPermission();

                return true;
            case R.id.menu_info:
                // Alla selezione di "info" viene mostrato un Dialog contenente le info dell'App
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
        if (mvc.model.getResult(mvc.model.getImageSel(), mvc.controller.getSwitchedView()).getPicFhd() != null) {
            progr_load_content.setVisibility(View.GONE);
            comments_adapter = new PictureAdapter(getActivity());
            img_comment.setAdapter(comments_adapter);
            getListViewSize(img_comment);
        }
    }

    @Override @UiThread
    public void onEmptyResult() { }

    /**
     * Metodo chiamato dal Controller quando la lista dei commenti è vuota, mostra "Nessun commento trovato"
     */
    @Override @UiThread
    public void onEmptyComments() {
        empty_comments = true;
        if (mvc.model.getResult(mvc.model.getImageSel(), mvc.controller.getSwitchedView()).getPicFhd() != null) {
            progr_load_content.setVisibility(View.GONE);
            no_comments.setText(R.string.empty_comments);
            getListViewSize(img_comment);
        }
    }

    @Override @UiThread
    public void onImgLdDownloaded() { }

    /**
     * Metodo chiamato dal Controller quando lo scaricamento dell'immagine Fhd è completato
     */
    @Override @UiThread
    public void onImgFhdDownloaded() {
        if (mvc.model.getResult(mvc.model.getImageSel(), mvc.controller.getSwitchedView()).getComments().length != 0 || empty_comments) {
            progr_load_content.setVisibility(View.GONE);
            if (empty_comments)
                onEmptyComments();
            else
                onResultsChanged();
        }
        img_fhd.setImageBitmap(mvc.model.getResult(mvc.model.getImageSel(), mvc.controller.getSwitchedView()).getPicFhd());
    }

    /**
     * Metodo chiamato dal Controller quando il salvataggio dell'immagine Fhd è completato
     * Avvia l'intent incaricato di condividere il contenuto in questione
     */
    @Override @UiThread
    public void onImgFhdSaved() {
        progr_share.dismiss();
        Uri uri = mvc.model.getResult(mvc.model.getImageSel(), mvc.controller.getSwitchedView()).getUri();
        Log.d("IMG Uri: ", uri.toString());

        Intent intent = new Intent().setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT, getResources().getText(R.string.share_mess));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, getResources().getText(R.string.share_title)));
    }

    /**
     * PictureAdapter è la classe che gestisce la visualizzazione dei commenti
     */
    private class PictureAdapter extends ArrayAdapter<CommentImg> {
        private final CommentImg[] comments = mvc.model.getResult(mvc.model.getImageSel(), mvc.controller.getSwitchedView()).getComments();

        private PictureAdapter(Activity context) {
            super(context, R.layout.fragment_comment_item, mvc.model.getResult(mvc.model.getImageSel(), mvc.controller.getSwitchedView()).getComments());
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

    /**
     * Metodo utilizzato per controllare la disponibilità della rete.
     */
    private void checkNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        // in caso di assenza di connessione viene visualizzato un messaggio di errore
        if (!(netInfo != null && netInfo.isConnected()))
            Toast.makeText(getActivity(), getResources().getText(R.string.network_warning), Toast.LENGTH_SHORT).show();
    }

    /**
     * Metodo che controlla i permessi attuali dell'app e in caso non siano concessi viene affettuata la richiesta all'utente.
     * Il metodo onRequestPermissionsResult è implementato nella classe MainActivity
     */
    private void checkDataPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            // Permesso Negato
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            // Permesso Garantito
            progr_share = ProgressDialog.show(getActivity(), getResources().getText(R.string.wait_title), getResources().getText(R.string.wait_mess), true);
            progr_share.setCancelable(false);

            if (mvc.controller.getSwitchedView())
                mvc.controller.sharePictureSel(getActivity());
            else
                mvc.controller.shareOwnPictureSel(getActivity());
        }
    }

    /*
     * Metodo incaricato di cambiare l'altezza della ListView contenente tutti i commenti dell'immagine
     * abilita così lo scroll della ScrollView
     */
    public void getListViewSize(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        // Itero su listView per ricavare l'altezza totale
        int totalHeight = listView.getPaddingBottom() + listView.getPaddingTop();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);

        scroll_view.smoothScrollTo(0,0);
    }

}