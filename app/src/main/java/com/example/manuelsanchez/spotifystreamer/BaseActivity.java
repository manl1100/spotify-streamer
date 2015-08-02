package com.example.manuelsanchez.spotifystreamer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_IDLE;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PLAY;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK_INDEX;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK_ITEMS;

/**
 * Created by Manuel Sanchez on 7/26/15
 */
public abstract class BaseActivity extends Activity implements MusicPlayerService.StatusChangeListener {

    protected MusicPlayerService mMusicPlayerService;
    private ShareActionProvider mShareActionProvider;
    protected Context mContext;

    private MenuItem mNowPlayingMenuItem;
    private MenuItem mShareCurrentTrackMenuItem;

    private boolean mBound;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MusicPlayerService.MusicPlayerBinder binder = (MusicPlayerService.MusicPlayerBinder) service;
            mMusicPlayerService = binder.getService();
            mMusicPlayerService.setStatusChangeListener(BaseActivity.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mContext = getApplicationContext();
        Intent intent = new Intent(mContext, MusicPlayerService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        mNowPlayingMenuItem = menu.findItem(R.id.action_now_playing);
        mShareCurrentTrackMenuItem = menu.findItem(R.id.menu_item_share);

        mShareActionProvider = new ShareActionProvider(this);
        MenuItem shareItem = menu.findItem(R.id.menu_item_share);
        shareItem.setActionProvider(mShareActionProvider);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getBaseContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_item_share) {
            Toast.makeText(this, "Share something", Toast.LENGTH_LONG).show();
        } else if (id == R.id.action_now_playing) {
            Toast.makeText(this, "Now playing", Toast.LENGTH_LONG).show();
            if (mMusicPlayerService.getTracks() != null) {
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(TRACK_ITEMS, mMusicPlayerService.getTracks());
                bundle.putInt(TRACK_INDEX, mMusicPlayerService.getCurrentIndex());

                MusicPlayerFragment musicPlayerFragment = new MusicPlayerFragment();
                musicPlayerFragment.setArguments(bundle);
                musicPlayerFragment.show(getFragmentManager(), "dialog");
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPlaybackStatusChange(String status) {
        if (status.equals(ACTION_PLAY)) {
            displayMenuItems();
            updateShareIntent(mMusicPlayerService.getCurrentlyPlayingTrack());
        } else if (status.equals(ACTION_IDLE)) {
            hideMenuItems();
        }
    }

    protected void displayMenuItems() {
        mNowPlayingMenuItem.setVisible(true);
        mShareCurrentTrackMenuItem.setVisible(true);

    }

    protected void hideMenuItems() {
        mNowPlayingMenuItem.setVisible(false);
        mShareCurrentTrackMenuItem.setVisible(false);
    }

    private void updateShareIntent(ArtistTopTrackItem currentlyPlayingTrack) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, currentlyPlayingTrack.getPreviewUrl());
        sendIntent.setType("text/plain");
        mShareActionProvider.setShareIntent(sendIntent);
    }

}
