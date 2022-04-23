package com.project.tape.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.project.tape.Fragments.AlbumsFragment;
import com.project.tape.Fragments.ArtistsFragment;
import com.project.tape.Fragments.PlaylistsFragment;
import com.project.tape.Fragments.SongsFragment;


public class FragmentsAdapter extends FragmentStateAdapter {

    public FragmentsAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new AlbumsFragment();
            case 2:
                return new ArtistsFragment();
            case 3:
                return new PlaylistsFragment();
        }
        return new SongsFragment();
    }

    @Override
    public int getItemCount() {
        return 4;
    }


}
