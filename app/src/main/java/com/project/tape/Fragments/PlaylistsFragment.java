package com.project.tape.Fragments;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.project.tape.Activities.AboutFragmentItem.aboutFragmentItemOpened;
import static com.project.tape.Activities.AboutPlaylist.aboutPlaylistOpened;
import static com.project.tape.Activities.AboutPlaylist.getSongsInPlaylistMap;
import static com.project.tape.Activities.AboutPlaylist.jsonDataMap;
import static com.project.tape.Activities.AboutPlaylist.jsonMap;
import static com.project.tape.Activities.MainActivity.artistNameStr;
import static com.project.tape.Activities.MainActivity.songNameStr;
import static com.project.tape.Activities.SongInfoTab.repeatBtnClicked;
import static com.project.tape.Fragments.AlbumsFragment.albumsFragmentOpened;
import static com.project.tape.Fragments.ArtistsFragment.artistsFragmentOpened;
import static com.project.tape.Fragments.SongsFragment.songsFragmentOpened;

import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.project.tape.Activities.AboutPlaylist;
import com.project.tape.Adapters.PlaylistsAdapter;
import com.project.tape.R;
import com.project.tape.SecondaryClasses.HeadsetActionButtonReceiver;
import com.project.tape.SecondaryClasses.JsonDataMap;
import com.project.tape.SecondaryClasses.JsonDataPlaylists;
import com.project.tape.SecondaryClasses.Playlist;
import com.project.tape.SecondaryClasses.RecyclerItemClickListener;
import com.project.tape.SecondaryClasses.VerticalSpaceItemDecoration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class PlaylistsFragment extends FragmentGeneral implements MediaPlayer.OnCompletionListener, PlaylistsAdapter.OnPlaylistListener {

    Button newPlaylistBtn, closeAddingNewPlaylistBtn, addNewPlaylistBtn, closeAlertPopupBtn, deletePlaylistBtn;
    EditText addNewPlaylistEditText;

    RecyclerView playlistRecyclerView;
    PlaylistsAdapter playlistsAdapter;
    private static final int VERTICAL_ITEM_SPACE = 3;

    final int REQUEST_CODE = 1;
    private int deletePosition;

    boolean fromLongClick;
    public static boolean playlistsFragmentOpened;

    Gson gson = new Gson();
    String json;
    JsonDataPlaylists jsonDataPlaylists;

    public static ArrayList<Playlist> playlistsList = new ArrayList<>();

    Set<String> set = new HashSet<>();

    private boolean fromBackground = false;

    public static boolean clickFromPlaylistFragment;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.playlists_fragment, container, false);
        newPlaylistBtn = v.findViewById(R.id.new_playlist_btn);
        newPlaylistBtn.setOnClickListener(btnL);

        //getArraylistOf playlists
        // getSharedPlaylists();

        getSharedPlaylists();

        for (int i = 0; i < playlistsList.size(); i++) {
            set.add(playlistsList.get(i).getPlaylistName());
        }

        addNewPlaylistEditText = v.findViewById(R.id.new_playlist_name);
        song_title_main = getActivity().findViewById(R.id.song_title_main);
        artist_name_main =  getActivity().findViewById(R.id.artist_name_main);
        album_cover_main =  getActivity().findViewById(R.id.album_cover_main);
        mainPlayPauseBtn =  getActivity().findViewById(R.id.pause_button);

        //Sets adapter to list and applies settings to recyclerView
        playlistsAdapter = new PlaylistsAdapter(getContext(), playlistsList, this);
        playlistsAdapter.updatePlaylistList(playlistsList);
        playlistRecyclerView = v.findViewById(R.id.playlists_recyclerView);
        playlistRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        playlistRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        playlistRecyclerView.setAdapter(playlistsAdapter);

        //RecyclerView listener
        playlistRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), playlistRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(getActivity(), AboutPlaylist.class);
                        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle();

                        clickFromPlaylistFragment = true;

                        intent.putExtra("playlistName", playlistsList.get(position).getPlaylistName());

                        getActivity().unregisterReceiver(audioSourceChangedReceiver);

                        startActivityForResult(intent, REQUEST_CODE, bundle);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        fromLongClick = true;
                        deletePosition = position;
                        onButtonShowPopupWindowClick(view);
                    }
                })
        );
        return v;
    }

    public void onButtonShowPopupWindowClick(View view) {
        //Inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView;
        //Check which popup needed
        if (fromLongClick) {
            popupView = inflater.inflate(R.layout.popup_delete_permission, null);
        } else {
            popupView = inflater.inflate(R.layout.popup_window, null);
        }

        //Create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Show the popup window
        //Which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(view, Gravity.CENTER_HORIZONTAL, 0, -230);

        //Listeners
        if (fromLongClick) {
            closeAlertPopupBtn = popupView.findViewById(R.id.close_popup_alert_btn);
            deletePlaylistBtn = popupView.findViewById(R.id.popup_delete_btn);

            //CloseAlertPopupBtn listener
            closeAlertPopupBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }
            });
            //DeletePlaylistBtn listener
            deletePlaylistBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSongsInPlaylistMap.remove(playlistsList.get(deletePosition).getPlaylistName());
                    set.remove(playlistsList.get(deletePosition).getPlaylistName());

                    playlistsList.remove(deletePosition);

                    jsonDataMap = new JsonDataMap(getSongsInPlaylistMap);
                    jsonMap = gson.toJson(jsonDataMap);
                    getActivity().getSharedPreferences("sharedJsonStringMap", Context.MODE_PRIVATE).edit()
                            .putString("sharedJsonStringMap", jsonMap).commit();


                    playlistsAdapter.updatePlaylistList(playlistsList);
                    popupWindow.dismiss();
                }
            });
            fromLongClick = false;
        } else {
            //AddNewPlaylistBtn onClickListener
            addNewPlaylistBtn = popupView.findViewById(R.id.save_new_playlist);
            addNewPlaylistEditText = popupView.findViewById(R.id.new_playlist_name);
            addNewPlaylistBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Playlist playlist = new Playlist();
                    playlist.setPlaylistName(addNewPlaylistEditText.getText().toString());

                    if (!set.contains(addNewPlaylistEditText.getText().toString())) {
                        set.add(addNewPlaylistEditText.getText().toString());
                        playlistsList.add(playlist);
                    } else {
                        Toast.makeText(getContext(), "Playlist with this name already exists", Toast.LENGTH_SHORT).show();
                    }

                    popupWindow.dismiss();
                    playlistsAdapter.updatePlaylistList(playlistsList);
                }
            });

            //CloseAddingNewPlaylistBtn onClickListener
            closeAddingNewPlaylistBtn = popupView.findViewById(R.id.close_adding_new_playlist);
            closeAddingNewPlaylistBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }
            });
        }
        //Dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

    View.OnClickListener btnL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.new_playlist_btn:
                    onButtonShowPopupWindowClick(v);
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Save json
        jsonDataPlaylists.setArray(playlistsList);
        json = gson.toJson(jsonDataPlaylists);
        getActivity().getSharedPreferences("json", Context.MODE_PRIVATE).edit()
                .putString("json", json).commit();
    }

    //Get json
    public void getSharedPlaylists() {
        json = getActivity().getSharedPreferences("json", Context.MODE_PRIVATE)
                .getString("json", "");
        if (!json.equals("")) {
            jsonDataPlaylists = gson.fromJson(json, JsonDataPlaylists.class);
            playlistsList.addAll(jsonDataPlaylists.getArray());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        coverLoaded = false;
        songsFragmentOpened = false;
        albumsFragmentOpened = false;
        artistsFragmentOpened = false;
        aboutFragmentItemOpened = false;
        playlistsFragmentOpened = true;
        aboutPlaylistOpened = false;

        if (fromBackground) {
            getActivity().unregisterReceiver(broadcastReceiver);
            Log.i("broadcast", "unreg_PLAYLISTSFRAGMENT");
            fromBackground = false;
        }

        createChannel();
        Log.i("broadcast", "reg_PLAYLISTSFRAGMENT");
        trackAudioSource();

        //Register headphones buttons
        HeadsetActionButtonReceiver.delegate = this;
        HeadsetActionButtonReceiver.register(getActivity());

        song_title_main.setText(songNameStr);
        artist_name_main.setText(artistNameStr);
        if (mediaPlayer != null) {
            if (!coverLoaded) {
                if (uri != null) {
                    metaDataInFragment(uri);
                    coverLoaded = true;
                }
            }

            if (mediaPlayer.isPlaying()) {
                mainPlayPauseBtn.setImageResource(R.drawable.pause_song);
            } else {
                mainPlayPauseBtn.setImageResource(R.drawable.play_song);
            }
        } else {
            song_title_main.setText(" ");
            artist_name_main.setText(" ");
        }
        mediaPlayer.setOnCompletionListener(PlaylistsFragment.this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //Checking is screen locked
        KeyguardManager myKM = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
        if (myKM.inKeyguardRestrictedInputMode()) {
            //if locked
        } else {
            getActivity().unregisterReceiver(broadcastReceiver);
            Log.i("broadcast", "unreg_PLAYLISTSFRAGMENT");
        }

        if (repeatBtnClicked) {
            getContext().getSharedPreferences("repeatBtnClicked", Context.MODE_PRIVATE).edit()
                    .putBoolean("repeatBtnClicked", true).commit();
        } else {
            getContext().getSharedPreferences("repeatBtnClicked", Context.MODE_PRIVATE).edit()
                    .putBoolean("repeatBtnClicked", false).commit();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (playlistsFragmentOpened) {
            createChannel();
            Log.i("broadcast", "reg_PLAYLISTSFRAGMENT");
            fromBackground = true;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        onTrackNext();
        mediaPlayer.setOnCompletionListener(PlaylistsFragment.this);
    }

    @Override
    public void onPlaylistClick(int position) throws IOException {
    }

    @Override
    public void onMediaButtonSingleClick() {
        if (isPlaying) {
            onTrackPause();
        } else {
            onTrackPlay();
        }
    }

    @Override
    public void onMediaButtonDoubleClick() {

    }


}
