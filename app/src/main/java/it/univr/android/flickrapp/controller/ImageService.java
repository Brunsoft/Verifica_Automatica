package it.univr.android.flickrapp.controller;

/**
 * @author  Luca Vicentini, Maddalena Zuccotto
 * @version 1.0 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.univr.android.flickrapp.ExecutorIntentService;
import it.univr.android.flickrapp.FlickrApplication;
import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.view.View;

/**
 * ImageService è una classe per gestire l'esecuzione parallela di più servizi
 * gestisce il download e il salvataggio in memoria delle immagini
 */
public class ImageService extends ExecutorIntentService {
    private final static String TAG = ImageService.class.getName();
    private final static int num_thread = 4;
    private final static String ACTION_DWN_IMG_FHD = "download_img_fhd";        // is_ld = false
    private final static String ACTION_DWN_IMG_LD = "download_img_ld";          // is_ld = true
    private final static String ACTION_SAVE_IMG_FHD = "save_img_fhd";
    private final static String AUTHOR_PAGE = "";
    private final static String PARAM_POS = "position";

    /**
     * Costruttore della classe corrente
     */
    public ImageService() {
        super("download_share service");
    }

    /**
     * Metodo che ritorna un ExecutorService composto da num_thread
     * @return  ExecutorService
     */
    @Override
    protected ExecutorService mkExecutorService() {
        return Executors.newFixedThreadPool(num_thread);
    }

    /**
     * Metodo che crea un intent per lanciare un servizio che andrà a scaricare l'immagine
     * @param   context contesto a cui si fa riferimento
     * @param   is_ld quale immagine scaricare, true > LD, false > FHD
     * @param   position posizione della foto cui si fa rifermento nella lista situata nel Model
     * @param   choice se true salvo nella lista result altrimenti nella lista resultAuthor
     */
    @UiThread
    static void downloadImage(Context context, boolean is_ld, int position, boolean choice) {
        Intent intent = new Intent(context, ImageService.class);
        if (is_ld) {
            intent.setAction(ACTION_DWN_IMG_LD);
            intent.putExtra(PARAM_POS, position);
        } else {
            intent.setAction(ACTION_DWN_IMG_FHD);
            intent.putExtra(PARAM_POS, position);
        }
        intent.putExtra(AUTHOR_PAGE, choice);
        context.startService(intent);
    }

    /**
     * Metodo che crea un intent per lanciare un servizio che andrà a salvare l'immagine
     * @param   context contesto a cui si fa riferimento
     * @param   position posizione della foto cui si fa rifermento nella lista situata nel Model
     * @param   choice se true salvo nella lista result altrimenti nella lista resultAuthor
     */
    @UiThread
    static void saveImage(Context context, int position, boolean choice) {
        Intent intent = new Intent(context, ImageService.class);
        intent.setAction(ACTION_SAVE_IMG_FHD);
        intent.putExtra(PARAM_POS, position);
        intent.putExtra(AUTHOR_PAGE, choice);
        context.startService(intent);
    }

    /**
     * Metodo che gestisce l'intent
     * @param   intent intent a cui si fa riferimento
     */
    @WorkerThread
    protected void onHandleIntent(Intent intent) {
        String url = "";
        int position;
        boolean choice = intent.getBooleanExtra(AUTHOR_PAGE, true);
        Log.d(TAG, "Azione di sharing val: "+ choice);
        Bitmap img;
        MVC mvc = ((FlickrApplication) getApplication()).getMVC();
        switch (intent.getAction()) {
            case ACTION_DWN_IMG_FHD:
                Log.d(TAG, ACTION_DWN_IMG_FHD);
                position = intent.getIntExtra(PARAM_POS, -1);
                url = mvc.model.getResult(position, choice).getUrl_l();
                img = getPic(url);
                mvc.model.setImageFhdSel(img, position, choice);
                break;

            case ACTION_DWN_IMG_LD:
                Log.d(TAG, ACTION_DWN_IMG_LD);
                position = intent.getIntExtra(PARAM_POS, -1);
                url = mvc.model.getResult(position, choice).getUrl_sq();
                img = getPic(url);
                mvc.model.setImageLdSel(img, position, choice);

                break;

            case ACTION_SAVE_IMG_FHD:
                Log.d(TAG, ACTION_DWN_IMG_LD);
                position = intent.getIntExtra(PARAM_POS, -1);
                if (mvc.model.getResult(position, choice).getPicFhd() == null){
                    url = mvc.model.getResult(position, choice).getUrl_l();
                    img = getPic(url);
                    mvc.model.setImageFhdSel(img, position, choice);
                }

                mvc.model.setUri(
                        saveToInternalStorage(
                                mvc.model.getResult(position, choice).getPicFhd(),
                                mvc.model.getResult(position, choice).getId()),
                        position, choice );
                mvc.forEachView(View::onImgFhdSaved);
                break;
        }
    }

    /**
     * Metodo che scarica, tramite url, l'immagine in Bitmap
     * @param   url indirizzo dell'immagine
     * @return  bm immagine scaricata in Bitmap
     */
    @WorkerThread
    private static Bitmap getPic(String url) {
        Bitmap bm = null;
        try {
            Log.d(TAG, url);
            InputStream is = new java.net.URL(url).openStream();
            bm = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            Log.e(TAG, "Errore durante lo scaricamento dell'img " + e.getMessage());
        }
        return bm;
    }

    /**
     * Metodo che salva l'immagine in memoria
     * @param   img_fhd Bitmap rappresentante l'immagine FHD
     * @param   id_img Stringa contenente l'id, futuro nome identificativo, dell'immagine
     * @return  imgUri Uri che punta all'immagine in memoria
     */
    @WorkerThread
    private Uri saveToInternalStorage(Bitmap img_fhd, String id_img) {
        Uri imgUri = null;
        try {
            // Save into folder /storage/emulated/0/FlickrApp/
            String path = Environment.getExternalStorageDirectory().getPath() + "/FlickrApp/";
            Log.d(TAG, path);
            File file = new File(path);

            if (!file.exists()) {
                Log.e(TAG, "Directory not exist!");
                if (!file.mkdirs())
                    Log.e(TAG, "Directory not created!");
            }

            FileOutputStream out = new FileOutputStream(path + id_img + ".png");
            img_fhd.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            imgUri = Uri.fromFile(new File(path + id_img + ".png"));
        } catch (IOException e) {
            Log.e(TAG, "Errore durante il salvataggio dell'imgFhd " + e.getMessage());
        }
        return imgUri;
    }
}