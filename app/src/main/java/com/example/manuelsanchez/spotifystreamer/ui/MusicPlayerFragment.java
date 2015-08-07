package com.example.manuelsanchez.spotifystreamer.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import com.example.manuelsanchez.spotifystreamer.MusicPlayerService;
import com.example.manuelsanchez.spotifystreamer.PlaybackController;
import com.example.manuelsanchez.spotifystreamer.PlaybackState;
import com.example.manuelsanchez.spotifystreamer.R;
import com.example.manuelsanchez.spotifystreamer.model.ArtistTopTrackItem;
import com.example.manuelsanchez.spotifystreamer.util.TimeStringHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_NEXT;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PAUSE;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PLAY;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.ACTION_PREV;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK_INDEX;
import static com.example.manuelsanchez.spotifystreamer.SpotifyStreamerConstants.TRACK_ITEMS;


public class MusicPlayerFragment extends DialogFragment implements PlaybackController.Callback {

    private static final String LOG_TAG = MusicPlayerFragment.class.getSimpleName();

    private ArrayList<ArtistTopTrackItem> mTracks;
    private int mCurrentTrackIndex;
    private Context mContext;
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

    private Handler handler = new Handler();

    public MusicPlayerFragment() {
    }

    public static MusicPlayerFragment newInstance(ArrayList<ArtistTopTrackItem> tracks, int trackIndex) {
        MusicPlayerFragment musicPlayerFragment = new MusicPlayerFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(TRACK_ITEMS, tracks);
        bundle.putInt(TRACK_INDEX, trackIndex);
        musicPlayerFragment.setArguments(bundle);
        return musicPlayerFragment;
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
        PlaybackController playbackController = PlaybackController.getInstance();
        playbackController.registerCallback(this);
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

        elapsed = (TextView) musicPlayerView.findViewById(R.id.time_elapse);

        remaining = (TextView) musicPlayerView.findViewById(R.id.time_remaining);

        mSeekBar = (SeekBar) musicPlayerView.findViewById(R.id.track_duration_bar);
//        mSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        previous = (Button) musicPlayerView.findViewById(R.id.rewind);
        previous.setOnClickListener(onPreviousTrackClickListener);

        playPause = (ToggleButton) musicPlayerView.findViewById(R.id.pause);
        playPause.setOnClickListener(onPlayPauseClickListener);

        next = (Button) musicPlayerView.findViewById(R.id.forward);
        next.setOnClickListener(onNextTrackClickListener);

        updateTrack();
        startTrackElapseTime();
        return musicPlayerView;
    }

    @Override
    public void onStart() {
        super.onStart();
        /**
         * TODO: Take care of case when player is paused then device is rotated
         * */
        mContext = getActivity().getApplicationContext();
        PlaybackController playbackController = PlaybackController.getInstance();
        Intent intent = new Intent(mContext, MusicPlayerService.class);
        intent.setAction(ACTION_PLAY);
        intent.putParcelableArrayListExtra(TRACK_ITEMS, mTracks);
        intent.putExtra(TRACK_INDEX, mCurrentTrackIndex);
        mContext.startService(intent);
    }

    private void startTrackElapseTime() {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PlaybackController playbackController = PlaybackController.getInstance();
                if (playbackController.getPlaybackState().equals(PlaybackState.PLAY)) {
                    int currentPosition = playbackController.getCurrentPosition() / 1000;
                    int remainingTime = (playbackController.getDuration() / 1000) - currentPosition;
                    mSeekBar.setProgress(currentPosition);
                    elapsed.setText(TimeStringHelper.getFormatedString(currentPosition));
                    remaining.setText(TimeStringHelper.getFormatedString(remainingTime));
                    mSeekBar.setMax(playbackController.getDuration() / 1000);
                }
                handler.postDelayed(this, 1000);
            }
        });
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

    private View.OnClickListener onPlayPauseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean isPlay = ((ToggleButton) view).isChecked();
            Intent intent;
            if (isPlay) {
                intent = new Intent(mContext, MusicPlayerService.class);
                intent.setAction(ACTION_PLAY);
                intent.putParcelableArrayListExtra(TRACK_ITEMS, mTracks);
                intent.putExtra(TRACK_INDEX, mCurrentTrackIndex);
            } else {
                intent = new Intent(mContext, MusicPlayerService.class);
                intent.setAction(ACTION_PAUSE);
            }
            mContext.startService(intent);

        }
    };

    private View.OnClickListener onNextTrackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mCurrentTrackIndex = mCurrentTrackIndex == mTracks.size() - 1 ? mCurrentTrackIndex : ++mCurrentTrackIndex;
            Intent intent = new Intent(mContext, MusicPlayerService.class);
            intent.setAction(ACTION_NEXT);
            mContext.startService(intent);
            updateTrack();
        }
    };

    private View.OnClickListener onPreviousTrackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mCurrentTrackIndex = mCurrentTrackIndex == 0 ? mCurrentTrackIndex : --mCurrentTrackIndex;
            Intent intent = new Intent(mContext, MusicPlayerService.class);
            intent.setAction(ACTION_PREV);
            mContext.startService(intent);
            updateTrack();
        }
    };

//    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
//
//        @Override
//        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//            if (fromUser) {
//                PlaybackController.getInstance().seekTo(progress);
//            }
//        }
//
//        @Override
//        public void onStartTrackingTouch(SeekBar seekBar) {
//
//        }
//
//        @Override
//        public void onStopTrackingTouch(SeekBar seekBar) {
//
//        }
//    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        PlaybackController playbackController = PlaybackController.getInstance();
        playbackController.unregisterCallback(this);
    }

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
    }

    @Override
    public void onTrackChanged(int trackIndex) {
        Log.d(LOG_TAG, "onTrackChanged");
        mCurrentTrackIndex = trackIndex;
        updateTrack();
    }
}
