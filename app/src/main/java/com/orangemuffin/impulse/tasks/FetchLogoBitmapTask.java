package com.orangemuffin.impulse.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.URL;

/* Created by OrangeMuffin on 2018-06-16 */
public class FetchLogoBitmapTask extends AsyncTask<String, Void, Bitmap> {
    FetchLogoBitmapCallback callback;

    public FetchLogoBitmapTask(FetchLogoBitmapCallback callback) {
        this.callback = callback;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        try {
            URL url = new URL(strings[0]);
            InputStream is = (InputStream) url.getContent();

            return BitmapFactory.decodeStream(is);
        } catch (Exception e) { }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        callback.onLogoBitmapFetched(bitmap);
    }

    public interface FetchLogoBitmapCallback {
        void onLogoBitmapFetched(Bitmap bitmap);
    }
}
