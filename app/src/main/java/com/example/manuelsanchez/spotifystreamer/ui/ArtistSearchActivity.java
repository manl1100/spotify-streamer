package com.example.manuelsanchez.spotifystreamer.ui;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.manuelsanchez.spotifystreamer.R;
import com.example.manuelsanchez.spotifystreamer.model.ArtistTopTrackItem;

import java.util.ArrayList;

import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.SELECTED_ARTIST_ID;


public class ArtistSearchActivity extends BaseActivity
        implements ArtistSearchFragment.OnArtistSelectedListener, ArtistTopTracksFragment.OnTrackSelectedListener {

    private ArtistSearchFragment mSearchActivity;
    private ArtistTopTracksFragment mTopTrackActivity;
    private boolean mIsTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_search);

        mSearchActivity = (ArtistSearchFragment) getFragmentManager().findFragmentById(R.id.fragment_search);
        mTopTrackActivity = (ArtistTopTracksFragment) getFragmentManager().findFragmentById(R.id.fragment_top_tracks);

        View topTrackView = findViewById(R.id.fragment_top_tracks);
        mIsTwoPane = topTrackView != null && topTrackView.getVisibility() == View.VISIBLE;

    }

    @Override
    public void onArtistSelected(String artistId) {
        if (mIsTwoPane) {
            mTopTrackActivity.displayArtistTracks(artistId);
        } else {
            Intent intent = new Intent(getApplicationContext(), ArtistTopTracksActivity.class);
            intent.putExtra(SELECTED_ARTIST_ID, artistId);
            startActivity(intent);
        }

    }

    @Override
    public void onTrackSelected(ArrayList<ArtistTopTrackItem> tracks, int trackIndex) {
        FragmentManager fragmentManager = getFragmentManager();
        MusicPlayerFragment musicPlayerFragment = MusicPlayerFragment.newInstance(tracks, trackIndex);
        musicPlayerFragment.show(fragmentManager, "dialog");
    }


}
