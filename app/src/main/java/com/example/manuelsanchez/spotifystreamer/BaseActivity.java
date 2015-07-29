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

import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK_INDEX;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK_ITEMS;

/**
 * Created by Manuel Sanchez on 7/26/15
 */
public abstract class BaseActivity extends Activity {

    protected MusicPlayerService mMusicPlayerService;
    private ShareActionProvider mShareActionProvider;
    protected Context mContext;

    private boolean mBound;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MusicPlayerService.MusicPlayerBinder binder = (MusicPlayerService.MusicPlayerBinder) service;
            mMusicPlayerService = binder.getService();
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
        getMenuInflater().inflate(R.menu.menu_artist_search, menu);

        MenuItem shareItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = new ShareActionProvider(this);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
        sendIntent.setType("text/plain");

        mShareActionProvider.setShareIntent(sendIntent);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
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

    public void setShareActionProvider(ShareActionProvider mShareActionProvider) {
        this.mShareActionProvider = mShareActionProvider;
    }

    public void shareTrack(Intent shareIntent) {
        mShareActionProvider.setShareIntent(shareIntent);
    }

}
