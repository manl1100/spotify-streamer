package com.example.manuelsanchez.spotifystreamer;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_NEXT;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PAUSE;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PLAY;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PREV;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_RESUME;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK_INDEX;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK_ITEMS;


public class MusicPlayerFragment extends DialogFragment implements MusicPlayerService.Callback, MusicPlayerService.StatusChangeListener {

    private static final String LOG_TAG = MusicPlayerFragment.class.getSimpleName();

    private ArrayList<ArtistTopTrackItem> mTracks;
    private int mCurrentTrackIndex;
    private Context mContext;
    private MusicPlayerService musicPlayerService;
    private boolean mBound;
    private TextView artistTextView;
    private TextView albumTextView;
    private TextView elapsed;
    private TextView remaining;
    private ImageView albumCover;
    private TextView trackTextView;
    private SeekBar mSeekBar;
    private Button previous;
    private ToggleButton playPause;
    private Button next;

    private String playbackState;


    public MusicPlayerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mTracks = savedInstanceState.getParcelableArrayList(TRACK);
            mCurrentTrackIndex = savedInstanceState.getInt(TRACK_INDEX);
        } else {
            mTracks = getArguments().getParcelableArrayList(TRACK_ITEMS);
            mCurrentTrackIndex = getArguments().getInt(TRACK_INDEX);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View musicPlayerView = inflater.inflate(R.layout.fragment_music_player, container, false);

        artistTextView = (TextView) musicPlayerView.findViewById(R.id.artist_title);
        albumTextView = (TextView) musicPlayerView.findViewById(R.id.artist_album);
        albumCover = (ImageView) musicPlayerView.findViewById(R.id.artist_album_cover);
        trackTextView = (TextView) musicPlayerView.findViewById(R.id.artist_track);
        updateTrack();

        elapsed = (TextView) musicPlayerView.findViewById(R.id.time_elapse);
        elapsed.setText("0:00");

        remaining = (TextView) musicPlayerView.findViewById(R.id.time_remaining);
        remaining.setText("0:30");

        mSeekBar = (SeekBar) musicPlayerView.findViewById(R.id.track_duration_bar);

        previous = (Button) musicPlayerView.findViewById(R.id.rewind);
        previous.setOnClickListener(onPreviousTrackClickListener());

        playPause = (ToggleButton) musicPlayerView.findViewById(R.id.pause);
        playPause.setOnClickListener(onPlayPauseClickListener());

        next = (Button) musicPlayerView.findViewById(R.id.forward);
        next.setOnClickListener(onNextTrackClickListener());

        return musicPlayerView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mContext = getActivity().getApplicationContext();
        Intent intent = new Intent(mContext, MusicPlayerService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void updateTrack() {
        artistTextView.setText(mTracks.get(mCurrentTrackIndex).getArtist());
        albumTextView.setText(mTracks.get(mCurrentTrackIndex).getAlbum());
        Picasso.with(getActivity())
                .load(mTracks.get(mCurrentTrackIndex).getImageUrl())
                .placeholder(R.drawable.ic_audiotrack_black_48dp)
                .error(R.drawable.ic_audiotrack_black_48dp)
                .into(albumCover);
        trackTextView.setText(mTracks.get(mCurrentTrackIndex).getTrack());
    }

    private View.OnClickListener onPlayPauseClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isPlay = ((ToggleButton) view).isChecked();
                if (isPlay) {
                    Intent intent = new Intent(mContext, MusicPlayerService.class);
                    intent.setAction(ACTION_RESUME);
                    mContext.startService(intent);
                } else {
                    Intent intent = new Intent(mContext, MusicPlayerService.class);
                    intent.setAction(ACTION_PAUSE);
                    mContext.startService(intent);
                }
            }
        };
    }

    private View.OnClickListener onNextTrackClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentTrackIndex = mCurrentTrackIndex == mTracks.size() - 1 ? mCurrentTrackIndex : ++mCurrentTrackIndex;
                Intent intent = new Intent(mContext, MusicPlayerService.class);
                intent.setAction(ACTION_NEXT);
                mContext.startService(intent);
                updateTrack();
            }
        };
    }

    private View.OnClickListener onPreviousTrackClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentTrackIndex = mCurrentTrackIndex == 0 ? mCurrentTrackIndex : --mCurrentTrackIndex;
                Intent intent = new Intent(mContext, MusicPlayerService.class);
                intent.setAction(ACTION_PREV);
                mContext.startService(intent);
                updateTrack();
            }
        };
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            MusicPlayerService.MusicPlayerBinder binder = (MusicPlayerService.MusicPlayerBinder) service;
            musicPlayerService = binder.getService();
            musicPlayerService.setCallBack(MusicPlayerFragment.this);
            musicPlayerService.setStatusChangeListener(MusicPlayerFragment.this);
            mBound = true;

            Intent intent = new Intent(mContext, MusicPlayerService.class);
            intent.setAction(ACTION_PLAY);
            intent.putParcelableArrayListExtra(TRACK_ITEMS, mTracks);
            intent.putExtra(TRACK_INDEX, mCurrentTrackIndex);
            mContext.startService(intent);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(TRACK, mTracks);
        outState.putInt(TRACK_INDEX, mCurrentTrackIndex);
    }

    @Override
    public void onPlaybackStatusChange(String status) {
        Log.d(LOG_TAG, "onPlaybackStatusChange");
        playPause.setChecked(status.equals(ACTION_PLAY));
        playbackState = status;
    }

    @Override
    public void onTrackChanged(int trackIndex) {
        Log.d(LOG_TAG, "onTrackChanged");
        mCurrentTrackIndex = trackIndex;
        updateTrack();
    }
}
