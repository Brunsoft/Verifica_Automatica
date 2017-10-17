package it.univr.android.flickrapp.view;

/**
 * @author  Luca Vicentini, Maddalena Zuccotto
 * @version 1.0 */

import android.Manifest;
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
import android.widget.TextView;
import android.widget.Toast;

import it.univr.android.flickrapp.FlickrApplication;
import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.R;
import it.univr.android.flickrapp.model.Model.CommentImg;
import it.univr.android.flickrapp.model.Model.ImgInfo;

/**
 * PictureFhdFragment è la classe che permette di visualizzare l'immagine selezionata in
 * SearchResultsFragment o SearchResultsAuthorFragment in Fhd e i relativi commenti
 */
public class PictureFhdFragment extends Fragment implements AbstractFragment {
    private MVC mvc;
    private ImageView img_fhd;
    private ListView img_comment;
    private TextView no_comments;
    private ProgressDialog progr_load;      // mostra il progresso del caricamento dell'img Fhd e i commenti
    private ProgressDialog progr_share;     // mostra il progresso del processo di condivisione dell'img Fhd

    @Override @UiThread
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable @Override @UiThread
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        // controllo della disponibilità di rete
        checkNetworkAvailable();

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

        // Se l'immagine Fhd non è disponibili nei risultati mostro "Caricamento in corso" nell'attesa del completamento del download
        if (mvc.model.getResult(mvc.model.getImageSel()).getPicFhd() == null) {
            progr_load = ProgressDialog.show(getActivity(), getResources().getText(R.string.wait_title), getResources().getText(R.string.wait_mess), true);
            progr_load.setCancelable(false);
        }else
            img_fhd.setImageBitmap(mvc.model.getResult(mvc.model.getImageSel()).getPicFhd());

        onResultsChanged();
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
        img_comment.setAdapter(new PictureAdapter());
        getListViewSize(img_comment);
    }

    @Override @UiThread
    public void onEmptyResult() { }

    /**
     * Metodo chiamato dal Controller quando la lista dei commenti è vuota, mostra "Nessun commento trovato"
     */
    @Override @UiThread
    public void onEmptyComments() {
        no_comments.setText(R.string.empty_comments);
    }

    @Override @UiThread
    public void onImgLdDownloaded() { }

    /**
     * Metodo chiamato dal Controller quando lo scaricamento dell'immagine Fhd è completato
     */
    @Override @UiThread
    public void onImgFhdDownloaded() {
        progr_load.dismiss();
        img_fhd.setImageBitmap(mvc.model.getResult(mvc.model.getImageSel()).getPicFhd());
    }

    /**
     * Metodo chiamato dal Controller quando il salvataggio dell'immagine Fhd è completato
     * Avvia l'intent incaricato di condividere il contenuto in questione
     */
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

    /**
     * PictureAdapter è la classe che gestisce la visualizzazione dei commenti
     */
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

    /**
     * Metodo incaricato di cambiare l'altezza della ListView contenente tutti i commenti dell'immagine
     * abilita così lo scroll della ScrollView
     */
    public static void getListViewSize(ListView listView) {
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
            mvc.controller.sharePictureSel(getActivity());
        }
    }

}