package com.example.manuelsanchez.spotifystreamer;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;

import java.util.ArrayList;

import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.*;

public class ArtistTopTracksActivity extends Activity implements ArtistTopTracksFragment.OnTrackSelectedListener {

    MusicPlayerFragment mMusicPlayerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_top_ten);
    }

    @Override
    public void onTrackSelected(ArrayList<ArtistTopTrackItem> artistTracks, int trackIndex) {
        if (mMusicPlayerFragment == null) {
            mMusicPlayerFragment = new MusicPlayerFragment();
        }
        Bundle args = new Bundle();
        args.putParcelableArrayList(TRACK_ITEMS, artistTracks);
        args.putInt(TRACK_INDEX, trackIndex);
        mMusicPlayerFragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.top_tracks_container, mMusicPlayerFragment, "music_fragment");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}