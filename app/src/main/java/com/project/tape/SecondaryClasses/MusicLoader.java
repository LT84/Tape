package com.project.tape.SecondaryClasses;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import java.util.ArrayList;
import java.util.List;


public class MusicLoader extends AsyncTaskLoader<List<Album>> {

    List<Album> mCache;
    MusicObserver mMusicObserver;


    public MusicLoader(Context context) {
        super(context);
    }

    @Override
    public List<Album> loadInBackground() {
        final ContentResolver contentResolver = getContext().getContentResolver();
        String [] projections = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID};
        String selection = MediaStore.Audio.Media.IS_MUSIC + " =1";
        String sortOrder = MediaStore.Audio.Media.ALBUM + " ASC";
        Cursor cr = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projections, selection, null, sortOrder);
        List<Album> items = new ArrayList<>();
        if (cr != null && cr.moveToFirst()) {
            //Cache the column indexes so we don't have to look them up for every iteration of the do-while loop.
            int idIndex = cr.getColumnIndex(MediaStore.Audio.Media._ID);
            int AlbumNameIndex = cr.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int albumId =  cr.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            do {
                if(isLoadInBackgroundCanceled()){
                    return items;
                }
                //Music object to hold the music data.
                Album item = new Album();
                //Retrieve the respective music data from the cursor using the column index.
                item.setId(cr.getLong(idIndex));
                item.setAlbumName(cr.getString(AlbumNameIndex));
                item.setAlbumId(cr.getLong(albumId));
                //Once we've loaded the Music object, store it inside of the arraylist.
                items.add(item);
            }
            while(cr.moveToNext());
            cr.close();

        }
        return items;
    }

    @Override
    public void deliverResult(List<Album> data) {
        if (isReset()){
            //CLose cursors or database handles.
            return;
        }
        //Keep a reference to the loaded music data.
        mCache = data;
        //If we are started pass the loaded music to our super implementation that handles sending it to the registered activity/fragment.
        if (isStarted()){
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onStartLoading() {
        if (mCache != null) {
            deliverResult(mCache);
        }

        if (mMusicObserver == null) {
            mMusicObserver = new MusicObserver(this, new Handler());
            getContext().getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, mMusicObserver);
        }

        if (takeContentChanged() || mCache == null) {
            forceLoad();
        }
    }

    @Override
    protected void onReset() {
        //Close any cursors, web-sockets or database objects
        if(mMusicObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(mMusicObserver);
            mMusicObserver = null;
        }
    }

    /**
     * Simple observer that notifies the loader when it has detected a change.
     */
    public static class MusicObserver extends ContentObserver {

        private Loader mLoader;

        public MusicObserver(Loader loader, Handler handler) {
            super(handler);
            mLoader = loader;
        }

        @Override
        public void onChange(boolean selfChange) {
            // A change has been detect notify the Loader.
            mLoader.onContentChanged();
        }
    }


}
