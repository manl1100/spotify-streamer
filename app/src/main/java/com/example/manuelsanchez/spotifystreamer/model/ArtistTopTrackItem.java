package com.example.manuelsanchez.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;


public class ArtistTopTrackItem implements Parcelable {

    private String artist;
    private String track;
    private String album;
    private long duration;
    private String previewUrl;
    private String imageUrl;


    public ArtistTopTrackItem() {
    }

    private ArtistTopTrackItem(Parcel in) {
        artist = in.readString();
        track = in.readString();
        album = in.readString();
        duration = in.readLong();
        previewUrl = in.readString();
        imageUrl = in.readString();
    }

    public static final Creator<ArtistTopTrackItem> CREATOR = new Creator<ArtistTopTrackItem>() {
        @Override
        public ArtistTopTrackItem createFromParcel(Parcel in) {
            return new ArtistTopTrackItem(in);
        }

        @Override
        public ArtistTopTrackItem[] newArray(int size) {
            return new ArtistTopTrackItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artist);
        dest.writeString(track);
        dest.writeString(album);
        dest.writeLong(duration);
        dest.writeString(previewUrl);
        dest.writeString(imageUrl);
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
