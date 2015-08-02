package com.example.manuelsanchez.spotifystreamer.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.manuelsanchez.spotifystreamer.R;
import com.example.manuelsanchez.spotifystreamer.model.ArtistTopTrackItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class ArtistTopTracksListAdapter extends ArrayAdapter<ArtistTopTrackItem> {

    ArrayList<ArtistTopTrackItem> mTopTrack;


    public ArtistTopTracksListAdapter(Context context, int resource) {
        this(context, resource, new ArrayList<ArtistTopTrackItem>());
    }

    private ArtistTopTracksListAdapter(Context context, int resource, ArrayList<ArtistTopTrackItem> items) {
        super(context, resource, items);
        mTopTrack = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.artist_top_ten_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ArtistTopTrackItem track = getItem(position);

        Picasso.with(getContext())
                .load(track.getImageUrl())
                .resize(150, 150)
                .placeholder(R.drawable.ic_audiotrack_black_48dp)
                .error(R.drawable.ic_audiotrack_black_48dp)
                .centerCrop()
                .into(viewHolder.albumImage);

        viewHolder.trackName.setText(track.getTrack());
        viewHolder.albumName.setText(track.getAlbum());

        return convertView;
    }

    public ArrayList<ArtistTopTrackItem> getItems() {
        return mTopTrack;
    }

    private class ViewHolder {
        public final TextView trackName;
        public final TextView albumName;
        public final ImageView albumImage;

        public ViewHolder(View view) {
            trackName = (TextView) view.findViewById(R.id.artist_song);
            albumName = (TextView) view.findViewById(R.id.artist_album);
            albumImage = (ImageView) view.findViewById(R.id.artist_song_thumbnail);
        }
    }

}
