package com.example.manuelsanchez.spotifystreamer.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;

import com.example.manuelsanchez.spotifystreamer.PlaybackController;
import com.example.manuelsanchez.spotifystreamer.R;
import com.example.manuelsanchez.spotifystreamer.model.ArtistTopTrackItem;

import java.util.ArrayList;

import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PLAY;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK_INDEX;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK_ITEMS;

/**
 * Created by Manuel Sanchez on 7/26/15
 */
public abstract class BaseActivity extends Activity implements PlaybackController.Callback {

    protected PlaybackController mPlaybackController;
    private ShareActionProvider mShareActionProvider;
    protected Context mContext;

    private MenuItem mNowPlayingMenuItem;
    private MenuItem mShareCurrentTrackMenuItem;

    private static final String IS_TRACK_PLAYING = "isTrackPlaying";
    private boolean isTrackPlaying;

    protected boolean mIsTwoPane;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlaybackController = PlaybackController.getInstance();
        if (savedInstanceState != null) {
            isTrackPlaying = savedInstanceState.getBoolean(IS_TRACK_PLAYING);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mContext = getApplicationContext();
        mPlaybackController.registerCallback(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        mNowPlayingMenuItem = menu.findItem(R.id.action_now_playing);
        mShareCurrentTrackMenuItem = menu.findItem(R.id.menu_item_share);

        mShareActionProvider = new ShareActionProvider(this);
        MenuItem shareItem = menu.findItem(R.id.menu_item_share);
        shareItem.setActionProvider(mShareActionProvider);

        updateMenuItems();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getBaseContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_now_playing) {
            displayMusicPlayer(mPlaybackController.getTracks(), mPlaybackController.getCurrentIndex());
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPlaybackStatusChange(String status) {
        if (status.equals(ACTION_PLAY)) {
            isTrackPlaying = true;
            updateShareIntent(mPlaybackController.getCurrentlyPlayingTrack());
        } else {
            isTrackPlaying = false;
        }
        updateMenuItems();
    }

    @Override
    public void onTrackChanged(int trackIndex) {

    }

    protected void displayMusicPlayer(ArrayList<ArtistTopTrackItem> artistTracks, int trackIndex) {
        if (mIsTwoPane) {
            MusicPlayerFragment musicPlayerFragment = MusicPlayerFragment.newInstance(artistTracks, trackIndex);
            musicPlayerFragment.show(getFragmentManager(), "dialog");
        } else {
            Intent intent = new Intent(this, MusicPlayerActivity.class);
            intent.putParcelableArrayListExtra(TRACK_ITEMS, artistTracks);
            intent.putExtra(TRACK_INDEX, trackIndex);
            startActivity(intent);
        }
    }

    protected void updateMenuItems() {
        mNowPlayingMenuItem.setVisible(isTrackPlaying);
        mShareCurrentTrackMenuItem.setVisible(isTrackPlaying);
    }

    private void updateShareIntent(ArtistTopTrackItem currentlyPlayingTrack) {
        String shareLink = currentlyPlayingTrack == null ? "" : currentlyPlayingTrack.getPreviewUrl();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareLink);
        sendIntent.setType("text/plain");
        mShareActionProvider.setShareIntent(sendIntent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_TRACK_PLAYING, isTrackPlaying);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlaybackController.unregisterCallback(this);
    }
}
