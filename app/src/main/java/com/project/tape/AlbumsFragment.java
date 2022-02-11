package com.project.tape;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AlbumsFragment extends Fragment implements AlbumAdapter.OnAlbumListener {


    private static final int VERTICAL_ITEM_SPACE = 5;
    private Parcelable listState;
    private RecyclerView myRecyclerView;
    private Set<Song> albumSet;
    static List<Song> albumList;
    static int position = 0;
    int  positionIndex, topView;
    TextView album_title_albumFragments;
    ImageView album_cover_albumFragment;
    LinearLayoutManager LLMAlbumFragment = new LinearLayoutManager(getContext());



    public AlbumsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.albums_fragment, container, false);

        album_title_albumFragments = (TextView) v.findViewById(R.id.album_title_albumFragment);
        myRecyclerView = (RecyclerView) v.findViewById(R.id.albums_recyclerview);
        album_cover_albumFragment = v.findViewById(R.id.album_cover_albumFragment);

        loadAudio();
        List<Song> albumListCopy = new ArrayList<Song>(albumSet);
        albumList = albumListCopy;


        //выкидываем все дубликаты альбомов

        //сортируем по альбому
        Collections.sort(albumList, new Comparator<Song>() {
            @Override
            public int compare(Song lhs, Song rhs) {
                return lhs.getAlbum().toLowerCase().compareTo(rhs.getAlbum().toLowerCase());
            }
        });

        //Создаем итератор и выкидываем дубли
        Iterator<Song> iterator = albumList.iterator();
        String album = "";
        while (iterator.hasNext()) {
            Song track = iterator.next();
            String currentAlbum = track.getAlbum().toLowerCase();
            if (currentAlbum.equals(album)) {
                iterator.remove();

            } else {

                album = currentAlbum;
            }
        }

        AlbumAdapter albumAdapter = new AlbumAdapter(getContext(),albumSet, this);

        myRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        myRecyclerView.setLayoutManager(LLMAlbumFragment);
        myRecyclerView.setAdapter(albumAdapter);
        myRecyclerView.setItemViewCacheSize(300);
        myRecyclerView.setDrawingCacheEnabled(true);
        myRecyclerView.setHasFixedSize(true);



        if (listState != null) {
            myRecyclerView.getLayoutManager().onRestoreInstanceState(listState);
            listState = savedInstanceState.getParcelable("ListState");
        }

        return v;
    }

    private void loadAudio() throws NullPointerException {
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            albumSet = new HashSet<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                // Save to audioList
                albumSet.add(new Song(data, title, album, artist, duration));
            }
        }
        cursor.close();
    }


    @Override
    public void onPause() {
        super.onPause();
        positionIndex = LLMAlbumFragment.findFirstVisibleItemPosition();
        View startView = myRecyclerView.getChildAt(0);
        topView = (startView == null) ? 0 : (startView.getTop() - myRecyclerView.getPaddingTop());
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("ListState", myRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onAlbumClick(int position) throws IOException {
        Intent intent = new Intent(getContext(), AlbumInfo.class);
        intent.putExtra("albumName",  albumList.get(position).getAlbum());
        getContext().startActivity(intent);
    }
}


