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
import com.project.tape.ItemClasses.Playlist;
import com.project.tape.JsonFilesClasses.JsonDataMap;
import com.project.tape.JsonFilesClasses.JsonDataPlaylists;
import com.project.tape.R;
import com.project.tape.SecondaryClasses.HeadsetActionButtonReceiver;
import com.project.tape.SecondaryClasses.RecyclerItemClickListener;
import com.project.tape.SecondaryClasses.VerticalSpaceItemDecoration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;


public class PlaylistsFragment extends FragmentGeneral implements MediaPlayer.OnCompletionListener {

    Button newPlaylistBtn, closeAddingNewPlaylistBtn, addNewPlaylistBtn, closeAlertPopupBtn, deletePlaylistBtn;
    EditText addNewPlaylistEditText;

    RecyclerView playlistRecyclerView;
    public static PlaylistsAdapter playlistsAdapter;
    private static final int VERTICAL_ITEM_SPACE = 3;

    final int REQUEST_CODE = 1;
    private int deletePosition;

    boolean fromLongClick;
    private boolean fromBackground = false;
    public static boolean playlistsFragmentOpened, clickFromPlaylistFragment;

    Gson gson = new Gson();
    String json;
    JsonDataPlaylists jsonDataPlaylists = new JsonDataPlaylists();

    public static ArrayList<Playlist> playListsList = new ArrayList<>();
    Set<String> allAlbumsNamesSet = new HashSet<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.fragment_playlists, container, false);
        newPlaylistBtn = v.findViewById(R.id.new_playlist_btn);
        newPlaylistBtn.setOnClickListener(btnL);
        //Booleans
        coverLoaded = false;

        //GetArraylistOf playlists
        getSharedPlaylists();

        //Fills up HashSet to then prevent creating duplicates
        for (int i = 0; i < playListsList.size(); i++) {
            allAlbumsNamesSet.add(playListsList.get(i).getPlaylistName());
        }

        //Init views
        addNewPlaylistEditText = v.findViewById(R.id.new_playlist_name);
        playlistRecyclerView = v.findViewById(R.id.playlists_recyclerView);
        //Init views in main
        song_title_main = getActivity().findViewById(R.id.song_title_main);
        artist_name_main = getActivity().findViewById(R.id.artist_name_main);
        album_cover_main = getActivity().findViewById(R.id.album_cover_main);
        mainPlayPauseBtn = getActivity().findViewById(R.id.pause_button);

        //Sets adapter to list and applies settings to recyclerView
        playlistsAdapter = new PlaylistsAdapter(getContext(), playListsList);
        playlistsAdapter.updatePlaylistList(playListsList);
        playlistRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        playlistRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        playlistRecyclerView.setAdapter(playlistsAdapter);

        //RecyclerView listener
        playlistRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), playlistRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    //RecyclerView click listener
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(getActivity(), AboutPlaylist.class);
                        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle();
                        clickFromPlaylistFragment = true;
                        //Passes name to aboutPlaylist
                        intent.putExtra("playlistName", playListsList.get(position).getPlaylistName());
                        //Unregister audioSourceChangedReceiver
                        getActivity().unregisterReceiver(audioSourceChangedReceiver);
                        startActivityForResult(intent, REQUEST_CODE, bundle);
                    }

                    //RecyclerView long click listener
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

    @Override
    public void onResume() {
        super.onResume();
        //Booleans
        songsFragmentOpened = false;
        albumsFragmentOpened = false;
        artistsFragmentOpened = false;
        aboutFragmentItemOpened = false;
        playlistsFragmentOpened = true;
        aboutPlaylistOpened = false;
        //Unregister broadcastReceiver after app was resumed
        if (fromBackground) {
            getActivity().unregisterReceiver(broadcastReceiver);
            fromBackground = false;
        }
        createChannel();
        trackAudioSource();

        //Register headphones buttons
        HeadsetActionButtonReceiver.delegate = this;
        HeadsetActionButtonReceiver.register(getActivity());

        //Sets information about about song after activity was resumed
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
                mainPlayPauseBtn.setImageResource(R.drawable.ic_pause_song);
            } else {
                mainPlayPauseBtn.setImageResource(R.drawable.ic_play_song);
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
        }

        //Puts repeatBtn state in to shared Preferences
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
        //Creates broadcastReceiver when app is collapsed
        if (playlistsFragmentOpened) {
            createChannel();
            fromBackground = true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Save json
        jsonDataPlaylists.setArray(playListsList);
        json = gson.toJson(jsonDataPlaylists);
        getActivity().getSharedPreferences("json", Context.MODE_PRIVATE).edit()
                .putString("json", json).commit();
    }

    //Popup windows method, when user wants to add new playlist or delete existed
    public void onButtonShowPopupWindowClick(View view) {
        //Inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView;


        //Check which popup needed
        if (fromLongClick) {
            popupView = inflater.inflate(R.layout.popup_delete_permission, null);
        } else {
            popupView = inflater.inflate(R.layout.popup_window_add_new_album, null);
        }


        //Create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Show the popup window
        //Which view you pass in doesn't matter, it is only used for the window token
        popupWindow.setAnimationStyle(R.style.popupWindowAnimation);
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
                    getSongsInPlaylistMap.remove(playListsList.get(deletePosition).getPlaylistName());
                    allAlbumsNamesSet.remove(playListsList.get(deletePosition).getPlaylistName());

                    playListsList.remove(deletePosition);

                    jsonDataMap = new JsonDataMap(getSongsInPlaylistMap);
                    jsonMap = gson.toJson(jsonDataMap);
                    getActivity().getSharedPreferences("sharedJsonStringMap", Context.MODE_PRIVATE).edit()
                            .putString("sharedJsonStringMap", jsonMap).commit();


                    playlistsAdapter.updatePlaylistList(playListsList);
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

                    if (!allAlbumsNamesSet.contains(addNewPlaylistEditText.getText().toString())) {
                        allAlbumsNamesSet.add(addNewPlaylistEditText.getText().toString());
                        playListsList.add(playlist);
                    } else {
                        if (Locale.getDefault().getLanguage().equals("en")) {
                            Toast.makeText(getContext(), "Playlist with this name already exists", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Плейлист с таким именем уже есть", Toast.LENGTH_SHORT).show();
                        }
                    }
                    popupWindow.dismiss();
                    playlistsAdapter.updatePlaylistList(playListsList);
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

    //Add new playlist button listener
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

    //Get json file and add its content in playlistList
    public void getSharedPlaylists() {
        json = getActivity().getSharedPreferences("json", Context.MODE_PRIVATE)
                .getString("json", "");
        if (!json.equals("")) {
            jsonDataPlaylists = gson.fromJson(json, JsonDataPlaylists.class);
            playListsList.addAll(jsonDataPlaylists.getArray());
        }
    }

    //Switches to next song after previous is ended
    @Override
    public void onCompletion(MediaPlayer mp) {
        onTrackNext();
        mediaPlayer.setOnCompletionListener(PlaylistsFragment.this);
    }

    //Handling headphones buttons methods
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
