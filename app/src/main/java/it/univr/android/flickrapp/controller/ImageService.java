package it.univr.android.flickrapp.controller;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.univr.android.flickrapp.ExecutorIntentService;
import it.univr.android.flickrapp.FlickrApplication;
import it.univr.android.flickrapp.MVC;
import it.univr.android.flickrapp.view.View;

public class ImageService extends ExecutorIntentService {
    private final static String TAG = ImageService.class.getName();
    private final static int num_thread = 4;
    private final static String ACTION_DWN_IMG_FHD = "download_img_fhd";        // choice = false
    private final static String ACTION_DWN_IMG_LD = "download_img_ld";          // choice = true
    private final static String ACTION_SAVE_IMG_FHD = "save_img_fhd";
    private final static String PARAM_POS = "position";

    public ImageService() {
        super("download_share service");
    }

    @Override
    protected ExecutorService mkExecutorService() {
        return Executors.newFixedThreadPool(num_thread);
    }

    @UiThread
    static void downloadImage(Context context, boolean choice, int position) {
        //Log.d(TAG, "Choice: " + choice);
        Intent intent = new Intent(context, ImageService.class);
        if (choice) {
            intent.setAction(ACTION_DWN_IMG_LD);
            intent.putExtra(PARAM_POS, position);
        } else {
            intent.setAction(ACTION_DWN_IMG_FHD);
            intent.putExtra(PARAM_POS, position);
        }
        context.startService(intent);
    }

    @UiThread
    static void saveImage(Context context, int position) {
        Intent intent = new Intent(context, ImageService.class);
        intent.setAction(ACTION_SAVE_IMG_FHD);
        intent.putExtra(PARAM_POS, position);
        context.startService(intent);
    }

    @WorkerThread
    protected void onHandleIntent(Intent intent) {
        String url = "";
        int position;
        Bitmap img;
        MVC mvc = ((FlickrApplication) getApplication()).getMVC();
        switch (intent.getAction()) {
            case ACTION_DWN_IMG_FHD:
                position = intent.getIntExtra(PARAM_POS, -1);
                url = mvc.model.getResult(position).getUrl_l();
                img = getPic(url);
                mvc.model.setImageFhdSel(img, position);
                break;

            case ACTION_DWN_IMG_LD:
                position = intent.getIntExtra(PARAM_POS, -1);
                url = mvc.model.getResult(position).getUrl_sq();
                img = getPic(url);
                mvc.model.setImageLdSel(img, position);
                break;

            case ACTION_SAVE_IMG_FHD:
                position = intent.getIntExtra(PARAM_POS, -1);
                if (mvc.model.getResult(position).getPicFhd() == null){
                    url = mvc.model.getResult(position).getUrl_l();
                    img = getPic(url);
                    mvc.model.setImageFhdSel(img, position);
                }

                mvc.model.setUri( saveToInternalStorage( mvc.model.getResult(position).getPicFhd(), mvc.model.getResult(position).getId() ), position );
                mvc.forEachView(View::onImgFhdSaved);
                break;
        }
    }

    @WorkerThread
    private static Bitmap getPic(String url) {
        Bitmap bm = null;
        try {
            Log.d(TAG, url);
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return bm;
    }

    @WorkerThread
    private Uri saveToInternalStorage(Bitmap img_fhd, String id_img) {
        Uri bmpUri = null;
        try {
            String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + "/FlickrApp/";
            File file = new File(path);

            if (!file.exists()) {
                Log.e(TAG, "Directory not exist!");
                if (!file.mkdirs())
                    Log.e(TAG, "Directory not created!");
            }

            FileOutputStream out = new FileOutputStream(path + id_img + ".png");
            img_fhd.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(new File(path + id_img + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
}