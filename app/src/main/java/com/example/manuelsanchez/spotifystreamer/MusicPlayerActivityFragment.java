package com.example.manuelsanchez.spotifystreamer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


public class MusicPlayerActivityFragment extends Fragment {

    public MusicPlayerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View musicPlayerView = inflater.inflate(R.layout.fragment_music_player, container, false);

        ArtistTopTrackItem track = getActivity().getIntent().getParcelableExtra(ArtistTopTracksFragment.TRACK);

        TextView artistTextView = (TextView) musicPlayerView.findViewById(R.id.artist_title);
        artistTextView.setText(track.getArtist());

        TextView albumTextView = (TextView) musicPlayerView.findViewById(R.id.artist_album);
        albumTextView.setText(track.getAlbum());

        ImageView albumCover = (ImageView) musicPlayerView.findViewById(R.id.artist_album_cover);
        Picasso.with(getActivity())
                .load(track.getImageUrl())
                .placeholder(R.drawable.ic_audiotrack_black_48dp)
                .error(R.drawable.ic_audiotrack_black_48dp)
                .into(albumCover);

        TextView trackTextView = (TextView) musicPlayerView.findViewById(R.id.artist_track);
        trackTextView.setText(track.getTrack());

        SeekBar seekBar = (SeekBar) musicPlayerView.findViewById(R.id.track_duration_bar);

        Button rewind = (Button) musicPlayerView.findViewById(R.id.rewind);

        Button pause = (Button) musicPlayerView.findViewById(R.id.pause);

        Button forward = (Button) musicPlayerView.findViewById(R.id.forward);


        return musicPlayerView;
    }

}
