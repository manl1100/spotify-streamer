package com.example.manuelsanchez.spotifystreamer;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View;

public class ArtistSearchActivity extends Activity implements ArtistSearchFragment.OnArtistSelectedListener {

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

        mSearchActivity.setmOnArtistSelectedListener(this);

    }

    @Override
    public void onArtistSelected() {

    }
}
