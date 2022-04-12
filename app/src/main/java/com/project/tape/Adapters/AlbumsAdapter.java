package com.project.tape.Adapters;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.tape.R;
import com.project.tape.SecondaryClasses.Album;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.AlbumViewHolder> {

    private Context mContext;
    private OnAlbumListener onAlbumListener;
    MediaMetadataRetriever retriever = new MediaMetadataRetriever();

    public static List<Album> mAlbum;

    LruCache<Long, Bitmap> mBitmapCache;
    BitmapDrawable mPlaceholder;

    private int position;

    public AlbumsAdapter(Context context, List<Album> albums) {
        mAlbum = new ArrayList<>();
        if(albums != null) {
            mAlbum.addAll(albums);
        }
        mContext = context;
        mPlaceholder = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.default_cover);
        // Get the maximum size of byte we are allowed to allocate on the VM head and convert it to bytes.
        int maxSize = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Divide the maximum size by eight to get a adequate size the LRU cache should reach before it starts to evict bitmaps.
        int cacheSize = maxSize / 8;
        mBitmapCache = new LruCache<Long, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(Long key, Bitmap value) {
                // returns the size of bitmaps in kilobytes.
                return value.getByteCount() / 1024;
            }
        };
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false);
        AlbumViewHolder vHolder = new AlbumViewHolder(v, onAlbumListener);
        return vHolder;
    }

    protected void sortAlbumsList() {
        //Throwing out duplicates from list
        //Sorting albums
        Collections.sort(mAlbum, new Comparator<Album>() {
            @Override
            public int compare(Album lhs, Album rhs) {
                return lhs.getAlbumName().toLowerCase().compareTo(rhs.getAlbumName().toLowerCase());
            }
        });

        //Creates iterator and throws out duplicates
        Iterator<Album> iterator = mAlbum.iterator();
        String album = "";
        while (iterator.hasNext()) {
            Album track = iterator.next();
            String currentAlbum = track.getAlbumName().toLowerCase();
            if (currentAlbum.equals(album)) {
                iterator.remove();
            } else {
                album = currentAlbum;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        holder.tv_album_title.setText(mAlbum.get(position).getAlbumName());
        Album album = mAlbum.get(position);
        // Check the Bitmap cache for the album art first..
        final Bitmap bitmap = mBitmapCache.get(album.getAlbumId());
        // If the bitmap is not null, then use the cached images.
        if(bitmap != null){
            holder.album_cover_albumFragment.setImageBitmap(bitmap);
        }
        else {
            // No album art could be found in the cache try reloading it.
            // In a real work example you should check that this value is not some junk value indicating that their is no album artwork.
            loadAlbumArt(holder.album_cover_albumFragment, album.getAlbumId());
        }
    }

    /**
     * Adds a {@link List} of {@link Album} to the adapters.
     * This method replaces the current music items inside of the adapter with the specified music items.
     * @param albums
     */
    public void addItems(List<Album> albums) {
        // Clear the old items. I only do this so that I don't have to do duplicating checks on the music items.
        mAlbum.clear();
        // Add the new music list.
        mAlbum.addAll(albums);
        sortAlbumsList();
        notifyItemRangeInserted(0, albums.size());
    }

    /**
     * Clears the {@link Album} items inside of this adapter.
     */
    public void clearItem() {
        mAlbum.clear();
    }

    /**
     * Helper method for asynchronously loading album art.
     * @param icon
     * @param albumId
     */
    public void loadAlbumArt(ImageView icon, long albumId) {
        // Check the current album art task if any and cancel it, if it is loading album art that doesn't match the specified album id.
        if(cancelLoadTask(icon, albumId)) {
            // There was either no task running or it was loading a different image so create a new one to load the proper image.
            LoadAlbumArt loadAlbumArt = new LoadAlbumArt(icon, mContext);
            // Store the task inside of the async drawable.
            AsyncDrawable drawable = new AsyncDrawable(mContext.getResources(), mPlaceholder.getBitmap(),loadAlbumArt);
            icon.setImageDrawable(drawable);
            loadAlbumArt.execute(albumId);
        }
    }

    /**
     * Helper method cancelling {@link LoadAlbumArt}.
     *
     * @param icon
     * @param albumId
     * @return
     */
    public boolean cancelLoadTask(ImageView icon, long albumId) {
        LoadAlbumArt loadAlbumArt = (LoadAlbumArt) getLoadTask(icon);
        // If the task is null return true because we want to try and load the album art.
        if(loadAlbumArt == null) {
            return true;
        }
        if(loadAlbumArt != null) {
            // If the album id differs cancel this task because it cannot be recycled for this imageview.
            if(loadAlbumArt.albumId != albumId) {
                loadAlbumArt.cancel(true);
                return true;
            }
        }
        return false;
    }

    /**
     * Helper method for extracting an {@link LoadAlbumArt}.
     * @param icon
     * @return
     */
    public AsyncTask getLoadTask(ImageView icon) {
        LoadAlbumArt task = null;
        Drawable drawable = icon.getDrawable();
        if(drawable instanceof AsyncDrawable) {
            task = ((AsyncDrawable) drawable).getLoadArtworkTask();
        }
        return task;
    }

    private class LoadAlbumArt extends AsyncTask<Long, Void, Bitmap> {

        // URI that points to the AlbumArt database.
        private final Uri albumArtURI = Uri.parse("content://media/external/audio/albumart");
        public WeakReference<ImageView> mIcon;
        // Holds a publicly accessible albumId to be checked against.
        public long albumId;
        private Context mContext;
        int width, height;

        public LoadAlbumArt(ImageView icon, Context context) {
            // Store a weak reference to the imageView.
            mIcon = new WeakReference<ImageView>(icon);
            // Store the width and height of the imageview.
            // This is necessary for properly scalling the bitmap.
            width = icon.getWidth();
            height = icon.getHeight();
            mContext = context;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(isCancelled() || bitmap == null){
                return;
            }
            // Check to make sure that the imageview has not been garbage collected as well as the
            // LoadArtworkTask is the same as this one.
            if(mIcon != null && mIcon.get() != null) {
                ImageView icon = mIcon.get();
                Drawable drawable = icon.getDrawable();
                if(drawable instanceof AsyncDrawable) {
                    LoadAlbumArt task = ((AsyncDrawable) drawable).getLoadArtworkTask();
                    // Make sure that this is the same task as the one current stored inside of the ImageView's drawable.
                    if(task != null && task == this) {
                        icon.setImageBitmap(bitmap);
                    }
                }
            }
            mBitmapCache.put(albumId, bitmap);
            super.onPostExecute(bitmap);
        }

        @Override
        protected Bitmap doInBackground(Long... params) {
            // AsyncTask are not guaranteed to start immediately and could be cancelled somewhere in between calling doInBackground.
            if(isCancelled()){
                return null;
            }
            albumId = params[0];
            // Append the albumId to the end of the albumArtURI to create a new Uri that should point directly to the album art if it exist.
            Uri albumArt = ContentUris.withAppendedId(albumArtURI, albumId);
            Bitmap bmp = null;
            try {
                // Decode the bitmap.
                bmp = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), albumArt);
                // Create a scalled down version of the bitmap to be more memory efficient.
                // THe smaller the bitmap the more items we can store inside of the LRU cache.
                bmp = Bitmap.createScaledBitmap(bmp, 200, 200, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bmp;
        }
    }
    /**
     * Custom drawable that holds a LoadArtworkTask
     */
    private static class AsyncDrawable extends BitmapDrawable {
        WeakReference<LoadAlbumArt> loadArtworkTaskWeakReference;

        public AsyncDrawable(Resources resources, Bitmap bitmap, LoadAlbumArt task) {
            super(resources, bitmap);
            // Store the LoadArtwork task inside of a weak reference so it can still be garbage collected.
            loadArtworkTaskWeakReference = new WeakReference<LoadAlbumArt>(task);
        }

        public LoadAlbumArt getLoadArtworkTask() {
            return loadArtworkTaskWeakReference.get();
        }
    }

    @Override
    public int getItemCount() {
        return mAlbum.size();
    }


    public class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_album_title;
        ImageView album_cover_albumFragment;
        OnAlbumListener onAlbumListener;

        public AlbumViewHolder(@NonNull View itemView, OnAlbumListener onAlbumListener) {
            super(itemView);
            this.onAlbumListener = onAlbumListener;
            tv_album_title = (TextView) itemView.findViewById(R.id.album_title_albumFragment);
            album_cover_albumFragment = (ImageView) itemView.findViewById(R.id.album_cover_albumFragment);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            try {
                onAlbumListener.onAlbumClick(position);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void updateAlbumList(ArrayList<Album> albumsArrayList) {
        mAlbum = new ArrayList<>();
        mAlbum.addAll(albumsArrayList);
        notifyDataSetChanged();
    }

    public interface OnAlbumListener {
        void onAlbumClick(int position) throws IOException;
    }

}

