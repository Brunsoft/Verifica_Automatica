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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import it.univr.android.flickrapp.FlickrApplication;
import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.R;
import it.univr.android.flickrapp.model.Model;
import it.univr.android.flickrapp.model.Model.ImgInfo;

import static android.content.ContentValues.TAG;

/**
 * SearchResultsFragment è la classe che permette di visualizzare i risultati di ricerca
 */
public class SearchResultsFragment extends Fragment implements AbstractFragment {
    protected MVC mvc;
    protected TextView empty_results;
    protected ListView results_list;
    protected ArrayAdapter<ImgInfo> results_adapter;

    private ProgressDialog progr_share;     // mostra il progresso del processo di condivisione dell'img Ld

    @Override @UiThread
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable @Override @UiThread
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_results, container, false);

        // controllo della disponibilità di rete
        checkNetworkAvailable();

        empty_results = (TextView)view.findViewById(R.id.empty_results);
        results_list = (ListView)view.findViewById(R.id.results_list);
        registerForContextMenu(results_list);
        progr_share = new ProgressDialog(getActivity());

        return view;
    }

    @Override @UiThread
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mvc = ((FlickrApplication) getActivity().getApplication()).getMVC();

        onResultsChanged();
    }

    @Override @UiThread
    public void onResultsChanged() {
        // switchedView -> true siamo in SearchResultsFragment
        mvc.controller.setSwitchedView(true);
        results_adapter = new SearchAdapter(getActivity());
        results_list.setAdapter(results_adapter);
    }

    /**
     * Metodo chiamato dal Controller quando la lista dei risultati è vuota, mostra "Nessun risultato trovato"
     */
    @Override @UiThread
    public void onEmptyResult() {
        empty_results.setText(R.string.empty_results);
    }

    @Override @UiThread
    public void onEmptyComments() { }

    /**
     * Metodo chiamato dal Controller quando lo scaricamento dell'immagine Ld è completato
     */
    @Override @UiThread
    public void onImgLdDownloaded() {
        results_adapter.notifyDataSetChanged();
    }

    @Override @UiThread
    public void onImgFhdDownloaded() { }

    /**
     * Metodo chiamato dal Controller quando il salvataggio dell'immagine Fhd è completato
     * Avvia l'intent incaricato di condividere il contenuto in questione
     */
    @Override @UiThread
    public void onImgFhdSaved() {
        try {
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
        catch (Exception e){
            Log.e(TAG, e.toString());
            return;
        }
    }

    /**
     * Metodo invocato automaticamente per la creazione del Menu. Viene disabilitato il pulsante share.
     */
    @Override @UiThread
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_share, menu);
        menu.removeItem(R.id.menu_item_share);
    }

    /**
     * Metodo invocato automaticamente alla selezione di una delle voci di menu.
     * @param   item Voce del menu selezionata
     */
    @Override @UiThread
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
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

    /**
     * Metodo invocato automaticamente per la creazione del ContextMenu (Clik lungo).
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(R.string.context_menu_title);
        String[] menuItems = getResources().getStringArray(R.array.context_menu);
        for (int i = 0; i < menuItems.length; i++) {
            String menu_item = menuItems[i];
            if ( i==1 )     // Altre foto dell' "autore"
                menu_item += " " + mvc.model.getResult(((AdapterView.AdapterContextMenuInfo) menuInfo).position, true).getAuthor_name();

            menu.add(Menu.NONE, i, i, menu_item);
        }
    }

    /**
     * Metodo invocato automaticamente alla selezione di una delle voci di ContextMenu.
     * @param   item Voce del ContextMenu selezionata
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch (item.getItemId()){
            case 0:
                Log.d("SRF", "Scelta SHARE");

                // C'è connessione e ci sono i permessi di lettura/scrittura in memoria e condivido
                if (checkNetworkAvailable())
                    checkDataPermission(info.position);

                break;
            case 1:
                Log.d("SRF", "Scelta search");
                if (checkNetworkAvailable()){
                    String author_id = mvc.model.getResult(info.position, true).getAuthor_id();
                    // switchedView -> false siamo in SearchResultsAuthorFragment
                    mvc.controller.setSwitchedView(false);
                    mvc.controller.search(getActivity(), 3, author_id);
                    mvc.controller.showResultsAuthor();
                }
                break;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * SearchAdapter è la classe che gestisce la visualizzazione dei risultati di ricerca
     */
    protected class SearchAdapter extends ArrayAdapter<ImgInfo> {

        private final Model.ImgInfo[] imgInfos = mvc.model.getResults(mvc.controller.getSwitchedView());

        SearchAdapter(Activity context) {
            super(context, R.layout.fragment_result_item, mvc.model.getResults(mvc.controller.getSwitchedView()));
        }

        @NonNull @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.fragment_result_item, parent, false);
                row.setLongClickable(true);
            }

            Model.ImgInfo imgInfo = imgInfos[position];
            ((ImageView) row.findViewById(R.id.icon)).setImageBitmap(imgInfo.getThmb());
            ((TextView) row.findViewById(R.id.title)).setText(imgInfo.getTitle());
            ((TextView) row.findViewById(R.id.author_name)).setText(imgInfo.getAuthor_name());

            if (mvc.controller.getSwitchedView())
                row.setOnClickListener(__ -> viewImageSel(position));
            else
                row.setOnClickListener(__ -> viewOwnImageSel(position));

            return row;
        }

        /**
         * Metodo di SearchAdapter invocato dai listener di ogni singolo risultato per visualizzare l'immagine
         * in una data posizione
         * @param   position Posizione dell'immagine da visualizzare nella lista corretta
         */
        private void viewImageSel(int position){
            if (checkNetworkAvailable()) {
                mvc.model.setImageSel(position);
                mvc.controller.viewPictureSel(getActivity());
                mvc.controller.showPictureFhd();
            }
        }

        /**
         * Metodo di SearchAdapter invocato dai listener di ogni singolo risultato per visualizzare l'immagine
         * dell'autore in questione in una data posizione
         * @param   position Posizione dell'immagine da visualizzare nella lista corretta
         */
        private void viewOwnImageSel(int position){
            if (checkNetworkAvailable()) {
                mvc.model.setImageSel(position);
                mvc.controller.viewOwnPictureSel(getActivity());
                mvc.controller.showPictureFhd();
            }
        }
    }

    /**
     * Metodo utilizzato per controllare la disponibilità della rete.
     */
    protected boolean checkNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        // in caso di assenza di connessione viene visualizzato un messaggio di errore
        if (!(netInfo != null && netInfo.isConnected())){
            Toast.makeText(getActivity(), getResources().getText(R.string.network_warning), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Metodo che controlla i permessi attuali dell'app e in caso non siano concessi viene affettuata la richiesta all'utente.
     * Il metodo onRequestPermissionsResult è implementato nella classe MainActivity
     */
    protected void checkDataPermission(int position) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            // Permesso Negato
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            // Permesso Garantito
            progr_share = ProgressDialog.show(getActivity(), getResources().getText(R.string.wait_title), getResources().getText(R.string.wait_mess), true);
            progr_share.setCancelable(false);
            mvc.model.setImageSel(position);

            if (mvc.controller.getSwitchedView())
                mvc.controller.sharePictureSel(getActivity());
            else
                mvc.controller.shareOwnPictureSel(getActivity());
        }
    }

}