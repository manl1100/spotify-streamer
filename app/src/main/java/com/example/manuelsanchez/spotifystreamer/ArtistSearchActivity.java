package com.example.manuelsanchez.spotifystreamer;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import java.util.ArrayList;

public class ArtistSearchActivity extends Activity
        implements ArtistSearchFragment.OnArtistSelectedListener, ArtistTopTracksFragment.OnTrackSelectedListener {

    private ArtistSearchFragment mSearchActivity;
    private ArtistTopTracksFragment mTopTrackActivity;
    private boolean mIsTwoPane;

    private MusicPlayerService mMusicPlayerService;
    private Intent playIntent;
    private boolean musicPlayerBound = false;


    private ServiceConnection mMusicPlayerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicPlayerService.MusicPlayerBinder binder = (MusicPlayerService.MusicPlayerBinder) iBinder;
            mMusicPlayerService = binder.getService();
            mMusicPlayerService.setTracks(new ArrayList<ArtistTopTrackItem>());
            musicPlayerBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicPlayerBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_search);

        mSearchActivity = (ArtistSearchFragment) getFragmentManager().findFragmentById(R.id.fragment_search);
        mTopTrackActivity = (ArtistTopTracksFragment) getFragmentManager().findFragmentById(R.id.fragment_top_tracks);

        View topTrackView = findViewById(R.id.fragment_top_tracks);
        mIsTwoPane = topTrackView != null && topTrackView.getVisibility() == View.VISIBLE;

        if (mIsTwoPane) {
            mTopTrackActivity.setOnTrackSelectedListener(this);
        }
        mSearchActivity.setOnArtistSelectedListener(this);

    }

    @Override
    public void onArtistSelected(String artistId) {
        if (mIsTwoPane) {
            mTopTrackActivity.displayArtistTracks(artistId);
        } else {
            Intent intent = new Intent(getApplicationContext(), ArtistTopTracksActivity.class);
            intent.putExtra(ArtistSearchFragment.SELECTED_ARTIST_ID, artistId);
            startActivity(intent);
        }

    }

    @Override
    public void onTrackSelected(ArtistTopTrackItem track) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ArtistTopTracksFragment.TRACK, track);

        FragmentManager fragmentManager = getFragmentManager();
        MusicPlayerFragment musicPlayer = new MusicPlayerFragment();
        musicPlayer.setArguments(bundle);

        musicPlayer.show(fragmentManager, "dialog");
    }
}
